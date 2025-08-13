package model.battle;

import common.GlobalData;
import common.GlobalThread;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import model.battle.pool.PveControllerPool;
import model.battle.pool.PvpControllerPool;
import model.battle.preInfo.AbstractPreWarInfo;
import model.player.util.PlayerUtil;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.*;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.CS_EnterFight;
import protocol.Battle.SC_BattleRevertData;
import protocol.Battle.SC_EnterFight;
import protocol.BattleMono.CS_FrameData;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.ClassUtil;
import util.GameUtil;
import util.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author huhan
 * @date 2020/04/23
 */
public class BattleManager implements Runnable {

    private static final PrepareWar.SC_PreWarInfo.Builder DEFAULT_PRE_WAR_INFO = PrepareWar.SC_PreWarInfo.newBuilder();

    private static BattleManager instance;

    public static BattleManager getInstance() {
        if (instance == null) {
            synchronized (BattleManager.class) {
                if (instance == null) {
                    instance = new BattleManager();
                }
            }
        }
        return instance;
    }

    private BattleManager() {
    }

    private final Map<String, AbstractBattleController> playerControllerData = new ConcurrentHashMap<>();

    private final AtomicBoolean run = new AtomicBoolean(true);

    private final Map<BattleTypeEnum, AbstractControllerPool> poolMap = new ConcurrentHashMap<>();

    public boolean init() {
        initPool();
        GlobalThread.getInstance().execute(this);
        initPreWarInfoHandler();
        return true;
    }

