package model.shop;


import cfg.ShopConfigObject;
import cfg.ShopSellObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import model.crossarena.CrossArenaManager;
import model.player.util.PlayerUtil;
import model.shop.ShopConst.SpecialType;
import model.shop.entity.PlayerShopInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Shop;
import protocol.Shop.GoodsInfo;
import util.*;

/**
 * @author huhan
 * @date 2020/04/03
 */

@Getter
@Setter
public class Store {
    /**
     * 商店配置
     */
    private ShopConfigObject shopCfg;

    /**
     * 手动随机列表
     */
    private final OddsGoods manualRandom = new OddsGoods();
    /**
     * 手动必出
     */
    private final OddsGoods manualMust = new OddsGoods();
    /**
     * 自动随机列表
     */
    private final OddsGoods autoRandom = new OddsGoods();
    /**
     * 自动必出列表
     */
    private final OddsGoods autoMust = new OddsGoods();

    /**
     * 周期刷新商品列表(有就不走autoMust/autoRandom)
     */
    private List<List<ShopSellObject>> autoCycleGoods = Collections.emptyList();

    /**
     * <商店id,<商品组,商品>>
     */
    private static final Map<Integer, Map<Integer, List<ShopSellObject>>> groupSelle = new HashMap<>();

    //private Map<Integer<>>

    /**
     * 特殊类型商品,<SpecialType,>
     */
    private Map<Integer, List<ShopSellObject>> specialGoodsMap = new HashMap<>();

    /**
     * 从多个商品池中随机商品
     * <<商品池,权重>,个数></>
     */
    private Map<Map<List<ShopSellObject>, Integer>, Integer> groupShellRandomPool = new HashMap<>();

    private volatile long nextUpdateTime;


    public Store(ShopConfigObject shopCfg) {
        this.shopCfg = shopCfg;
    }

    public int getShopTypeValue() {
        return shopCfg.getId();
    }

    public boolean check() {
        if (null == shopCfg) {
            return false;
        }
        return true;
    }

    public void addGoods(ShopSellObject goods) {
        if (null == goods || getShopTypeValue() != goods.getShopid()) {
            LogUtil.error("can not addGoods to store,  store id =" + getShopTypeValue()
                    + "goods id =" + goods.getId() + ", goods store type = " + goods.getShopid());
            return;
        }
        if (goods.getSellgroup() > 0) {
            Map<Integer, List<ShopSellObject>> map = groupSelle.computeIfAbsent(goods.getShopid(), a -> new HashMap<>());
            map.computeIfAbsent(goods.getSellgroup(), a -> new ArrayList<>()).add(goods);
        }

        if (!isRandomStore()) {
            return;
        }

        //随机商店几率为0的商品不出现
        if (0 == goods.getAppearrate()) {
            LogUtil.warn("ShopSell id = " + goods.getId() + ", shop type need Random, but appear rate is 0, skip this ShopSell");
            return;
        }

        if (goods.getSpecialtype() == 0) {
            if (goods.getAppearrate() == -1) {
                this.autoMust.addGoods(goods);
            } else {
                this.autoRandom.addGoods(goods);
            }

            //不参与手动
            if (ArrayUtil.intArrayContain(shopCfg.getNotjoinmanual(), goods.getId())) {
                return;
            }

            if (goods.getAppearrate() == -1) {
                this.manualMust.addGoods(goods);
            } else {
                this.manualRandom.addGoods(goods);
            }
        } else {
            //特殊商品类型处理成必出类型
            specialGoodsMap.computeIfAbsent(goods.getSpecialtype(), e -> new ArrayList<>()).add(goods);
        }
    }

    /**
     * 是否支持手动刷新
     *
     * @return
     */
    public boolean supportManual() {
        return 1 == shopCfg.getManualrefresh();
    }

    public boolean canAutoRefresh() {
        return 0 != shopCfg.getAutorefresh();
    }

    /**
     * 返回不参与手动刷新的商品列表
     *
     * @return
     */
    public List<Integer> getNotJoinManualList() {
        return Arrays.stream(this.shopCfg.getNotjoinmanual()).boxed().collect(Collectors.toList());
    }

