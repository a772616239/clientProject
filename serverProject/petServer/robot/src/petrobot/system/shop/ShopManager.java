package petrobot.system.shop;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import protocol.MessageId.MsgIdEnum;
import protocol.Shop.*;
import protocol.Shop.CS_ClaimShopInfo.Builder;

import java.util.Random;

@Controller
public class ShopManager {
    @Index(value = IndexConst.CLAIM_SHOP_INFO)
    public void settleOnHookReward(Robot robot) {
        if (robot == null) {
            return;
        }

        Builder builder = CS_ClaimShopInfo.newBuilder();
        //shopType 1 - 4
        builder.setShopTypeValue(new Random().nextInt(5));
        robot.getClient().send(MsgIdEnum.CS_ClaimShopInfo_VALUE, builder);
    }

    @Index(value = IndexConst.REFRESH_SHOP)
    public void refreshShop(Robot robot) {
        if (robot == null) {
            return;
        }

        CS_RefreshShop.Builder builder = CS_RefreshShop.newBuilder();
        //shopType 1 - 4
        builder.setShopTypeValue(2);
        robot.getClient().send(MsgIdEnum.CS_RefreshShop_VALUE, builder);
    }

    @Index(value = IndexConst.BUY_GOODS)
    public void bugGoods(Robot robot) {
        if (robot == null) {
            return;
        }

        CS_BuyGoods.Builder builder = CS_BuyGoods.newBuilder();
        Random random = new Random();
        int shopType = random.nextInt(8) + 1;
        builder.setShopTypeValue(shopType);
        ShopInfo shopInfoByType = robot.getData().getShopInfoByType(shopType);
        int goodsId = 0;
        if (shopInfoByType != null && shopInfoByType.getGoodsInfosCount() > 0) {
            GoodsInfo goodsInfos = shopInfoByType.getGoodsInfos(random.nextInt(shopInfoByType.getGoodsInfosCount()));
            if (goodsInfos != null) {
                goodsId = goodsInfos.getGoodsCfgId();
            }
        }
        builder.setBuyGoodsId(goodsId);
        builder.setBuyCount(random.nextInt(10));
        robot.getClient().send(MsgIdEnum.CS_BuyGoods_VALUE, builder);
    }
}
