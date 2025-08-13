package platform.purchase;

import cfg.RechargeProduct;
import cfg.RechargeProductObject;
import common.GameConst;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import platform.entity.PurchaseData;

public abstract class BasePurchaseHandler implements PurchaseInterface {

    @Getter
    private GameConst.RechargeType rechargeType;

    @Override
    public void init() {

    }

    @Override
    public boolean settlePurchase(PurchaseData data) {
        return false;
    }

    @Override
    public boolean containsProduct(PurchaseData data) {
        RechargeProductObject product = getProductByPurchaseData(data);
        if (product == null) {
            return false;
        }
        return product.getProducttype() == getRechargeType().getCode();
    }

    private RechargeProductObject getProductByPurchaseData(PurchaseData data) {
        RechargeProductObject product = data.getRechargeProduct();
        if (product == null) {
            product = findProductByCode(data.getProductCode());
        }
        return product;
    }

    private RechargeProductObject findProductByCode(String productCode) {
        if (StringUtils.isEmpty(productCode)) {
            return null;
        }
     return RechargeProduct.finalRechargeProduct(productCode);
    }
}
