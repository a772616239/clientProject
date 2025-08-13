package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolMoveResult;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolTreasure;
import protocol.Patrol.SC_PatrolTreasure;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolTreasure_VALUE;

/**
 * 处理客户端打开宝箱请求
 *
 * @author xiao_FL
 * @date 2019/8/7
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolTreasure_VALUE)
public class PatrolTreasureHandler extends AbstractBaseHandler<CS_PatrolTreasure> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolTreasure parse(byte[] bytes) throws Exception {
        return CS_PatrolTreasure.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolTreasure req, int i) {
        LogUtil.info("recv patrolTreasure msg:" + req.toString());
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            // 查询巡逻队
            PatrolMoveResult returnResult = patrolService.treasure(playerId, req.getLocation(), req.getBuy());
            // 返回
            SC_PatrolTreasure.Builder result = SC_PatrolTreasure.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            if (returnResult.isSuccess()) {
                retCode.setRetCode(RetCodeEnum.RCE_Success);
                result.setLocation(returnResult.getLocation());
            } else {
                retCode.setRetCode(returnResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(SC_PatrolTreasure_VALUE, result);
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
        gsChn.send(MsgIdEnum.SC_PatrolTreasure_VALUE, SC_PatrolTreasure.newBuilder().setResult(retCode));
    }
}
