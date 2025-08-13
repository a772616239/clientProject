package model.drawCard;

import cfg.*;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.*;

import model.mainLine.dbCache.mainlineCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.lang.StringUtils;
import protocol.Common.Reward;
import protocol.DrawCard.EnumDrawCardType;
import protocol.PlayerDB.DB_DrawCardData.Builder;
import util.GameUtil;
import util.LogUtil;

public class DrawCardManager {
    private static DrawCardManager instance = new DrawCardManager();

    public static DrawCardManager getInstance() {
        if (instance == null) {
            synchronized (DrawCardManager.class) {
                if (instance == null) {
                    instance = new DrawCardManager();
                }
            }
        }
        return instance;
    }

    private DrawCardManager() {
    }

    /**
     * 缓存图鉴信息
     */
    private DrawRateLimitCfg tujianCfg = new DrawRateLimitCfg();

    /**
     * 抽卡最高品质
     */
    public static int HIGHEST_QUALITY;

    private final Map<EnumDrawCardType, DrawCardPool> drawCardPools = new HashMap<>();

    private DrawCardObject drawCardCfg;

    public boolean init() {
        this.drawCardCfg = DrawCard.getById(GameConst.CONFIG_ID);
        HIGHEST_QUALITY = drawCardCfg.getHighestquality();
        readyDrawRateInfo();
        return initFriendShipPool() &&
                initCommonCardPool() &&
                initHighCardPool();
    }

    /**
     * 准备抽卡概率显示信息
     * @return
     */
    public boolean readyDrawRateInfo() {
        DrawRateLimitCfg tujianCfg = new DrawRateLimitCfg();
        Map<Integer, Map<Integer, DrawPetRateBean>> temppetidInfo = new HashMap<Integer, Map<Integer, DrawPetRateBean>>();
        Map<Integer, DrawPetRateBean> tempspidInfo = new HashMap<Integer, DrawPetRateBean>();
        Map<Integer, DrawPetRateBean> tempspidInfoHigh = new HashMap<Integer, DrawPetRateBean>();
        Map<Integer, DrawPetRateBean> tempspidInfoAncient = new HashMap<Integer, DrawPetRateBean>();
        // 准备抽卡宠物信息
        for (DrawCommonCardConfigObject dccco : DrawCommonCardConfig._ix_id.values()) {
            int suipianid = dccco.getRewards()[1];
            // 查找该奖池是否可以合成
            PetFragmentConfigObject pfco = PetFragmentConfig.getById(suipianid);
            if (null == pfco) {
                continue;
            }
            if (tempspidInfo.containsKey(suipianid)) {
                continue;
            }
            // 查找对应的宠物
            PetBasePropertiesObject pbpo = PetBaseProperties.getByPetid(pfco.getPetid());
            if (null == pbpo) {
                continue;
            }
            DrawPetRateBean dprb = new DrawPetRateBean();
            dprb.setPetId(pbpo.getPetid());
            dprb.setPetcore(pbpo.getPetcore());
            dprb.setSuipianId(suipianid);
            dprb.setUnlocklimit(pbpo.getDrawgk());
            dprb.setNum(pfco.getAmount());
            temppetidInfo.computeIfAbsent(pbpo.getPetcore(), k -> new HashMap<Integer, DrawPetRateBean>()).put(pbpo.getPetid(), dprb);
            tempspidInfo.put(suipianid, dprb);
        }
        for (DrawHighCardConfigObject dccco : DrawHighCardConfig._ix_id.values()) {
            int suipianid = dccco.getRewards()[1];
            // 查找该奖池是否可以合成
            PetFragmentConfigObject pfco = PetFragmentConfig.getById(suipianid);
            if (null == pfco) {
                continue;
            }
            if (tempspidInfoHigh.containsKey(suipianid)) {
                continue;
            }
            // 查找对应的宠物
            PetBasePropertiesObject pbpo = PetBaseProperties.getByPetid(pfco.getPetid());
            if (null == pbpo) {
                continue;
            }
            DrawPetRateBean dprb = new DrawPetRateBean();
            dprb.setPetId(pbpo.getPetid());
            dprb.setPetcore(pbpo.getPetcore());
            dprb.setSuipianId(suipianid);
            dprb.setUnlocklimit(pbpo.getDrawgk());
            dprb.setNum(pfco.getAmount());
            temppetidInfo.computeIfAbsent(pbpo.getPetcore(), k -> new HashMap<Integer, DrawPetRateBean>()).put(pbpo.getPetid(), dprb);
            tempspidInfoHigh.put(suipianid, dprb);
        }

        for (AncientCallObject aco : AncientCall._ix_id.values()) {
            int suipianid = aco.getContant()[1];
            // 查找该奖池是否可以合成
            PetFragmentConfigObject pfco = PetFragmentConfig.getById(suipianid);
            if (null == pfco) {
                continue;
            }
            if (tempspidInfoAncient.containsKey(suipianid)) {
                continue;
            }
            // 查找对应的宠物
            PetBasePropertiesObject pbpo = PetBaseProperties.getByPetid(pfco.getPetid());
            if (null == pbpo) {
                continue;
            }
            DrawPetRateBean dprb = new DrawPetRateBean();
            dprb.setPetId(pbpo.getPetid());
            dprb.setPetcore(pbpo.getPetcore());
            dprb.setSuipianId(suipianid);
            dprb.setUnlocklimit(pbpo.getDrawgk());
            dprb.setNum(pfco.getAmount());
            temppetidInfo.computeIfAbsent(pbpo.getPetcore(), k -> new HashMap<Integer, DrawPetRateBean>()).put(pbpo.getPetid(), dprb);
            tempspidInfoAncient.put(suipianid, dprb);
        }
        tujianCfg.setCoreKeyCommon(temppetidInfo);
        tujianCfg.setPetspKeyCommon(tempspidInfo);
        tujianCfg.setPetspKeyhigh(tempspidInfoHigh);
        tujianCfg.setPetspKeyAncient(tempspidInfoAncient);
        this.tujianCfg = tujianCfg;
        return true;
    }