    /**
     * 是否需要服务器刷新
     * //首先判断商店是否是需要刷新的商店 -1 不用随机,
     * <p>
     * 设置了最大展示商品和最小展示商品或者设置了轮换字段
     *
     * @return
     */
    public boolean isRandomStore() {
        boolean randomSize = shopCfg.getRandommincount() > 0 && shopCfg.getRandommaxcount() > 0;
        return randomSize || ArrayUtil.checkArraySize(shopCfg.getGoodscycle(), 1, 1);
    }

    /**
     * 手动刷新
     *
     * @param goodsInfoList 当前玩家的商品记录
     * @return
     */
    public List<GoodsInfo> refreshByManual(String playerIdx, Collection<GoodsInfo> goodsInfoList) {
        List<GoodsInfo> result = new ArrayList<>();

        if (!GameUtil.collectionIsEmpty(goodsInfoList)) {
            List<Integer> notJoinManual = getNotJoinManualList();
            goodsInfoList.stream()
                    .filter(e -> notJoinManual.contains(e.getGoodsCfgId()))
                    .forEach(result::add);
        }

        List<ShopSellObject> randomGoods = randomByManual(playerIdx);
        if (!GameUtil.collectionIsEmpty(randomGoods)) {
            for (ShopSellObject goods : randomGoods) {
                result.add(GoodsInfo.newBuilder().setGoodsCfgId(goods.getId()).build());
            }
        }

        return result;
    }

    /**
     * 获取刷新个数
     *
     * @return
     */
    private int getRandomCount() {
        return RandomUtil.randomInScope(this.shopCfg.getRandommincount(), this.shopCfg.getRandommaxcount());
    }

    /**
     * @param autoRefreshTimes 第几次自动刷新
     * @return
     */
    public List<GoodsInfo> refreshByAuto(String playerIdx, int autoRefreshTimes) {
        if (!isRandomStore()) {
            return null;
        }
        List<ShopSellObject> randomGoods = randomByAuto(playerIdx, autoRefreshTimes);
        if (GameUtil.collectionIsEmpty(randomGoods)) {
            return null;
        }

        return randomGoods.stream()
                .map(e -> GoodsInfo.newBuilder().setGoodsCfgId(e.getId()).build())
                .collect(Collectors.toList());
    }

    /**
     * 手动随机
     *
     * @return
     */
    private List<ShopSellObject> randomByManual(String playerIdx) {
        List<ShopSellObject> result = new ArrayList<>(this.manualMust.getAll());

        List<ShopSellObject> specialGoods = specialMust(playerIdx);
        if (CollectionUtils.isNotEmpty(specialGoods)) {
            result.addAll(specialGoods);
        }

        Set<ShopSellObject> randomList = this.manualRandom.randomGet(getRandomCount());
        if (CollectionUtils.isNotEmpty(randomList)) {
            result.addAll(randomList);
        }

        return result;
    }

    /**
     * 自动随机
     *
     * @param autoRefreshTimes
     * @return
     */
    private List<ShopSellObject> randomByAuto(String playerIdx, int autoRefreshTimes) {
        int initCycle = getShopCfg().getInitcycle();
        int generalCycle = getShopCfg().getGeneralcycle();

        //轮换商店
        if (CollectionUtils.isNotEmpty(autoCycleGoods) && initCycle > 0 && generalCycle > 0) {
            if (autoRefreshTimes <= 0) {
                return autoCycleGoods.get(0);
            }
            return autoCycleGoods.get(autoRefreshTimes % autoCycleGoods.size());
        }

        if (!MapUtils.isEmpty(groupShellRandomPool)) {
            return randomGoodsFromMuiltGroup();
        }


        List<ShopSellObject> result = new ArrayList<>(this.autoMust.getAll());

        List<ShopSellObject> specialGoods = specialMust(playerIdx);
        if (CollectionUtils.isNotEmpty(specialGoods)) {
            result.addAll(specialGoods);
        }

        Set<ShopSellObject> randomList = this.autoRandom.randomGet(getRandomCount());
        if (CollectionUtils.isNotEmpty(randomList)) {
            result.addAll(randomList);
        }
        return filterShopList(playerIdx,result);
    }

    private List<ShopSellObject> filterShopList(String playerIdx, List<ShopSellObject> result) {
        if (Shop.ShopTypeEnum.STE_CrossArena_VALUE == getShopTypeValue()) {
            int gradeLv = CrossArenaManager.getInstance().findPlayerGradeLv(playerIdx);
            if (CollectionUtils.isEmpty(result)) {
                return result;
            }
            return result.stream().filter(e -> e.getHonrlv() == gradeLv).collect(Collectors.toList());
        }
        return result;
    }

