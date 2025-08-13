
package platform.purchase;


import common.GameConst;
import lombok.Getter;
import platform.entity.PurchaseHandler;
import protocol.MonthCard;

@PurchaseHandler
@Getter
public class BaseMonthCardPurchaseHandler extends MonthCardPurchaseHandler {
    @Getter
    private static final MonthCardPurchaseHandler instance = new BaseMonthCardPurchaseHandler();
    private GameConst.RechargeType rechargeType = GameConst.RechargeType.BaseMonthCard;
    private int cardId = MonthCard.MonthTypeEnum.MTE_Normal_VALUE;

}
