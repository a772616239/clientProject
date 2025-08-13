
package platform.purchase;


import common.GameConst;
import lombok.Getter;
import platform.entity.PurchaseHandler;
import protocol.MonthCard;

@PurchaseHandler
@Getter
public class AdvancedCardPurchaseHandler extends MonthCardPurchaseHandler {

    @Getter
    private static final AdvancedCardPurchaseHandler instance = new AdvancedCardPurchaseHandler();

    private int cardId = MonthCard.MonthTypeEnum.MTE_Advanced_VALUE;

    private GameConst.RechargeType rechargeType = GameConst.RechargeType.AdvancedMonthCard;

}
