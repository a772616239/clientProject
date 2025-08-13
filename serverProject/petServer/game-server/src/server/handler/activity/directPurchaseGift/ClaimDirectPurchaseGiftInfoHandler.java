package server.handler.activity.directPurchaseGift;


import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Map;
import model.activity.ActivityManager;
import model.player.util.PlayerUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Server.ServerActivity;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.10.08
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimDirectPurchaseGiftInfo_VALUE)
public class ClaimDirectPurchaseGiftInfoHandler extends AbstractBaseHandler<Activity.CS_ClaimDirectPurchaseGiftInfo> {
    @Override
    protected Activity.CS_ClaimDirectPurchaseGiftInfo parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimDirectPurchaseGiftInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimDirectPurchaseGiftInfo req, int i) {
        Activity.SC_ClaimDirectPurchaseGiftInfo.Builder result = Activity.SC_ClaimDirectPurchaseGiftInfo.newBuilder();

        ServerActivity activity = ActivityManager.getInstance().findOneRecentActivityByType(ActivityTypeEnum.ATE_DirectPurchaseGift);
        if (activity == null) {
            gsChn.send(MsgIdEnum.SC_ClaimDirectPurchaseGiftInfo_VALUE, result);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            gsChn.send(MsgIdEnum.SC_ClaimDirectPurchaseGiftInfo_VALUE, result);
            return;
        }
        Common.LanguageEnum language = PlayerUtil.queryPlayerLanguage(playerIdx);
        Map<Long, Integer> buyRecordMap = entity.getDb_Builder().getSpecialInfo().getDirectPurchaseGiftBuyRecordMap();
        for (Activity.DirectPurchaseGift gift : activity.getDirectPurchaseGiftList()) {
            result.addDirectPurchaseGift(parsePlayerGift(language, buyRecordMap, gift));
        }
        gsChn.send(MsgIdEnum.SC_ClaimDirectPurchaseGiftInfo_VALUE, result);

    }

    private Activity.DirectPurchaseGift parsePlayerGift(Common.LanguageEnum language, Map<Long, Integer> buyRecordMap, Activity.DirectPurchaseGift gift) {
        Activity.DirectPurchaseGift.Builder builder = gift.toBuilder();
        builder.setGiftName(GameUtil.getLanguageStr(gift.getGiftName(), language));
        builder.setOriginalPrice(GameUtil.getLanguageStr(gift.getOriginalPrice(), language));
        builder.setNowPrice(GameUtil.getLanguageStr(gift.getNowPrice(), language));
        builder.setLimitBuy(queryGiftRemainTimes(buyRecordMap, gift.getGiftId(), gift.getLimitBuy()));
        builder.setRechargeProductId(gift.getRechargeProductId());
        return builder.build();
    }

    private int queryGiftRemainTimes(Map<Long, Integer> buyRecordMap, long giftId, int totalLimit) {
        Integer times = buyRecordMap.get(giftId);
        return times == null ? totalLimit : totalLimit - times;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
