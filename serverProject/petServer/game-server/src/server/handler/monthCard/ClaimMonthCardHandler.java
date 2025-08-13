package server.handler.monthCard;

import cfg.MonthlyCardConfig;
import cfg.MonthlyCardConfigObject;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Collection;
import java.util.List;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.MessageId.MsgIdEnum;
import protocol.MonthCard;
import protocol.MonthCard.MonthCardInfo;
import protocol.MonthCard.MonthCardInfo.Builder;
import protocol.MonthCard.SC_ClaimMonthCard;
import protocol.PlayerDB;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimMonthCard_VALUE)
public class ClaimMonthCardHandler extends AbstractBaseHandler<MonthCard.CS_ClaimMonthCard> {

    @Override
    protected MonthCard.CS_ClaimMonthCard parse(byte[] bytes) throws Exception {
        return MonthCard.CS_ClaimMonthCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, MonthCard.CS_ClaimMonthCard req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        MonthCard.SC_ClaimMonthCard.Builder result = MonthCard.SC_ClaimMonthCard.newBuilder();

        if (player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimMonthCard_VALUE, result);
            return;
        }

        Collection<MonthlyCardConfigObject> values = MonthlyCardConfig._ix_id.values();
        List<PlayerDB.DB_MonthCardInfo> ownCardList = player.getDb_data().getRechargeCardsBuilder().getMonthCardListList();
        for (MonthlyCardConfigObject cardConfig : values) {
            PlayerDB.DB_MonthCardInfo ownCard = ownCardList.stream().filter(card -> card.getCarId() == cardConfig.getId())
                    .findAny().orElse(null);
            //月卡剩余天数
            int remainDays = ownCard == null ? 0 : ownCard.getRemainDays();
            MonthCard.MonthCardInfo.Builder cardInfo = getMonthCardInfoBuilder(cardConfig, remainDays);
            result.addMonthCardList(cardInfo.build());
        }
        result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimMonthCard_VALUE, result);

    }

    private MonthCard.MonthCardInfo.Builder getMonthCardInfoBuilder(MonthlyCardConfigObject config, int remainDays) {

        Builder result = MonthCardInfo.newBuilder();
        result.setMonthCardId(config.getId());
        List<Reward> everyDayRewards = RewardUtil.parseRewardIntArrayToRewardList(config.getEverydayrewards());
        if (CollectionUtils.isNotEmpty(everyDayRewards)) {
            result.addAllEverydayRewards(everyDayRewards);
        }
        List<Reward> instanceRewards = RewardUtil.parseRewardIntArrayToRewardList(config.getInstantrewards());
        if (CollectionUtils.isNotEmpty(instanceRewards)) {
            result.addAllInstantRewards(instanceRewards);
        }
        Consume consume = ConsumeUtil.parseConsume(config.getPrice());
        if (consume != null) {
            result.setPrice(consume);
        }

        return result.setSumRewards(config.getSumrewards()).setRemainDays(remainDays);
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MonthCard;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimMonthCard_VALUE, SC_ClaimMonthCard.newBuilder().setRetCode(retCode));
    }
}