    private void initPreWarInfoHandler() {
        List<Class<AbstractPreWarInfo>> subClass = ClassUtil.getSubClass("model.battle.preInfo", AbstractPreWarInfo.class);
        if (CollectionUtils.isEmpty(subClass)) {
            LogUtil.error("initPreWarInfoHandler error by getSubClass is empty");
        }
        for (Class<AbstractPreWarInfo> aClass : subClass) {
            try {
                AbstractPreWarInfo instance = aClass.newInstance();
                map.put(instance.getTypeEnum(), instance);
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
    }

    private void initPool() {
        putControllerPool(new PvpControllerPool());
        putControllerPool(new PveControllerPool());
    }

    public void putControllerPool(AbstractControllerPool pool) {
        if (pool == null) {
            return;
        }
        this.poolMap.put(pool.getBattleType(), pool);
    }

    public AbstractControllerPool getPool(BattleTypeEnum battleType) {
        if (battleType == null || battleType == BattleTypeEnum.BTE_Null) {
            return null;
        }
        return this.poolMap.get(battleType);
    }

    public void stop() {
        run.set(false);
    }

    @Override
    public void run() {
        LogUtil.info("start battle tick, cur time =" + GlobalTick.getInstance().getCurrentTime());
        while (run.get()) {
            try {
                //标记需要回收的controller

                Map<String, AbstractBattleController> cycleController = new HashMap<>();
                for (Entry<String, AbstractBattleController> entry : playerControllerData.entrySet()) {
                    AbstractBattleController value = entry.getValue();
                    synchronized (value) {
                        if (value.timeOutTick()) {
                            cycleController.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                if (!cycleController.isEmpty()) {
                    tryToRecycle(cycleController);
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            } finally {
                GameUtil.sleep(ServerConfig.getInstance().getBattleTickCycle());
            }
        }
        LogUtil.info("battle end tick");
    }

    /**
     * 尝试移除玩家controller
     *
     * @param tryToRemove
     */
    private synchronized void tryToRecycle(Map<String, AbstractBattleController> tryToRemove) {
        if (tryToRemove == null || tryToRemove.isEmpty()) {
            return;
        }

        for (Entry<String, AbstractBattleController> entry : tryToRemove.entrySet()) {
            AbstractBattleController controller = this.playerControllerData.get(entry.getKey());
            if (Objects.equals(controller, entry.getValue())) {
                this.playerControllerData.remove(entry.getKey());
            } else {
                LogUtil.error("player:{} battleController remove Failed, subType={}", entry.getKey(), controller.getSubBattleType());
            }
            recycleController(controller);
        }
    }


    /**
     * 获取战斗控制器
     *
     * @return
     */
    public AbstractBattleController createBattleController(String playerIdx, BattleTypeEnum battleType, BattleSubTypeEnum subType) {
        if (!PlayerUtil.playerIsExist(playerIdx)) {
            LogUtil.error("BattleManager.getBattleController, error params player is not exist, playerIdx:" + playerIdx);
            return null;
        }

        AbstractControllerPool pool = getPool(battleType);
        if (pool == null) {
            LogUtil.error("controller pool is not exist, type =" + battleType);
            return null;
        }

        AbstractBattleController controller = pool.getControllerByType(subType);
        if (controller != null) {
            controller.setPlayerIdx(playerIdx);
        }
        return controller;
    }

    public void recycleController(AbstractBattleController controller) {
        if (controller == null) {
            return;
        }

        AbstractControllerPool pool = getPool(controller.getBattleType());
        if (pool == null) {
            return;
        }

        pool.recycleController(controller);
    }

    /**
     * 玩家是否在战斗中
     *
     * @param playerIdx
     * @return
     */
    public boolean isInBattle(String playerIdx) {
        AbstractBattleController controller = playerControllerData.get(playerIdx);
        return controller != null && controller.isInBattle();
    }

    public RetCodeEnum enterPveBattle(String playerIdx, CS_EnterFight req) {
        AbstractBattleController controller = null;
        RetCodeEnum retCodeEnum;
        if (req == null) {
            LogUtil.warn("player:{} initBattle fail ,req is null", playerIdx);
            retCodeEnum = RetCodeEnum.RCE_ErrorParam;
        } else if (isInBattle(playerIdx)) {
            retCodeEnum = RetCodeEnum.RCE_Battle_RepeatedEnterBattle;
        } else {
            AbstractBattleController tmpController = playerControllerData.get(playerIdx);
            if (tmpController != null) {
                LogUtil.error("Player:{} battleController is not null, tmpPlayerIdx={}, tmpSubType={}", playerIdx, tmpController.getPlayerIdx(), tmpController.getSubBattleType());
            }
            controller = createBattleController(playerIdx, BattleTypeEnum.BTE_PVE, req.getType());
            if (controller == null) {
                LogUtil.warn("cam not create pve controller, subtype = " + req.getType());
                retCodeEnum = RetCodeEnum.RCE_UnknownError;
            } else {
                retCodeEnum = controller.initBattle(req);
            }
        }

        SC_EnterFight.Builder resultBuilder;
        if (retCodeEnum == RetCodeEnum.RCE_Success) {
            resultBuilder = controller.buildEnterBattleBuilder();
            controller.setPveEnterFightData(resultBuilder);
            LogUtil.info("player[{}] enterFight fightMakeId={}, subtype={}, battleId={}",
                    playerIdx, resultBuilder.getFightMakeId(), resultBuilder.getSubType(), resultBuilder.getBattleId());
        } else {
            resultBuilder = SC_EnterFight.newBuilder()
                    .setSubTypeValue(req == null ? 0 : req.getTypeValue())
                    .setRetCode(GameUtil.buildRetCode(retCodeEnum));
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_EnterFight_VALUE, resultBuilder);

        if (controller != null) {
            managerBattle(controller);
        }

        return resultBuilder.getRetCode().getRetCode();
    }

    /**
     * 将control移交管理
     * ,要判断玩家是否在战斗中，调用下方接口判断 {@link BattleManager#isInBattle(java.lang.String)}
     *
     * @param controller
     * @return
     */
    public boolean managerBattle(AbstractBattleController controller) {
        if (controller == null) {
            return false;
        }
        this.playerControllerData.put(controller.getPlayerIdx(), controller);
        return true;
    }

    /**
     * 战斗结算
     *
     * @param playerIdx
     * @param battleId
     * @param winCamp
     */
    public void settleBattle(String playerIdx, long battleId, int winCamp) {
        AbstractBattleController controller = playerControllerData.get(playerIdx);
        if (controller == null) {
            LogUtil.error("playerIdx=" + playerIdx + ", is not in battle, can not settle battle");
            return;
        }
        synchronized (controller) {
            controller.settleBattle(battleId, winCamp);
        }
    }

    public void settleBattle(String playerIdx, int winCamp) {
        AbstractBattleController controller = playerControllerData.get(playerIdx);
        if (controller == null) {
            LogUtil.error("playerIdx=" + playerIdx + ", is not in battle, can not settle battle");
            return;
        }
        synchronized (controller) {
            controller.settleBattle(winCamp);
        }
    }

    /**
     * @param playerIdx
     * @param pvpBattleResultData
     * 下发观战玩家战斗结果信息
     */
    public void battleEndWatchResultSend(String playerIdx, ServerTransfer.PvpBattleResultData pvpBattleResultData) {
        Battle.SC_BattleResult.Builder builder = Battle.SC_BattleResult.newBuilder();
        builder.setFightMakeId(0);
        builder.setBattleSubType(pvpBattleResultData.getSubBattleType());
        builder.setBattleId(pvpBattleResultData.getBattleId());
        builder.setWinnerCamp(pvpBattleResultData.getBattleResult().getWinnerCamp());
        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        builder.setRemainBattle(false);
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_BattleResult_VALUE, builder);
    }

    public void settleBattle(String playerIdx, CS_BattleResult req) {
        AbstractBattleController controller = playerControllerData.get(playerIdx);
        if (controller == null) {
            LogUtil.error("playerIdx=" + playerIdx + ", is not in battle, can not settle battle");
            return;
        }

        synchronized (controller) {
            controller.settleBattle(req);
        }
    }

    public void onOwnerLeave(String playerIdx, boolean settlePvpBattle) {
        AbstractBattleController controller = playerControllerData.get(playerIdx);
        if (controller == null) {
            return;
        }
        synchronized (controller) {
            controller.onOwnerLeave(settlePvpBattle);
        }
    }


    /**
     * SC_BattleRevertData 除了pvp(战斗服处理)战斗，其他状态都会在这里发
     * 字段：IsMistBattling， 不在战斗中也会发送一个false,如果处于迷雾深林中发送true
     *
     * @param playerIdx
     * @param isResume
     */
    public void onPlayerLogin(String playerIdx, boolean isResume) {
        AbstractBattleController controller = this.playerControllerData.get(playerIdx);
        if (controller != null && controller.isInBattle()) {
            synchronized (controller) {
                if (controller.isHangOn()) {
                    //挂起的战斗,进入下一场战斗
                    controller.enterNextBattle();
                    controller.setPveEnterFightData(controller.buildEnterBattleBuilder());
                }
                controller.onPlayerLogin(isResume);
            }
        } else {
            SC_BattleRevertData.Builder builder = SC_BattleRevertData.newBuilder();
            if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(playerIdx) > 0) {
                builder.setIsMistBattling(true);
            }
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_BattleRevertData_VALUE, builder);
        }
    }

    public boolean playerInBattle(String playerIdx, BattleSubTypeEnum typeEnum) {
        AbstractBattleController controller = this.playerControllerData.get(playerIdx);
        if (typeEnum != null && controller != null && controller.isInBattle()) {
            return typeEnum == controller.getSubBattleType();
        }
        return false;
    }

    public void handleBattleFrameData(String playerIdx, CS_FrameData req) {
        handleAllBattleFrameData(playerIdx, Collections.singletonList(req));
    }

    public void handleAllBattleFrameData(String playerIdx, List<CS_FrameData> frameList) {
        if (GameUtil.collectionIsEmpty(frameList)) {
            return;
        }

        AbstractBattleController controller = this.playerControllerData.get(playerIdx);
        if (controller == null) {
            return;
        }
        synchronized (controller) {
            for (CS_FrameData frameData : frameList) {
                if (BattleUtil.checkSerializedFrame(frameData.getOperation())) {
                    controller.handleBattleFrameData(frameData);
                } else {
                    LogUtil.error("Battle frame data deserialize error,frameType="
                            + frameData.getOperation().getFramType() + ",playerIdx=" + controller.getPlayerIdx());
                }
            }
        }
    }

    /**
     * 玩家战斗模式
     *
     * @param playerIdx
     * @return
     */
    public BattleTypeEnum getBattleType(String playerIdx) {
        if (!isInBattle(playerIdx)) {
            return BattleTypeEnum.BTE_Null;
        }

        AbstractBattleController controller = this.playerControllerData.get(playerIdx);
        if (controller == null) {
            return BattleTypeEnum.BTE_Null;
        }
        return controller.getBattleType();
    }

    /**
     * 清空战斗数据
     *
     * @param playerIdx
     */
    public void clearController(String playerIdx) {
        AbstractBattleController controller = playerControllerData.get(playerIdx);
        if (controller == null) {
            return;
        }
        synchronized (controller) {
            controller.clear();
        }
    }

    public AbstractBattleController getController(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }
        return this.playerControllerData.get(playerIdx);
    }

    private final static Map<Battle.BattleSubTypeEnum, AbstractPreWarInfo> map = new HashMap<>();


    public PrepareWar.SC_PreWarInfo.Builder buildPreWarInfo(String playerIdx, PrepareWar.CS_PreWarInfo req) {
        AbstractPreWarInfo abstractPreWarInfo = map.get(req.getType());
        if (abstractPreWarInfo == null) {
            return DEFAULT_PRE_WAR_INFO;
        }
        return abstractPreWarInfo.preBattleInfo(playerIdx, req);

    }

    /**
     * @param battleId
     * @param playerIdx
     * 发送战场服请求观战
     */
    public void sendBattleServerBattleWatch(String battleId, String playerIdx) {
        BaseNettyClient nettyClient = BattleServerManager.getInstance().getBsClientByPlayerIdx(playerIdx, true);
        if (nettyClient == null) {
            return;
        }
        sendBattleServerBattleWatch(nettyClient, battleId, playerIdx);
    }

    /**
     * @param bnc
     * @param battleId
     * @param playerIdx
     * 发送战场服请求观战
     */
    public void sendBattleServerBattleWatch(BaseNettyClient bnc, String battleId, String playerIdx) {
        ServerTransfer.GS_BS_BattleWatch.Builder msg = ServerTransfer.GS_BS_BattleWatch.newBuilder();
        msg.setRoomId(battleId);
        msg.setPlayerId(playerIdx);
        int serverIdx = ServerConfig.getInstance().getServer();
        msg.setSvrIndex(serverIdx);
        bnc.send(MessageId.MsgIdEnum.GS_BS_BattleWatch_VALUE, msg);
    }

}