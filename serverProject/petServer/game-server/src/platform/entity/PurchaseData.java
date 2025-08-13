package platform.entity;

import cfg.RechargeProductObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import platform.logs.ReasonManager;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PurchaseData {
    private String orderNo;
    private BigDecimal payPrice;
    private String productCode;
    private RechargeProductObject rechargeProduct;
    private String playerIdx;
    private ReasonManager.Reason reason;

    public PurchaseData(String userId, RechargeProductObject rechargeProduct, ReasonManager.Reason reason) {
        this.playerIdx = userId;
        this.rechargeProduct = rechargeProduct;
        this.reason = reason;
    }


}
