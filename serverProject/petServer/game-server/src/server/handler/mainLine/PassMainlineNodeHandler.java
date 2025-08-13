package server.handler.mainLine;

import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import common.AbstractBaseHandler;
import common.GameConst.EventType;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MainLine.CS_PassMainlineNode;
import protocol.MainLine.MainLineProgress;
import protocol.MainLine.SC_PassMainlineNode;
import protocol.MainLineDB.DB_MainLine;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.ArrayUtil;
import util.GameUtil;

import java.util.List;

import static protocol.MessageId.MsgIdEnum.SC_PassMainlineNode_VALUE;

@MsgId(msgId = MsgIdEnum.CS_PassMainlineNode_VALUE)
public class PassMainlineNodeHandler extends AbstractBaseHandler<CS_PassMainlineNode> {
    @Override
    protected CS_PassMainlineNode parse(byte[] bytes) throws Exception {
        return CS_PassMainlineNode.parseFrom(bytes);
    }

    /**
     * 主线奖励节点类型
     */
    private static final int MainLineRewardNodeType = 5;

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PassMainlineNode req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_PassMainlineNode.Builder msg = SC_PassMainlineNode.newBuilder();

        RetCodeEnum codeEnum = passNode(playerIdx, req.getNodeId());
        msg.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(SC_PassMainlineNode_VALUE, msg);
    }

    public RetCodeEnum passNode(String playerIdx, int nodeId) {
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        DB_MainLine.Builder dbBuilder = entity.getDBBuilder();
        if (dbBuilder == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        MainLineProgress.Builder mainLineProBuilder = dbBuilder.getMainLineProBuilder();
        int curCheckPoint = mainLineProBuilder.getCurCheckPoint();

        MainLineCheckPointObject checkPointCfg = MainLineCheckPoint.getById(curCheckPoint);
        if (checkPointCfg == null) {
            return RetCodeEnum.RCE_MainLine_CheckPointCfgIsNull;
        }

        //该节点是否已经闯关过了
        if (mainLineProBuilder.getProgressList().contains(nodeId)) {
            return RetCodeEnum.RCE_MainLine_CurNodeIsPassed;
        }

        //关卡是否解锁
        if (checkPointCfg.getUnlocklv() > PlayerUtil.queryPlayerLv(entity.getLinkplayeridx())) {
            return RetCodeEnum.RCE_MainLine_CheckPointIsLock;
        }

        MainLineNodeObject nodeCfg = MainLineNode.getById(nodeId);
        if (nodeCfg == null) {
            return RetCodeEnum.RCE_MainLine_NodeCfgIsNull;
        }
        if (nodeCfg.getNodetype() != MainLineRewardNodeType) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        //该关卡是否包含该节点
        if (!ArrayUtil.intArrayContain(checkPointCfg.getNodelist(), nodeId)) {
            return RetCodeEnum.RCE_MainLine_NodeCanNotReach;
        }


        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(nodeCfg.getReward());

        RewardManager.getInstance().doRewardByList(entity.getLinkplayeridx(), rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MainLineKeyNode), true);

        Event event = Event.valueOf(EventType.ET_MainLineBattleSettle, entity, entity);
        event.pushParam(nodeId, 1, 0L);
        EventManager.getInstance().dispatchEvent(event);

        return RetCodeEnum.RCE_Success;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_PassMainlineNode_VALUE, SC_PassMainlineNode.newBuilder().setRetCode(retCode));
    }
}
