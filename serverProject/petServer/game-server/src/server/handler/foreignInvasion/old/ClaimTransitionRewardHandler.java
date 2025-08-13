//package server.handler.foreignInvasion;
//
//import cfg.ForeignInvasionParamConfig;
//import common.GameConst;
//import hyzNet.GameServerTcpChannel;
//import common.AbstractBaseHandler;
//import hyzNet.message.MsgId;
//import model.foreigninvasion.oldVersion.ForeignInvasionManager;
//import model.reward.RewardManager;
//import model.reward.RewardUtil;
//import platform.logs.ReasonManager;
//import protocol.Common.Reward;
//import protocol.Common.RewardSourceEnum;
//import protocol.Gameplay.CS_ClaimTransitionReward;
//import protocol.Gameplay.ForeignInvasionStatusEnum;
//import protocol.Gameplay.SC_ClaimTransitionReward;
//import protocol.MessageId.MsgIdEnum;
//import protocol.RetCodeId.RetCodeEnum;
//import util.GameUtil;
//
//@MsgId(msgId = MsgIdEnum.CS_ClaimTransitionReward_VALUE)
//public class ClaimTransitionRewardHandler extends AbstractBaseHandler<CS_ClaimTransitionReward> {
//    @Override
//    protected CS_ClaimTransitionReward parse(byte[] bytes) throws Exception {
//        return CS_ClaimTransitionReward.parseFrom(bytes);
//    }
//
//    @Override
//    protected void execute(GameServerTcpChannel gsChn, CS_ClaimTransitionReward req, int i) {
//        String playerIdx = String.valueOf(gsChn.getPlayerId1());
//
//        SC_ClaimTransitionReward.Builder builder = SC_ClaimTransitionReward.newBuilder();
//        if(ForeignInvasionManager.getInstance().getStatus() != ForeignInvasionStatusEnum.FISE_Transition){
//            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ForInv_StatusMismatching));
//            gsChn.send(MsgIdEnum.SC_ClaimTransitionReward_VALUE, builder);
//            return;
//        }
//
//        int claimCount = ForeignInvasionManager.getInstance().getPlayerTransitionRewardCount(playerIdx);
//        if(claimCount > ForeignInvasionParamConfig.getById(GameConst.CONFIG_ID).getMaxgainrewardcount()){
//            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ForInv_ClaimCountLimit));
//            gsChn.send(MsgIdEnum.SC_ClaimTransitionReward_VALUE, builder);
//            return;
//        }
//
//        int index = req.getIndex();
//        int[][] transitionPool = ForeignInvasionParamConfig.getById(GameConst.CONFIG_ID).getTransitionrandompool();
//        if (index < 0 || transitionPool.length <= index) {
//            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
//            gsChn.send(MsgIdEnum.SC_ClaimTransitionReward_VALUE, builder);
//            return;
//        }
//
//        Reward reward = RewardUtil.parseReward(transitionPool[index]);
//        if (reward != null) {
//            RewardManager.getInstance().doReward(playerIdx, reward,
//                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ForeignInvasion, "随机"), false);
//            //增加玩家已经领取的奖励次数
//            ForeignInvasionManager.getInstance().increaseClaimRewardCount(playerIdx);
//            builder.addRewards(reward);
//            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
//        } else {
//            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
//        }
//        gsChn.send(MsgIdEnum.SC_ClaimTransitionReward_VALUE, builder);
//    }
//}