    private List<ShopSellObject> randomGoodsFromMuiltGroup() {
        List<ShopSellObject> result = new ArrayList<>();
        int needNum;
        int totalWeight;
        for (Entry<Map<List<ShopSellObject>, Integer>, Integer> entry : groupShellRandomPool.entrySet()) {
            needNum = entry.getValue();

            Map<List<ShopSellObject>, Integer> sellPools = entry.getKey();

            totalWeight = sellPools.values().stream().mapToInt(Integer::intValue).sum();
            int num = 0;
            int totalCycle = 999;
            int cycle = 0;
            do {
                ShopSellObject one = randomOne(totalWeight, sellPools);
                if (one != null && !result.contains(one)) {
                    result.add(one);
                    num++;
                }
                cycle++;
            } while (num < needNum && cycle < totalCycle);
        }
        return result;
    }

    private ShopSellObject randomOne(int totalWeight, Map<List<ShopSellObject>, Integer> sellPools) {
        int random = RandomUtils.nextInt(totalWeight);

        for (Entry<List<ShopSellObject>, Integer> sellEntry : sellPools.entrySet()) {
            if (random < sellEntry.getValue()) {
                List<ShopSellObject> result = randomShellFromList(sellEntry.getKey(), 1);
                return CollectionUtils.isEmpty(result) ? null : result.get(0);
            }
            random -= sellEntry.getValue();
        }
        return null;
    }

    private List<ShopSellObject> randomShellFromList(List<ShopSellObject> source, int needNum) {
        if (CollectionUtils.isEmpty(source) || needNum <= 0) {
            return Collections.emptyList();
        }
        if (source.size() <= needNum) {
            return source;
        }
        int odds = source.stream().mapToInt(ShopSellObject::getAppearrate).sum();
        List<ShopSellObject> result = new ArrayList<>();
        while (result.size() < needNum) {
            int random = RandomUtils.nextInt(odds);
            for (ShopSellObject shopSellObject : source) {
                if (random < shopSellObject.getAppearrate()) {
                    result.add(shopSellObject);
                    break;
                }
                random -= shopSellObject.getAppearrate();
            }
        }
        return result;
    }

    private List<ShopSellObject> randomShellFromMuiltGroup() {
        return null;
    }


//    public void tick() {
//        if (!canAutoRefresh()) {
//            return;
//        }
//
//        if (getNextUpdateTime() == 0) {
//            this.setNextUpdateTime(calculateNextUpdateTime());
//        }
//
//        if (GlobalTick.getInstance().getCurrentTime() >= this.nextUpdateTime) {
//            EventUtil.unlockObjEvent(EventType.ET_AUTO_REFRESH_SHOP, ShopTypeEnum.forNumber(shopCfg.getId()));
//            this.setNextUpdateTime(calculateNextUpdateTime());
//        }
//    }

    /**
     * 计算商店下次刷新时间
     *
     * @return
     */
//    private long calculateNextUpdateTime() {
//        int autoRefreshType = shopCfg.getAutorefresh();
//        //0不自动刷新
//        //1可自动刷新，每日刷新，使用统一时间刷新
//        //2自动刷新, 固定时间间隔刷新
//        //3每月一号刷新（一天的统一刷新时间）
//        long currentTime = GlobalTick.getInstance().getCurrentTime();
//        if (1 == autoRefreshType) {
//            TimeUtil.getNextDayResetTime(currentTime);
//        } else if (2 == autoRefreshType) {
//            long refreshInterval = shopCfg.getRefreshtime() * TimeUtil.MS_IN_A_HOUR;
//            return ((currentTime / refreshInterval) + 1) * refreshInterval;
//        }/* else if (3 == autoRefreshType) {
//            return getNextOpenTimeType3();
//        }*/
//        LogUtil.error("shop :" + getShopTypeValue() + ", refresh type is not supported");
//        return TimeUtil.getNextDayResetTime(currentTime);
//    }
    /*
     *//**
     * 用于计算刷新类型为3的商店开启时间
     *
     * @return
     *//*
    public long getNextOpenTimeType3() {
        int[] openMonthArray = shopCfg.getServerrefreshpermonth();

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        int curMonth = TimeUtil.getMonth(currentTime);

        int maxMonth = ArrayUtil.getMaxInt(openMonthArray, curMonth);
        int nextOpenMonth = curMonth;
        if (maxMonth > curMonth) {
            for (int i = curMonth + 1; i <= maxMonth; i++) {
                if (ArrayUtil.intArrayContain(openMonthArray, i)) {
                    nextOpenMonth = i;
                    break;
                }
            }
        } else {
            nextOpenMonth = ArrayUtil.getMinInt(openMonthArray, curMonth);
        }
        return TimeUtil.getMonthFirstResetTime(currentTime, nextOpenMonth);
    }*/

