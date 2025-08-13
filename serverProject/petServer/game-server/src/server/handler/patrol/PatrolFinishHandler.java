package server.handler.patrol;

import common.AbstractBaseHandler;
import entity.CommonResult;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolFinish;
import protocol.Patrol.SC_PatrolFinish;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolFinish_VALUE;

/**
 * @author xiao_FL
 * @date 2019/8/7
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolFinish_VALUE)
public class PatrolFinishHandler extends AbstractBaseHandler<CS_PatrolFinish> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolFinish parse(byte[] bytes) throws Exception {
        return CS_PatrolFinish.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolFinish csPatrolFinish, int i) {
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            // 查询巡逻队
            CommonResult returnResult = patrolService.finish(playerId);
            // 返回
            SC_PatrolFinish.Builder result = SC_PatrolFinish.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            if (returnResult.isSuccess()) {
                retCode.setRetCode(RetCodeEnum.RCE_Success);
            } else {
                retCode.setRetCode(returnResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(SC_PatrolFinish_VALUE, result);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Patrol;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PatrolFinish_VALUE, SC_PatrolFinish.newBuilder().setResult(retCode));
    }
}
