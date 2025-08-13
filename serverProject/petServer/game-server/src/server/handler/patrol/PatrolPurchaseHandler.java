package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolPurchaseResult;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolPurchase;
import protocol.Patrol.SC_PatrolPurchase;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * 处理客户端购买时光卷轴请求
 *
 * @author xiao_FL
 * @date 2019/8/27
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolPurchase_VALUE)
public class PatrolPurchaseHandler extends AbstractBaseHandler<CS_PatrolPurchase> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolPurchase parse(byte[] bytes) throws Exception {
        return CS_PatrolPurchase.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolPurchase csPatrolPurchase, int i) {
        LogUtil.info("recv patrolPurchase msg:" + csPatrolPurchase.toString());
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            // 购买卷轴
            PatrolPurchaseResult returnResult = patrolService.patrolPurchase(playerId);
            SC_PatrolPurchase.Builder result = SC_PatrolPurchase.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            if (returnResult.isSuccess()) {
                retCode.setRetCode(RetCodeEnum.RCE_Success);
            } else {
                retCode.setRetCode(returnResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(MsgIdEnum.SC_PatrolPurchase_VALUE, result);
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
        gsChn.send(MsgIdEnum.SC_PatrolPurchase_VALUE, SC_PatrolPurchase.newBuilder().setResult(retCode));
    }
}
