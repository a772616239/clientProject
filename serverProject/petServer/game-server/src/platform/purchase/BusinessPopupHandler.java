package platform.purchase;

import cfg.PopupMission;
import cfg.PopupMissionObject;
import cfg.RechargeProductObject;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.entity.PurchaseData;
import platform.entity.PurchaseHandler;
import protocol.Common;
import protocol.TargetSystem;
import util.LogUtil;

@PurchaseHandler
public class BusinessPopupHandler extends BasePurchaseHandler {

    @Getter
    private GameConst.RechargeType rechargeType = GameConst.RechargeType.BusinessPopup;
    @Getter
    private static final BusinessPopupHandler instance = new BusinessPopupHandler();

    @Override
    public boolean settlePurchase(PurchaseData data) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(data.getPlayerIdx());
        if (target == null) {
            LogUtil.error("player:{} BusinessPopupHandler settlePurchase failed  targetsystemEntity is null", data.getPlayerIdx());
            return false;
        }
        RechargeProductObject product = data.getRechargeProduct();
        PopupMissionObject mission = PopupMission.getInstance().getMissionBuyProductId(product.getId());
        if (mission == null) {
            LogUtil.error("player:{},BusinessPopupHandler settlePurchase error case by mission is null,purchaseData:{}", data.getPlayerIdx(), data);
            return false;
        }
        Stream<TargetSystem.BusinessPopupItem> itemStream = target.getDb_Builder().getDbBusinessPopup().getBusinessItemsList()
                .stream().filter(e -> e.getCfgId() == mission.getId());
        if (!itemStream.findAny().isPresent()) {
            LogUtil.error("player:{},have not chance to buy business popup item", data.getPlayerIdx());
            return false;
        }

        List<Common.Reward> exReward = RewardUtil.parseRewardIntArrayToRewardList(mission.getReward());
        PurchaseManager.getInstance().doPurchaseReward(data.getPlayerIdx(), data, exReward, null);

        settleBusinessPopupPurchase(data.getPlayerIdx(), mission);
        return true;
    }

    private void settleBusinessPopupPurchase(String playerIdx, PopupMissionObject mission) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            LogUtil.error("player:{} BusinessPopupHandler settleBusinessPopupPurchase save limit buy count record failed  target is null", playerIdx);
            return;
        }
        SyncExecuteFunction.executeConsumer(target, e -> {
            TargetSystem.BusinessPopupItem.Builder builder = target.getOneBusinessPopup(mission.getId());
            TargetSystem.BusinessPopupItem item = builder.setAlreadyBuyTime(builder.getAlreadyBuyTime() + 1).build();
            if (item.getAlreadyBuyTime() >=mission.getLimitbuy()) {
                removeCantBuyItem(target, builder);
            }
            target.sendBusinessPopupUpdate(item);
            target.doTargetPro(TargetSystem.TargetTypeEnum.TTE_PopMission_BuyGift,1, mission.getId());
        });
    }

    private void removeCantBuyItem(targetsystemEntity target, TargetSystem.BusinessPopupItem.Builder builder) {
        int index = findRemoveItemIndex(target.getDb_Builder().getDbBusinessPopup().getBusinessItemsList(), builder.getPopupId());
        if (index!=-1) {
            target.getDb_Builder().getDbBusinessPopupBuilder().removeBusinessItems(index);
        }
    }

    private int findRemoveItemIndex(List<TargetSystem.BusinessPopupItem> businessItemsList, long popupId) {
        for (int i = 0; i < businessItemsList.size(); i++) {
            if (businessItemsList.get(i).getPopupId() == popupId) {
                return i;
            }
        }
        return -1;
    }

}
