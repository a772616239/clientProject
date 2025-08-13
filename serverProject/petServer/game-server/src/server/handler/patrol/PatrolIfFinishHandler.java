package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolFinishResult;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolIfFinish;
import protocol.Patrol.SC_PatrolIfFinish;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * 处理客户端请求上次游戏是否已经结束
 *
 * @author xiao_FL
 * @date 2019/9/9
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolIfFinish_VALUE)
public class PatrolIfFinishHandler extends AbstractBaseHandler<CS_PatrolIfFinish> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolIfFinish parse(byte[] bytes) throws Exception {
        return CS_PatrolIfFinish.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolIfFinish csPatrolIfFinish, int i) {
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        try {
            PatrolFinishResult finishResult = patrolService.patrolStatus(playerId);
            SC_PatrolIfFinish.Builder result = SC_PatrolIfFinish.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            if (finishResult.isSuccess()) {
                retCode.setRetCode(RetCodeEnum.RCE_Success);
                result.setResult(retCode);
                result.setFinish(finishResult.getFinish());
                result.setTodayCreateCount(finishResult.getTodayCreateCount());
            } else {
                retCode.setRetCode(RetCodeEnum.RCE_ErrorParam);
                result.setResult(retCode);
            }
            gameServerTcpChannel.send(MsgIdEnum.SC_PatrolIfFinish_VALUE, result);
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
        gsChn.send(MsgIdEnum.SC_PatrolIfFinish_VALUE, SC_PatrolIfFinish.newBuilder().setResult(retCode));
    }
}
