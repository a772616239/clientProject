package model.battle.pve;


import cfg.KeyNodeConfig;
import cfg.KeyNodeConfigObject;
import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import cfg.PlotAffect;
import cfg.PlotAffectObject;
import common.GameConst.EventType;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mainLine.util.MainLineUtil;
import model.player.util.PlayerUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.LogService;
import platform.logs.entity.GamePlayLog;
import protocol.Battle;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.SC_BattleResult;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MainLine.MainLineProgress;
import protocol.MainLineDB.DB_MainLine;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.ArrayUtil;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/04/23
 */
public class MainLinePveBattleController extends AbstractPveBattleController {



    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (GameUtil.collectionIsEmpty(enterParams)) {
            return false;
        }
        putEnterParam("nodeId", enterParams.get(0));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(getPlayerIdx());
        if (entity == null) {
            LogUtil.error("playerIdx[" + getPlayerIdx() + "] mainLineEntity is null");
            return RetCodeEnum.RCE_UnknownError;
        }
        DB_MainLine.Builder dbBuilder = entity.getDBBuilder();
        if (dbBuilder == null) {
            LogUtil.error("playerIdx[" + getPlayerIdx() + "] mainLineDbData is null");
            return RetCodeEnum.RCE_UnknownError;
        }

        MainLineProgress.Builder mainLineProBuilder = dbBuilder.getMainLineProBuilder();
        int curCheckPoint = mainLineProBuilder.getCurCheckPoint();

        MainLineCheckPointObject checkPointCfg = MainLineCheckPoint.getById(curCheckPoint);
        if (checkPointCfg == null) {
            LogUtil.error("EnterBattleHandler.getFightMakeId,mainLineCheckPoint, ["
                    + curCheckPoint + "] cfg is null");
            return RetCodeEnum.RCE_MainLine_CheckPointCfgIsNull;
        }

        //关卡是否解锁
        if (checkPointCfg.getUnlocklv() > PlayerUtil.queryPlayerLv(getPlayerIdx())) {
            return RetCodeEnum.RCE_MainLine_CheckPointIsLock;
        }

        int nodeId = getIntEnterParam("nodeId");

        int canBattleMaxKeyNodeId = curKeyNodeCanBattleMaxKeyNode(dbBuilder.getKeyNodeId());

        if (nodeId >= canBattleMaxKeyNodeId) {
            return RetCodeEnum.RCE_MainLine_CompleteKeyNodeMissionFirst;
        }

        //该关卡是否包含该节点
        if (!ArrayUtil.intArrayContain(checkPointCfg.getNodelist(), nodeId)) {
            LogUtil.error("EnterBattleHandler.getFightMakeId, nodeId [" + nodeId
                    + "] is not in checkPoint [" + curCheckPoint + "]");
            return RetCodeEnum.RCE_MainLine_NodeCanNotReach;
        }

        //该节点是否已解锁（传送型节点可能不在解锁的范围内,传送型节点可以重复闯关）
        if (checkPointCfg.getType() != 4) {
            if (!mainLineProBuilder.getUnlockNodesList().contains(nodeId)) {
                LogUtil.error("EnterBattleHandler.getFightMakeId, nodeId [" + nodeId
                        + "] is not in checkPoint [" + curCheckPoint + "]");
                return RetCodeEnum.RCE_MainLine_CurNodeIsLock;
            }
        }

        //该节点是否已经闯关过了
        if (mainLineProBuilder.getProgressList().contains(nodeId)) {
            return RetCodeEnum.RCE_MainLine_CurNodeIsPassed;
        }


        int actualNode = tryReplaceNodeId(nodeId, dbBuilder, getPlayerIdx());

        MainLineNodeObject nodeCfg = MainLineNode.getById(actualNode);
        if (nodeCfg == null) {
            LogUtil.error("EnterBattleHandler.getFightMakeId, nodeId [" + actualNode + "] cfg is null");
            return RetCodeEnum.RCE_MainLine_NodeCfgIsNull;
        }

        //非战斗型节点不能进入战斗
        if (nodeCfg.getNodetype() != 1 && nodeCfg.getNodetype() != 2) {
            LogUtil.error("EnterBattleHandler.getFightMakeId, nodeId [" + actualNode + "] can not fight");
            return RetCodeEnum.RCE_MainLine_CurNodeCanNotBattle;
        }

        ExtendProperty.Builder extendBuilder = null;

