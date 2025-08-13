
package platform.purchase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cfg.ItemCard;
import cfg.ItemCardObject;
import common.GlobalData;
import common.SyncExecuteFunction;
import lombok.Getter;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.entity.PurchaseData;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.ItemCardData;
import protocol.Activity.SC_RefreshItemCard;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystemDB.DB_ItemCard;
import util.LogUtil;

@Getter
public abstract class ItemCardHandler extends BasePurchaseHandler {

	private static final Map<Integer, Reward> showRewardMap = new HashMap<>(2);

	@Override
	public void init() {
//		showRewardMap.put(getCardId(), Reward.newBuilder().setCount(1).setRewardType(Common.RewardTypeEnum.RTE_MonthCard).setId(getCardId()).build());
	}

	@Override
	public boolean settlePurchase(PurchaseData data) {

		String playerIdx = data.getPlayerIdx();
		if (data.getRechargeProduct() == null) {
			LogUtil.error("ItemCardHandler,data.getRechargeProduct() == null,playerIdx:{}", playerIdx);
			return false;
		}
		int id = data.getRechargeProduct().getSubtype();
		ItemCardObject itemCardConfig = ItemCard.getById(id);
		if (itemCardConfig == null) {
			LogUtil.error("ItemCardHandler,itemCardConfig == null,playerIdx:{}", playerIdx);
			return false;
		}

		targetsystemEntity tarsystem = targetsystemCache.getByIdx(playerIdx);
		if (tarsystem == null) {
			LogUtil.error("ItemCardHandler,targetsystemEntity == null,playerIdx:{}", playerIdx);
			return false;
		}
		Map<Integer, DB_ItemCard> itemCardMap = tarsystem.getDb_Builder().getItemCardMap();
		if (itemCardMap.containsKey(id)) {
			LogUtil.error("ItemCardHandler,repeated buy,playerIdx:{}", playerIdx);
			return false;
		}

		SyncExecuteFunction.executeConsumer(tarsystem, entity -> {
			DB_ItemCard.Builder builder = DB_ItemCard.newBuilder();
			builder.setHave(itemCardConfig.getLimitday());
			List<Reward> rwList = new ArrayList<>();
			Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ItemCard_day);
			rwList.addAll(RewardUtil.getRewardsByRewardId(itemCardConfig.getBuyreward()));
			RewardManager.getInstance().doRewardByList(playerIdx, rwList, reason, true);
			tarsystem.getDb_Builder().putItemCard(itemCardConfig.getId(), builder.build());
		});
		SC_RefreshItemCard.Builder builder = SC_RefreshItemCard.newBuilder();
		
		for (Entry<Integer, DB_ItemCard> ent : tarsystem.getDb_Builder().getItemCardMap().entrySet()) {
			ItemCardData.Builder b = ItemCardData.newBuilder();
//			b.setHave(ent.getValue().getHave());
			b.setIndex(ent.getKey());
			b.setToday(ent.getValue().getToday());
			builder.addItemCard(b);
		}
		GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefreshItemCard_VALUE, builder);
		return true;
	}
}