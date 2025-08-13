/*
package server.handler.pet.colleciont;

import cfg.Collectingrewards;
import cfg.CollectingrewardsObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetCollectionReward;
import protocol.PetMessage.SC_PetCollectionReward;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import java.util.List;
import util.LogUtil;

*/
/**
 * 处理客户端请求图鉴奖励
 *
 * @author xiao_FL
 * @date 2019/9/6
 *//*

@MsgId(msgId = MsgIdEnum.CS_PetCollectionReward_VALUE)
public class PetCollectionRewardHandler extends AbstractBaseHandler<CS_PetCollectionReward> {

    @Override
    protected CS_PetCollectionReward parse(byte[] bytes) throws Exception {
        return CS_PetCollectionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetCollectionReward req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        LogUtil.info("receive player:{} claim pet collection reward,req", playerId, req);
        SC_PetCollectionReward.Builder resultBuilder = SC_PetCollectionReward.newBuilder();
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetCollectionReward_VALUE, resultBuilder);
            return;
        }
        CollectingrewardsObject cfg = Collectingrewards.getById(req.getRewardId());
        if (cfg == null || (req.getRewardId() != entity.getPetCollectionBuilder().getRewardId()
                && entity.getPetCollectionBuilder().getCfgIdList().size() < cfg.getCount())) {

            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_PetCollectionReward_VALUE, resultBuilder);
            return;
        }
        List<Reward> rewardList = RewardUtil.parseRewardIntArrayToRewardList(cfg.getAwards());
        RewardManager.getInstance().doRewardByList(playerId, rewardList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetCollection), true);

        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> cacheTemp.getPetCollectionBuilder().setRewardId(req.getRewardId() + 1));
        LogUtil.info(" player:{} claim pet collection reward success,req", playerId, req);
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_PetCollectionReward_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetCollect;
    }



    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetCollectionReward_VALUE, SC_PetCollectionReward.newBuilder().setResult(retCode));
    }


}
*/