        MainLineCheckPointObject beforePoint = MainLineCheckPoint.getById(checkPointCfg.getBeforecheckpoint());
        //如果是密码关卡（密码与关卡分开填的）
        if (beforePoint != null && beforePoint.getType() == 1) {
            List<Integer> pswRecord = entity.getPswRecord(beforePoint.getId());
            //检查密码类型是否已经输入完毕,
            if (!MainLineUtil.isInputPswFinished(pswRecord, beforePoint.getNodelist())) {
                return RetCodeEnum.RCE_MainLine_PswNotInputFinish;
            }

            if (MainLineUtil.pswIsRight(beforePoint.getId(), pswRecord)) {
                extendBuilder = BattleUtil.builderMonsterExtendProperty(2, nodeCfg.getWeaken());
            } else {
                extendBuilder = BattleUtil.builderMonsterExtendProperty(2, nodeCfg.getEnhance());
            }
            //破阵型
        } else if (checkPointCfg.getType() == 2) {
            if (nodeCfg.getNodetype() == 2) {
                //改为击杀了小怪削弱百分比
                extendBuilder = BattleUtil.builderMonsterExtendProperty(2, nodeCfg.getEnhance(),
                        MainLineUtil.calculateEnhanceFactor(curCheckPoint, mainLineProBuilder.getProgressList()));
            } else if (!MainLineUtil.killMonsterByOrder(nodeId, mainLineProBuilder.getProgressList(), checkPointCfg.getCorrectorder())) {
                //没按照顺序击杀小怪则按照配置表增强
                extendBuilder = BattleUtil.builderMonsterExtendProperty(2, nodeCfg.getEnhance());
            }
        } else if (checkPointCfg.getType() == 3) {
            if (!MainLineUtil.nodeCanReach(mainLineProBuilder.getProgressList(), checkPointCfg.getNodelist(), nodeId)) {
                return RetCodeEnum.RCE_MainLine_NodeCanNotReach;
            }
        } else if (checkPointCfg.getType() == 4) {
            if (!MainLineUtil.nodeCanReach(mainLineProBuilder.getUnlockNodesList(), nodeId, true)) {
                return RetCodeEnum.RCE_MainLine_NodeCanNotReach;
            }
        } else if (checkPointCfg.getType() == 5) {
            if (!mainLineProBuilder.getUnlockNodesList().contains(nodeId)) {
                return RetCodeEnum.RCE_MainLine_FightOrderError;
            }
        }

        if (extendBuilder != null) {
            addExtendProp(extendBuilder.build());
        }

        setFightMakeId(nodeCfg.getFightmakeid());
        return RetCodeEnum.RCE_Success;
    }

    private int tryReplaceNodeId(int nodeId, DB_MainLine.Builder dbBuilder, String playerIdx) {
        PlotAffectObject affect = PlotAffect.getById(nodeId);
        if (affect == null) {
            return nodeId;
        }
        boolean playerTriggerAffectPlot = dbBuilder.getPersonalPlotList().contains(affect.getPlotid());
        if (!playerTriggerAffectPlot) {
            return nodeId;
        }
        LogUtil.info("player:{} play mainline battle replace node Cfg,node:{} change to node:{}", playerIdx, nodeId, affect.getNewnodeid());
        return affect.getNewnodeid();
    }

    private int curKeyNodeCanBattleMaxKeyNode(int keyNodeId) {
        if (keyNodeId < 0) {
            return 0;
        }
        KeyNodeConfigObject cfg = KeyNodeConfig.getById(keyNodeId + 1);
        if (cfg == null) {
            return Integer.MAX_VALUE;
        }
        return cfg.getMainlinenodeid();
    }

    @Override
    protected void initSuccess() {
        LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.MainLine));
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Common;
    }

    @Override
    public void tailSettle(CS_BattleResult resultData, List<Reward> rewardList, SC_BattleResult.Builder resultBuilder) {
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(getPlayerIdx());
        if (entity == null) {
            LogUtil.error("playerIdx[" + getPlayerIdx() + "] mainLineEntity is null");
            return;
        }

        Event event = Event.valueOf(EventType.ET_MainLineBattleSettle, entity, entity); // 当前线程直接处理，保证顺序
        event.pushParam(getIntEnterParam("nodeId"), resultData.getWinnerCamp(),getTeamAbility());
        EventManager.getInstance().dispatchEvent(event);
    }

    public long getTeamAbility() {
        List<Battle.BattlePlayerInfo> playerBattleData = getPlayerBattleData();
        if (CollectionUtils.isEmpty(playerBattleData)) {
            return 0;
        }

        return playerBattleData.get(0).getPetListList().stream().mapToLong(Battle.BattlePetData::getAbility).sum();
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MainLineCheckPoint;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_MainLineCheckPoint;
    }

    @Override
    public int getPointId() {
        return getIntEnterParam("nodeId");
    }
}
