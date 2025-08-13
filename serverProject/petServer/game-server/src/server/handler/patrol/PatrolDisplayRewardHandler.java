package server.handler.patrol;

import common.AbstractBaseHandler;
import entity.RewardResult;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.patrol.dbCache.service.IPatrolService;
import model.patrol.dbCache.service.PatrolServiceImpl;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolDisplayReward;
import protocol.Patrol.SC_PatrolDisplayReward;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @author xiao_FL
 * @date 2019/9/10
 */
@MsgId(msgId = MsgIdEnum.CS_PatrolDisplayReward_VALUE)
public class PatrolDisplayRewardHandler extends AbstractBaseHandler<CS_PatrolDisplayReward> {
    private IPatrolService patrolService = PatrolServiceImpl.getInstance();

    @Override
    protected CS_PatrolDisplayReward parse(byte[] bytes) throws Exception {
        return CS_PatrolDisplayReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PatrolDisplayReward csPatrolDisplayReward, int i) {
        LogUtil.info("recv patrolDisplayReward msg:" + csPatrolDisplayReward.toString());
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            RewardResult rewardResult = patrolService.displayReward(playerId);
            SC_PatrolDisplayReward.Builder result = SC_PatrolDisplayReward.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            if (rewardResult.isSuccess()) {
                retCode.setRetCode(RetCodeEnum.RCE_Success);
                result.addAllReward(rewardResult.getRewardList());
            } else {
                retCode.setRetCode(rewardResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(MsgIdEnum.SC_PatrolDisplayReward_VALUE, result);
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
        gsChn.send(MsgIdEnum.SC_PatrolDisplayReward_VALUE, SC_PatrolDisplayReward.newBuilder().setResult(retCode));
    }
}
