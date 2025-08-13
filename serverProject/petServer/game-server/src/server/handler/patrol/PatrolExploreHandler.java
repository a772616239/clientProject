package server.handler.patrol;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.patrol.entity.PatrolExploreResult;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolExplore;
import protocol.Patrol.SC_PatrolExplore;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolExplore_VALUE;

/**
 * 处理客户端探索请求
 *
 * @author xiao_FL
 * @date 2019/8/7
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolExplore_VALUE)
public class PatrolExploreHandler extends AbstractBaseHandler<CS_PatrolExplore> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolExplore parse(byte[] bytes) throws Exception {
        return CS_PatrolExplore.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolExplore csPatrolExplore, int i) {
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            // 查询巡逻队
            PatrolExploreResult returnResult = patrolService.explore(playerId, csPatrolExplore.getEvent(), csPatrolExplore.getLocation());
            // 返回
            SC_PatrolExplore.Builder result = SC_PatrolExplore.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            if (returnResult.isSuccess()) {
                retCode.setRetCode(RetCodeEnum.RCE_Success);
                result.setStatus(returnResult.getStatus());
                result.addAllStatusCollection(returnResult.getAllStatus());
                result.setGreed(returnResult.getGreed());
                result.setLocation(returnResult.getLocation());
            } else {
                retCode.setRetCode(returnResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(SC_PatrolExplore_VALUE, result);
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
        gsChn.send(MsgIdEnum.SC_PatrolExplore_VALUE, SC_PatrolExplore.newBuilder().setResult(retCode));
    }
}