    /**
     * 初始化友情点奖池
     *
     * @return
     */
    private boolean initFriendShipPool() {
        DrawCardPool drawCardPool = DrawCardPool.createDrawCardPoolEntity(EnumDrawCardType.EDCT_FRIEND,
                drawCardCfg.getFriendshipcardweight(), drawCardCfg.getFriendoddschangequality(),
                drawCardCfg.getFriendmustquality(), drawCardCfg.getFriendpettypeodds());
        if (drawCardPool == null) {
            return false;
        }
        for (OddsRandom value : DrawFriendShipCardConfig._ix_id.values()) {
            drawCardPool.addOddsRandom(value);
        }
        drawCardPools.put(EnumDrawCardType.EDCT_FRIEND, drawCardPool);
        return true;
    }

    /**
     * 初始化普通抽卡奖池
     *
     * @return
     */
    private boolean initCommonCardPool() {
        DrawCardPool drawCardPool = DrawCardPool.createDrawCardPoolEntity(EnumDrawCardType.EDCT_COMMON,
                drawCardCfg.getCommoncardqualityweight(), drawCardCfg.getCommonoddschangequality(),
                drawCardCfg.getCommondrawcardmust(), drawCardCfg.getCommonpettypeodds());
        if (drawCardPool == null) {
            return false;
        }
        for (OddsRandom value : DrawCommonCardConfig._ix_id.values()) {
            drawCardPool.addOddsRandom(value);
        }
        drawCardPools.put(EnumDrawCardType.EDCT_COMMON, drawCardPool);
        return true;
    }

    /**
     * 初始化高级奖次
     */
    private boolean initHighCardPool() {
        DrawCardPool drawCardPool = DrawCardPool.createDrawCardPoolEntity(EnumDrawCardType.EDCT_HIGH,
                drawCardCfg.getHighcardqualityweight(), drawCardCfg.getHighoddschangequality(),
                drawCardCfg.getHighcardmustquality(), drawCardCfg.getHighpettypeodds());
        if (drawCardPool == null) {
            return false;
        }
        for (OddsRandom value : DrawHighCardConfig._ix_id.values()) {
            drawCardPool.addOddsRandom(value);
        }
        drawCardPools.put(EnumDrawCardType.EDCT_HIGH, drawCardPool);
        return true;
    }

    public DrawCardPool getDrawCardPool(EnumDrawCardType type) {
        DrawCardPool cardPool = drawCardPools.get(type);
        if (cardPool == null) {
            LogUtil.error("DrawCardManager.getDrawCardPool, type pool is not exist, type =" + type);
        }
        return cardPool;
    }

    public List<OddsRandom> drawCardByType(EnumDrawCardType type, String playerIdx, int times, boolean mustDrawCorePet) {
        DrawCardPool pool = getDrawCardPool(type);
        if (pool == null) {
            return null;
        }
        return drawCardByPool(pool, playerIdx, times, mustDrawCorePet);
    }

