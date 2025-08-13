package server.handler.patrol;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.patrolCache;
import model.patrol.entity.PatrolTree;
import model.patrol.entity.patrolEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolTrigger;
import protocol.Patrol.PatrolPoint;
import protocol.Patrol.PatrolStatus;
import protocol.Patrol.SC_PatrolTrigger;
import protocol.Patrol.SC_PatrolTrigger.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;
import util.PatrolUtil;

import static protocol.MessageId.MsgIdEnum.SC_PatrolTrigger_VALUE;

/**
 * 处理客户端移动位置请求
 *
 * @author xiao_FL
 * @date 2019/8/7
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolTrigger_VALUE)
public class PatrolTriggerEventHandler extends AbstractBaseHandler<CS_PatrolTrigger> {

    @Override
    protected CS_PatrolTrigger parse(byte[] bytes) throws Exception {
        return CS_PatrolTrigger.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolTrigger req, int i) {
        LogUtil.info("recv patrolTrigger msg:" + req.toString());
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        Builder result = SC_PatrolTrigger.newBuilder();
        patrolEntity cache = patrolCache.getInstance().getCacheByPlayer(playerId);
        if (cache == null) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gameServerTcpChannel.send(SC_PatrolTrigger_VALUE, result);
            return;
        }
        PatrolPoint forward = req.getLocation();

        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            // 查询目标点
            PatrolTree goal = PatrolUtil.preOrderByLocation(cache.getPatrolTree(), new PatrolTree(forward.getX(), forward.getY()));
            if (cache.gameFailed()) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Patrol_FailureError));
                gameServerTcpChannel.send(SC_PatrolTrigger_VALUE, result);
                return;
            }

            if (goal == null || !goal.ifReachable()) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gameServerTcpChannel.send(SC_PatrolTrigger_VALUE, result);
                return;
            }

            PatrolStatus.Builder builder = cache.getPatrolStatusEntity().toBuilder();
            goal.setExplored(1);
            if (PatrolTree.TRAVELING_SALESMAN == goal.getPointType()) {
                builder.getSaleManBuilder().setOpen(true);
            } else if (PatrolTree.BOSS_KEY == goal.getPointType()) {
                builder.setObtainBossKey(true);
            }
            builder.setLocation(cache.getPatrolStatusEntity().getLocation()
                    .toBuilder().clear().setExplored(1).setX(goal.getX()).setY(goal.getY())
                    .build());

            cache.setPatrolStatusEntity(builder.build());
            patrolCache.getInstance().flush(cache);
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gameServerTcpChannel.send(SC_PatrolTrigger_VALUE, result);
        });
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Patrol;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PatrolTrigger_VALUE, SC_PatrolTrigger.newBuilder().setResult(retCode));
    }
}
