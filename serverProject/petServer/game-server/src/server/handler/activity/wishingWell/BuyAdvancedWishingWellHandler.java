//package server.handler.activity.wishingWell;
//
//import cfg.GameConfig;
//import common.AbstractBaseHandler;
//import common.GameConst;
//import common.SyncExecuteFunction;
//import hyzNet.GameServerTcpChannel;
//import hyzNet.message.MsgId;
//import model.consume.ConsumeManager;
//import model.consume.ConsumeUtil;
//import model.targetsystem.dbCache.targetsystemCache;
//import model.targetsystem.entity.targetsystemEntity;
//import platform.logs.ReasonManager;
//import protocol.Activity;
//import protocol.Common;
//import protocol.MessageId.MsgIdEnum;
//import protocol.RetCodeId.RetCodeEnum;
//import util.GameUtil;
//
///**
// * 购买高级许愿池
// */
//@MsgId(msgId = MsgIdEnum.CS_BuyAdvancedWishingWell_VALUE)
//public class BuyAdvancedWishingWellHandler extends AbstractBaseHandler<Activity.CS_BuyAdvancedWishingWell> {
//    @Override
//    protected Activity.CS_BuyAdvancedWishingWell parse(byte[] bytes) throws Exception {
//        return Activity.CS_BuyAdvancedWishingWell.parseFrom(bytes);
//    }
//
//    @Override
//    protected void execute(GameServerTcpChannel gsChn, Activity.CS_BuyAdvancedWishingWell req, int i) {
//        String playerIdx = String.valueOf(gsChn.getPlayerId1());
//        Activity.SC_BuyAdvancedWishingWell.Builder result = Activity.SC_BuyAdvancedWishingWell.newBuilder();
//        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
//        if (target == null) {
//            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
//            gsChn.send(MsgIdEnum.SC_BuyAdvancedWishingWell_VALUE, result);
//            return;
//        }
//        if (target.getDb_Builder().getSpecialInfo().getWishingWell().getWishingWellType() == 1) {
//            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
//            gsChn.send(MsgIdEnum.SC_BuyAdvancedWishingWell_VALUE, result);
//            return;
//        }
//
//        Common.Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getAdvancedwishingwellprice());
//        //购买消耗
//        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FeatsReward))) {
//            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
//            gsChn.send(MsgIdEnum.SC_BuyAdvancedWishingWell_VALUE, result);
//            return;
//        }
//        SyncExecuteFunction.executeConsumer(target, entity -> target.getDb_Builder().getSpecialInfoBuilder().getWishingWellBuilder().setWishingWellType(1));
//        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
//        gsChn.send(MsgIdEnum.SC_BuyAdvancedWishingWell_VALUE, result);
//    }
//
//}
