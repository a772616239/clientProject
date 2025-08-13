package platform.logs.entity;

import cfg.ShopSell;
import cfg.ShopSellObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.consume.ConsumeUtil;
import model.reward.RewardUtil;
import platform.logs.LogClass.ConsumeLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.StatisticsLogUtil;
import platform.logs.AbstractPlayerLog;
import protocol.Shop.ShopTypeEnum;

@Getter
@Setter
@NoArgsConstructor
public class ShopLog extends AbstractPlayerLog {
    private RewardLog buy;
    private ConsumeLog consume;
    private String shop;

    public ShopLog(String playerIdx, int buyGoodsId, int buyCount, ShopTypeEnum shopType) {
        super(playerIdx);
        ShopSellObject shopCfg = ShopSell.getById(buyGoodsId);
        if (shopCfg != null) {
            this.buy = new RewardLog(RewardUtil.parseAndMulti(shopCfg.getCargo(), buyCount));
            this.consume = new ConsumeLog(ConsumeUtil.parseAndMulti(shopCfg.getPrice(), buyCount));
        }
        this.shop = StatisticsLogUtil.getShopName(shopType);
    }
}