    /**
     * 抽卡
     *
     * @param cardPool 奖池类
     * @return
     */
    private List<OddsRandom> drawCardByPool(DrawCardPool cardPool, String playerIdx, int times, boolean mustDrawCorePet) {
        if (cardPool == null || times <= 0) {
            return null;
        }
        List<OddsRandom> randoms = cardPool.drawCard(playerIdx, times, mustDrawCorePet);

        if (!GameUtil.collectionIsEmpty(randoms) && DrawCardUtil.canMustGet(playerIdx, true)) {
            int[] mustQuality = cardPool.getMustGainQuality();
            for (int must : mustQuality) {
                if (DrawCardUtil.containSpecifyQuality(randoms, must)) {
                    continue;
                }
                OddsRandom oddsRandom = cardPool.randomByQuality(playerIdx, must, true);
                if (oddsRandom != null) {
                    LogUtil.info("DrawCardManager.drawCardByPool, player:" + playerIdx + "type：" + cardPool.getType()
                            + ", gain a must quality, quality: " + must);
                    replaceLowQuality(randoms, oddsRandom);

                    //重置概率
                    cardPool.resetPlayerOdds(playerIdx, must);
                }
            }
        }
        return randoms;
    }

    public List<Reward> drawFriendShipCard(String playerIdx, int times) {
        // 好友抽卡没有保底抽到核心魔灵
        List<OddsRandom> result = drawCardByType(EnumDrawCardType.EDCT_FRIEND, playerIdx, times, false);
        return DrawCardUtil.parseOddsRandomToReward(result);
    }

    /**
     * @param times 普通抽卡次数
     * @return
     */
    public List<Reward> drawCommonCard(String playerIdx, int times) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return null;
        }
        DrawCardPool cardPool = getDrawCardPool(EnumDrawCardType.EDCT_COMMON);
        if (cardPool == null) {
            return null;
        }
        int commonCardFloorTimes = player.getDb_data().getDrawCard().getCommonCardFloorTimes();
        int commonCardFloorTimesLimit = DrawCard.getById(GameConst.CONFIG_ID).getCommoncardcorefloortimes();
        List<OddsRandom> randoms = drawCardByPool(cardPool, playerIdx, times, commonCardFloorTimes + times >= commonCardFloorTimesLimit);
        if (GameUtil.collectionIsEmpty(randoms)) {
            return null;
        }

        SyncExecuteFunction.executeConsumer(player, entity->{
            boolean containCorePet = false;
            for (OddsRandom oddsRandom : randoms) {
                if (oddsRandom.getIscorepet()) {
                    containCorePet = true;
                    break;
                }
            }
            if (containCorePet) {
                entity.getDb_data().getDrawCardBuilder().clearCommonCardFloorTimes();
            } else {
                entity.getDb_data().getDrawCardBuilder().setCommonCardFloorTimes(commonCardFloorTimes + times);
            }
        });

        //第一次十连保底
//        int firstQuality = drawCardCfg.getCommonfirsttendrawsmustquality();
//        if (times >= 10 && isFirstTenDraws(playerIdx)) {
//            if (!DrawCardUtil.containSpecifyQuality(randoms, firstQuality)) {
//                OddsRandom oddsRandom = cardPool.randomByQuality(playerIdx, firstQuality);
//                if (oddsRandom == null) {
//                    LogUtil.error("DrawCardManager.drawCommonCard, first ten draws must quality:" + firstQuality
//                            + ", random failed, can not random a rewards");
//                } else {
//                    replaceLowQuality(randoms, oddsRandom);
//                }
//            }
//            //使用了第一次十连抽保底
//            setUseFirstTenDraws(playerIdx);
//        }

        //判断是否出特殊品质,
        if (DrawCardUtil.containSpecifyQuality(randoms, HIGHEST_QUALITY)) {
            LogUtil.debug("player:" + playerIdx + " get common special quality:"
                    + HIGHEST_QUALITY + ", reset consume");
            resetPlayerConsume(playerIdx);
        }
