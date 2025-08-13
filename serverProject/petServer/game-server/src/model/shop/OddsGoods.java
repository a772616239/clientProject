package model.shop;

import cfg.ShopSellObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author huhan
 * @date 2020/04/08
 *
 * 保存goods列表和列表的总概率
 */
public class OddsGoods {
    private List<ShopSellObject> goodsList = new ArrayList<>();
    private int totalOdds;

    public OddsGoods() {}

    public void addGoods(ShopSellObject goods) {
        if (null == goods || this.goodsList.contains(goods)) {
            return;
        }
        this.goodsList.add(goods);
        this.totalOdds += goods.getAppearrate();
    }

    public void addAllGoods(List<ShopSellObject> goods) {
        if(CollectionUtils.isEmpty(goodsList)) {
            return;
        }

        for (ShopSellObject good : goods) {
            addGoods(good);
        }
    }

    public List<ShopSellObject> getAll() {
        return Collections.unmodifiableList(goodsList);
    }

    /**
     * 不重复获取
     * @param count
     * @return
     */
    public Set<ShopSellObject> randomGet(int count) {
        if (count <= 0) {
            return null;
        }
        Set<ShopSellObject> result = new HashSet<>();
        if (this.goodsList.size() <= count) {
            result.addAll(this.goodsList);
            return result;
        }
        Random random = new Random();
        if (totalOdds <= 0) {
            List<ShopSellObject> tempList = new LinkedList<>(this.goodsList);
            for (int i = 0; i < count; i++) {
                int randomIndex = random.nextInt(tempList.size());
                result.add(tempList.get(randomIndex));
                tempList.remove(randomIndex);
            }
        } else {
            for (int i = 0; i < count * 5; i++) {
                int randomNum = random.nextInt(this.totalOdds);
                int curNum = 0;
                for (ShopSellObject shopSellObject : this.goodsList) {
                    if ((curNum += shopSellObject.getAppearrate()) > randomNum) {
                        result.add(shopSellObject);
                        break;
                    }
                }

                if (result.size() >= count) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * 不重复随机
     * @param goodsList
     * @param count
     * @return
     */
    public static List<ShopSellObject> randomGoods(List<ShopSellObject> goodsList, int count) {
        if (CollectionUtils.isEmpty(goodsList) || goodsList.size() <= count) {
            return goodsList;
        }

        OddsGoods oddsGoods = new OddsGoods();
        oddsGoods.addAllGoods(goodsList);
        Set<ShopSellObject> randomResult = oddsGoods.randomGet(count);
        if (randomResult == null) {
            return null;
        }
        return new ArrayList<>(randomResult);
    }

}
