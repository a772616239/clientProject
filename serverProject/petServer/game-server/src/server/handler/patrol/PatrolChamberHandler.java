package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolMoveResult;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolChamber;
import protocol.Patrol.SC_PatrolChamber;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @author xiao_FL
 * @date 2020/3/3
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolChamber_VALUE)
public class PatrolChamberHandler extends AbstractBaseHandler<CS_PatrolChamber> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolChamber parse(byte[] bytes) throws Exception {
        return CS_PatrolChamber.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolChamber csPatrolChamber, int i) {
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            PatrolMoveResult returnResult = patrolService.chooseChamber(playerId, csPatrolChamber.getLocation(), csPatrolChamber.getEvent());
            SC_PatrolChamber.Builder result = SC_PatrolChamber.newBuilder();
            RetCodeId.RetCode.Builder retCode = RetCodeId.RetCode.newBuilder();
            if (returnResult.isSuccess()) {
                retCode.setRetCode(RetCodeId.RetCodeEnum.RCE_Success);
                result.setLocation(returnResult.getLocation());
            } else {
                retCode.setRetCode(returnResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(MsgIdEnum.SC_PatrolChamber_VALUE, result);
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
        gsChn.send(MsgIdEnum.SC_PatrolChamber_VALUE, SC_PatrolChamber.newBuilder().setResult(retCode));
    }
}