    /**
     * 创建一个新的playerStoreInfo
     *
     * @return
     */
    public PlayerShopInfo createPlayerShopInfoBuilder(String playerIdx) {
        PlayerShopInfo result = new PlayerShopInfo();
        List<ShopSellObject> goodsList = randomByAuto(playerIdx, 0);
        if (!GameUtil.collectionIsEmpty(goodsList)) {
            goodsList.forEach(e ->
                    result.putBuyRecord(e.getId(), GoodsInfo.newBuilder().setGoodsCfgId(e.getId()).build()));
        }
        result.setAutoRefreshTimes(1);
        return result;
    }

    /**
     * 特殊必出
     *
     * @param playerIdx
     * @return
     */
    private List<ShopSellObject> specialMust(String playerIdx) {
        if (this.specialGoodsMap.isEmpty()) {
            LogUtil.debug("model.shop.Store.specialRandom, shop id:" + this.shopCfg.getId() + ", have no special goods");
            return Collections.emptyList();
        }

        List<ShopSellObject> result = new ArrayList<>();
        for (Entry<Integer, List<ShopSellObject>> entry : specialGoodsMap.entrySet()) {
            if (entry.getKey() == SpecialType.FIND_IN_PLAYER_LV_SCOPE) {
                List<ShopSellObject> type1List = randomSpecialGoodsType1(playerIdx, entry.getValue());
                if (CollectionUtils.isNotEmpty(type1List)) {
                    result.addAll(type1List);
                }
            }
        }

        return result;
    }

    /**
     * @return
     */
    private List<ShopSellObject> randomSpecialGoodsType1(String playerIdx, List<ShopSellObject> shopSellList) {
        if (StringUtils.isBlank(playerIdx) || CollectionUtils.isEmpty(shopSellList)) {
            return Collections.emptyList();
        }

        int playerLv = PlayerUtil.queryPlayerLv(playerIdx);

        return shopSellList.stream()
                .filter(e -> {
                    if (e == null) {
                        return false;
                    }

                    int[] specialParam = e.getSpecialparam();
                    if (specialParam.length < 2) {
                        LogUtil.error("Store.randomSpecialGoodsType1, type 1 params length is less than 2, shop sell cfg:" + e.getShopid());
                        return false;
                    }

                    return GameUtil.inScope(specialParam[0], specialParam[1], playerLv);
                })
                .collect(Collectors.toList());
    }

    public boolean afterGoodsInit() {
        if (ArrayUtils.isEmpty(shopCfg.getRandomgroupcfg())) {
            return true;
        }
        Map<Integer, List<ShopSellObject>> allGoods = groupSelle.get(shopCfg.getId());
        if (MapUtils.isEmpty(allGoods)) {
            LogUtil.error("Shop Config setting randomGroupCfg,but find no sells in shopSell,shopId:{} ", shopCfg.getId());
            return false;
        }
        for (int[] ints : shopCfg.getRandomgroupcfg()) {
            Map<List<ShopSellObject>, Integer> sells = new HashMap<>();
            int needNum = ints[ints.length - 1];
            groupShellRandomPool.put(sells, needNum);
            for (int i = 0; i < ints.length - 1; i = i + 2) {
                List<ShopSellObject> sellObjects = allGoods.get(ints[i]);
                if (sellObjects == null || sellObjects.size() < needNum) {
                    LogUtil.error("Shop Config setting randomGroupCfg,shop sell size not enough," +
                            "shop:{} ,group config size:{},but only:{}", shopCfg.getId(), needNum, sellObjects == null ? 0 : sellObjects.size());
                    continue;
                }
                sells.put(sellObjects, ints[i + 1]);
            }
        }
        return true;
    }
}