//        else {
//            int consume = queryPlayerConsume(playerIdx);
//            if (queryPlayerConsume(playerIdx) >= drawCardCfg.getCommonmustconsumecount()) {
//                OddsRandom oddsRandom = cardPool.randomByQuality(playerIdx, HIGHEST_QUALITY);
//                if (oddsRandom != null) {
//                    LogUtil.info("player:" + playerIdx + ", type :" + cardPool.getType()
//                            + "gain a specify quality, consume:" + consume);
//                    replaceLowQuality(randoms, oddsRandom);
//                    resetPlayerConsume(playerIdx);
//                }
//            }
//        }

        return DrawCardUtil.parseOddsRandomToReward(randoms);
    }

    /**
     * 判断是否是首次十连抽
     *
     * @param playerIdx
     * @return
     */
    private boolean isFirstTenDraws(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return false;
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return false;
        }

        return !entity.getDb_data().getDrawCard().getUseFirstTenDrawsCard();
    }

    /**
     * 修改玩家首次十连抽状态
     *
     * @param playerIdx
     */
    private void setUseFirstTenDraws(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDb_data().getDrawCardBuilder().setUseFirstTenDrawsCard(true);
        });
    }

    /**
     * 查询抽卡消耗
     *
     * @param playerIdx
     * @return
     */
    private int queryPlayerConsume(String playerIdx) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return 0;
        }
        return entity.getDb_data().getDrawCard().getDrawCardConsume();
    }

    /**
     * 重置抽卡消耗
     *
     * @param playerIdx
     */
    private void resetPlayerConsume(String playerIdx) {
        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDb_data().getDrawCardBuilder().clearDrawCardConsume();
        });
    }

    /**
     * 重置高级抽卡奖池,固定返回10个
     */
    public Collection<OddsRandom> drawHighCard(String playerIdx, boolean mustDrawCorePet) {
        int highPoolCount = drawCardCfg.getHighcardpoolcount();

        DrawCardPool drawCardPool = getDrawCardPool(EnumDrawCardType.EDCT_HIGH);
        if (drawCardPool == null) {
            return null;
        }

        List<OddsRandom> oddsRandoms = drawCardPool.uniqueDrawCard(playerIdx, highPoolCount, mustDrawCorePet);
        if (GameUtil.collectionIsEmpty(oddsRandoms)) {
            LogUtil.error("DrawCardManager.drawHighCard, draw high card failed");
            return null;
        }
//        for (int must : drawCardPool.getMustGainQuality()) {
//            if (DrawCardUtil.containSpecifyQuality(oddsRandoms, must)) {
//                continue;
//            }
//
//            OddsRandom mustRandom = drawCardPool.uniqueRandomByQuality(playerIdx, must, oddsRandoms);
//            if (mustRandom == null) {
//                continue;
//            }
//
//            replaceLowQuality(oddsRandoms, mustRandom);
//            drawCardPool.resetPlayerOdds(playerIdx, must);
//        }

        return oddsRandoms;
    }

    /**
     * 替换指定列表 品质低于当前品质的位置,如果没有低于指定品质的则不替换
     *
     * @param target
     * @return
     */
    public List<OddsRandom> replaceLowQuality(List<OddsRandom> target, OddsRandom replace) {
        if (GameUtil.collectionIsEmpty(target) || replace == null) {
            return target;
        }

        int randomIndex = new Random().nextInt(target.size());
        OddsRandom oddsRandom = target.get(randomIndex);
        if (oddsRandom.getQuality() < replace.getQuality()) {
            target.set(randomIndex, replace);
        } else {
            //遍历替换一次低品质
            for (int i = 0; i < target.size(); i++) {
                if (target.get(i).getQuality() < replace.getQuality()) {
                    target.set(i, replace);
                    break;
                }
            }
        }
        return target;
    }

    public boolean canDraw(String playerIdx, int doCount) {
        if (StringUtils.isBlank(playerIdx)) {
            return false;
        }

        if (doCount <= 0) {
            return true;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }

        BanShuConfigObject banShuCfg = BanShuConfig.getById(GameConst.CONFIG_ID);

        return banShuCfg.getDrawcardlimit() == GameConst.UN_LIMIT
                || (player.getDb_data().getDrawCard().getTodayDrawCount() + doCount) <= banShuCfg.getDrawcardlimit();
    }

    public void addDrawCount(String playerIdx, int doCount) {
        if (StringUtils.isBlank(playerIdx) || doCount <= 0) {
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            Builder drawCardBuilder = player.getDb_data().getDrawCardBuilder();
            drawCardBuilder.setTodayDrawCount(drawCardBuilder.getTodayDrawCount() + doCount);
        });
    }

    public DrawRateLimitCfg getTujianCfg() {
        return tujianCfg;
    }

    public void setTujianCfg(DrawRateLimitCfg tujianCfg) {
        this.tujianCfg = tujianCfg;
    }

    public List<Integer> getUnlockReward(String playerIdx, EnumDrawCardType type) {
        List<Integer> unlockid = new ArrayList<Integer>();
        int playergk = mainlineCache.getInstance().getPlayerCurCheckPoint(playerIdx);
        if (type == EnumDrawCardType.EDCT_COMMON) {
            for (DrawPetRateBean ent : tujianCfg.getPetspKeyCommon().values()) {
                if (ent.getUnlocklimit() <= playergk) {
                    unlockid.add(ent.getSuipianId());
                }
            }
        } else if (type == EnumDrawCardType.EDCT_HIGH) {
            for (DrawPetRateBean ent : tujianCfg.getPetspKeyhigh().values()) {
                if (ent.getUnlocklimit() <= playergk) {
                    unlockid.add(ent.getSuipianId());
                }
            }
        } else {
            for (DrawPetRateBean ent : tujianCfg.getPetspKeyAncient().values()) {
                if (ent.getUnlocklimit() <= playergk) {
                    unlockid.add(ent.getSuipianId());
                }
            }
        }
        return unlockid;
    }

}
