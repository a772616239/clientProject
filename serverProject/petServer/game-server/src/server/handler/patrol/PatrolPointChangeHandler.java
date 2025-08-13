package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolMoveResult;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolPointChange;
import protocol.Patrol.SC_PatrolPointChange;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolPointChange_VALUE;

/**
 * 处理客户端移动位置请求
 *
 * @author xiao_FL
 * @date 2019/8/7
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolPointChange_VALUE)
public class PatrolPointChangeHandler extends AbstractBaseHandler<CS_PatrolPointChange> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolPointChange parse(byte[] bytes) throws Exception {
        return CS_PatrolPointChange.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolPointChange csPatrolPointChange, int i) {
//        LogUtil.info("recv patrolPointChange msg:" + csPatrolPointChange.toString());
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            // 查询巡逻队
            PatrolMoveResult returnResult = patrolService.move(playerId, csPatrolPointChange.getPosition());
            // 返回
            SC_PatrolPointChange.Builder result = SC_PatrolPointChange.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            if (returnResult.isSuccess()) {
                retCode.setRetCode(RetCodeEnum.RCE_Success);
                result.setPosition(returnResult.getLocation());
            } else {
                retCode.setRetCode(returnResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(SC_PatrolPointChange_VALUE, result);
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
        gsChn.send(MsgIdEnum.SC_PatrolPointChange_VALUE, SC_PatrolPointChange.newBuilder().setResult(retCode));
    }
}
