package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolMissionSwitch;
import protocol.Patrol.SC_PatrolMissionSwitch;
import protocol.Patrol.SC_PatrolMissionSwitch.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolMissionSwitch_VALUE;

/**
 * 处理客户端移动位置请求
 *
 * @author xiao_FL
 * @date 2019/8/7
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolMissionSwitch_VALUE)
public class PatrolMissionSwitchHandler extends AbstractBaseHandler<CS_PatrolMissionSwitch> {

    @Override
    protected CS_PatrolMissionSwitch parse(byte[] bytes) throws Exception {
        return CS_PatrolMissionSwitch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolMissionSwitch req, int i) {
        LogUtil.debug("recv PatrolMissionSwitch msg:" + req.toString());
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        Builder result = SC_PatrolMissionSwitch.newBuilder();

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (target == null) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gameServerTcpChannel.send(SC_PatrolMissionSwitch_VALUE, result);
            return;
        }
        long endTime = target.getDb_Builder().getPatrolMission().getEndTime();
        if (endTime == 0) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gameServerTcpChannel.send(SC_PatrolMissionSwitch_VALUE, result);
            return;
        }

        if (Common.MissionStatusEnum.MSE_Finished == target.getDb_Builder().getPatrolMission().getMission().getStatus()) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Patrol_MissionEnd));
            gameServerTcpChannel.send(SC_PatrolMissionSwitch_VALUE, result);
            return;
        }
        EventUtil.triggerPausePatrolMission(playerId, req.getPause());
        result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gameServerTcpChannel.send(SC_PatrolMissionSwitch_VALUE, result);
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Patrol;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PatrolMissionSwitch_VALUE, SC_PatrolMissionSwitch.newBuilder().setResult(retCode));
    }
}
