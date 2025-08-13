package platform.purchase;

import cfg.RechargeProductObject;
import common.GameConst;
import common.SyncExecuteFunction;
import lombok.Getter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.entity.PurchaseData;
import platform.entity.PurchaseHandler;
import protocol.Common;
import protocol.TargetSystemDB;
import protocol.TargetSystemDB.DB_Feats;
import protocol.TargetSystemDB.DB_Feats.Builder;
import util.LogUtil;

@PurchaseHandler
public class FeatsPurchaseHandler extends BasePurchaseHandler {

	@Getter
	private GameConst.RechargeType rechargeType = GameConst.RechargeType.AdvancedFeats;

	@Getter
	private static final FeatsPurchaseHandler instance = new FeatsPurchaseHandler();

	private static final Common.Reward exShowReward = Common.Reward.newBuilder().setRewardType(Common.RewardTypeEnum.RTE_AdvancedFeats).setCount(1).build();

	@Override
	public boolean settlePurchase(PurchaseData data) {
		String playerIdx = data.getPlayerIdx();
		targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
		playerEntity player = playerCache.getByIdx(playerIdx);
		if (target == null || player == null) {
			LogUtil.error("FeatsPurchaseHandler playerEntity or targetSystemEntity is null by playerIdx", playerIdx);
			return false;
		}
		RechargeProductObject rechargeProduct = data.getRechargeProduct();

		TargetSystemDB.DB_Feats featsInfo = target.getDb_Builder().getFeatsInfosMap().get(rechargeProduct.getSubtype());
		if (featsInfo == null) {
			LogUtil.error(" FeatsPurchaseHandler player:{} errer type,type:{}", playerIdx,rechargeProduct.getSubtype());
			return false;
		}
		// 高级功勋未过期
		if (hasActiveAdvancedFeats(featsInfo)) {
			LogUtil.error(" FeatsPurchaseHandler player:{} advancedFeats not expire,expireTime:{}", playerIdx, featsInfo.getResetTime());
			return false;

		}
		SyncExecuteFunction.executeConsumer(target, entity -> {
			DB_Feats db_Feats = target.getDb_Builder().getFeatsInfosMap().get(rechargeProduct.getSubtype());
			Builder builder = db_Feats.toBuilder().setFeatsType(1);
			target.getDb_Builder().putFeatsInfos(rechargeProduct.getSubtype(), builder.build());
		});

		LogUtil.info("FeatsPurchaseHandler player:{}  success active advanced feats", playerIdx);
		PurchaseManager.getInstance().doPurchaseReward(playerIdx, data, null, exShowReward);
		target.sendFeats();
		return true;
	}

	private boolean hasActiveAdvancedFeats(TargetSystemDB.DB_Feats featsInfo) {
		return featsInfo.getFeatsType() == 1;
	}

}
