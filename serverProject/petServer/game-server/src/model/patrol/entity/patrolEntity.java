/**
 * created by tool DAOGenerate
 */
package model.patrol.entity;

import cfg.FunctionOpenLvConfig;
import cfg.VIPConfig;
import com.bowlong.third.FastJSON;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import common.tick.GlobalTick;
import model.battle.BattleUtil;
import model.mainLine.dbCache.mainlineCache;
import model.obj.BaseObj;
import model.patrol.dbCache.patrolCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import protocol.Battle.ExtendProperty;
import protocol.Common.EnumFunction;
import protocol.Patrol;
import protocol.Patrol.PatrolChamber;
import protocol.Patrol.PatrolSaleMan;
import protocol.Patrol.PatrolStatus;
import protocol.Patrol.SC_PatrolRewardRefresh;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.LogUtil;
import util.PatrolUtil;
import util.RandomUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolRewardRefresh_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PatrolUpdate_VALUE;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class patrolEntity extends BaseObj {
    public patrolEntity() {
    }

    public patrolEntity(String initPlayerId, int cfgId, int mapId) {
        this.idx = IdGenerator.getInstance().generateId();
        this.playeridx = initPlayerId;
        // 读取配置，生成地图
        createEntityMap(cfgId, mapId);
    }

    private void createEntityMap(int cfgId, int mapId) {
        PatrolMapHelper patrolMapHelper = new PatrolMapHelper();
        PatrolMapInitResult returnMap = patrolMapHelper.createPatrolTree(playeridx, cfgId, mapId);
        this.patrolTree = returnMap.getInitPoint();
        this.patrolStatusEntity = buildNewPatrolStatus(returnMap);
        clearFinish();
    }

    public void reCreate(int cfgId, int mapId) {
        createEntityMap(cfgId, mapId);
    }


    @Override
    public String getClassType() {
        return "patrolEntity";
    }

    @Override
    public void putToCache() {
        patrolCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.patrolstatus = patrolStatusEntity.toByteArray();
    }

    /**
     * 主键
     */
    private String idx;

    /**
     * 玩家id
     */
    private String playeridx;

    /**
     * 巡逻队地图详情
     */
    private String map;

    /**
     * 玩家当前状态
     */
    private byte[] patrolstatus;

    /**
     * 已完成探索：0否1是
     */
    private int finish;


    /**
     * 获得主键
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置主键
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得玩家id
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置玩家id
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得巡逻队地图详情
     */
    public String getMap() {
        return map;
    }

    /**
     * 设置巡逻队地图详情
     */
    public void setMap(String map) {
        this.map = map;
    }

    /**
     * 获得玩家当前状态
     */
    public byte[] getPatrolstatus() {
        return patrolstatus;
    }

    /**
     * 设置玩家当前状态
     */
    public void setPatrolstatus(byte[] patrolstatus) {
        this.patrolstatus = patrolstatus;
    }

    /**
     * 获得已完成探索：0否1是
     */
    public int getFinish() {
        return finish;
    }

    /**
     * 设置已完成探索：0否1是
     */
    public void setFinish(int finish) {
        this.finish = finish;
    }

    @Override
    public String getBaseIdx() {
        return idx;
    }

    /***************************分割**********************************/
    private PatrolStatus patrolStatusEntity;

    private PatrolTree patrolTree;

    public PatrolStatus getPatrolStatusEntity() {
        return patrolStatusEntity;
    }

    public void setPatrolStatusEntity(PatrolStatus patrolStatusEntity) {
        this.patrolStatusEntity = patrolStatusEntity;
    }

    public PatrolTree getPatrolTree() {
        return patrolTree;
    }

    public void setPatrolTree(PatrolTree patrolTree) {
        this.patrolTree = patrolTree;
    }

    public void toBuilder() throws InvalidProtocolBufferException {
        if (patrolstatus != null) {
            patrolStatusEntity = PatrolStatus.parseFrom(patrolstatus);
        } else {
            patrolStatusEntity = PatrolStatus.getDefaultInstance();
        }
        patrolTree = FastJSON.parseObject(map, PatrolTree.class);
    }

    public void refresh() {
        patrolstatus = patrolStatusEntity.toByteArray();
        map = FastJSON.toJSONString(patrolTree);
    }

    /**
     * 刷新奖励
     */
    public void sendRewardRefreshMsg() {
        SC_PatrolRewardRefresh.Builder result = SC_PatrolRewardRefresh.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();
        retCode.setRetCode(RetCodeEnum.RCE_Success);
        result.setResult(retCode);
        result.addAllRewardList(getPatrolStatusEntity().getRewardList());
        GlobalData.getInstance().sendMsg(getPlayeridx(), SC_PatrolRewardRefresh_VALUE, result);
    }


    private void clearFinish() {
        finish = 0;
    }


    private PatrolStatus buildNewPatrolStatus( PatrolMapInitResult returnMap) {
        PatrolChamber patrolChamber = PatrolChamber.newBuilder().addAllPetRuneList(returnMap.getRuneList())
                .addAllArtifactAdditionKeys(returnMap.getArtifactAddition().keySet())
                .addAllArtifactAdditionValues(returnMap.getArtifactAddition().values())
                .addAllNewTitleId(returnMap.getTitleIds())
                .addAllArtifact(returnMap.getArtifacts()).build();
        PatrolSaleMan patrolSaleMan = PatrolSaleMan.newBuilder().addAllGoodsList(RandomUtil.randomSaleGoods()).build();
        // 根据地图初始化状态
        PatrolStatus.Builder builder = PatrolStatus.newBuilder()
                .setLocation(PatrolUtil.getPointByTree(patrolTree))
                .setGreed(0)
                .setTime(GlobalTick.getInstance().getCurrentTime())
                .setFreeReborn(getRebornTime(playeridx))
                .setMapId(returnMap.getMapId())
                .setPatrolChamber(patrolChamber)
                .setSaleMan(patrolSaleMan)
                .setTodayCreateCount(getTodayCreateCount() + 1)
                .setTodayDiffNodeId(getTodayDiffNodeId());

        //怪物难度
        ExtendProperty.Builder propertyAdjust = getTodayMonsterDiff();
        if (propertyAdjust != null) {
            builder.setMonsterExProperty(propertyAdjust);
        }
        //怪物等级
        EventUtil.recreateMonsterDiff(getPlayeridx(), EnumFunction.Patrol, getTodayDiffNodeId());
        return builder.build();
    }

    public int getTodayCreateCount() {
        return getPatrolStatusEntity() == null ? 0 : getPatrolStatusEntity().getTodayCreateCount();
    }

    /**
     * 按vip等级获取免费重生次数
     *
     * @param playerId 玩家id
     * @return 免费重生次数
     */
    public static int getRebornTime(String playerId) {
        playerEntity player = playerCache.getByIdx(playerId);
        int freeRebornTime;
        if (player != null) {
            freeRebornTime = VIPConfig.getById(player.getVip()).getPatrolreborntimes();
        } else {
            freeRebornTime = VIPConfig.getById(GameConst.CONFIG_ID).getPatrolreborntimes();
            LogUtil.error("error in PatrolServiceImpl,method patrolMapInit(),playerId = " + playerId + "\n");
        }
        return freeRebornTime;
    }

    /**
     * 之前的探索是否已完成
     *
     * @return 探索完成情况
     */
    public boolean gameNotFinished() {
        return finish == 0;
    }

    /**
     * 当前状态是否失败
     *
     * @return 失败true/没有失败false
     */
    public boolean gameFailed() {
        return patrolStatusEntity.getNowFailure() != 0;
    }

    public void sendPatrolUpdate() {
        Patrol.SC_PatrolUpdate.Builder msg = Patrol.SC_PatrolUpdate.newBuilder();
        PatrolStatus patrolStatus = getPatrolStatusEntity();
        msg.setFreeReborn(patrolStatus.getFreeReborn());
        msg.setTodayCreateCount(patrolStatus.getTodayCreateCount());
        GlobalData.getInstance().sendMsg(getPlayeridx(), SC_PatrolUpdate_VALUE, msg);
    }

    public void playerLeavePatrol() {
        if (getFinish() == 1 || patrolStatusEntity.getNowFailure() == 1) {
            return;
        }
        EventUtil.triggerPausePatrolMission(getPlayeridx(), true);
    }

    public void updateDailyData() {
        if (!PlayerUtil.queryFunctionUnlock(getPlayeridx(), EnumFunction.Patrol)) {
            return;
        }
        patrolStatusEntity = getPatrolStatusEntity().toBuilder().clearTodayCreateCount().build();

        initTodayMonsterDiffNodeId();
    }

    private void initTodayMonsterDiffNodeId() {
        if (getPatrolStatusEntity() == null) {
            return;
        }
        int playerCurNode = mainlineCache.getInstance().getPlayerCurNode(getPlayeridx());
        PatrolStatus newBuild = getPatrolStatusEntity().toBuilder().setTodayDiffNodeId(playerCurNode).build();
        setPatrolStatusEntity(newBuild);
    }

    private ExtendProperty.Builder getTodayMonsterDiff() {
        return BattleUtil.initMonsterExPropertyAdjustByNodeId(getTodayDiffNodeId(), 2);
    }

    public int getTodayDiffNodeId() {
        return getPatrolStatusEntity() == null ?
                mainlineCache.getInstance().getPlayerCurNode(getPlayeridx()) : getPatrolStatusEntity().getTodayDiffNodeId();
    }
}