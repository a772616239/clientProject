package model.exchangehistory.dbCache.service;

import cfg.*;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import java.util.ArrayList;
import java.util.List;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.exchangehistory.dbCache.exchangehistoryCache;
import model.exchangehistory.entity.ExchangeHistoryResult;
import model.exchangehistory.entity.exchangehistoryEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Activity.*;
import protocol.Activity.ClientActivity.Builder;
import protocol.Common.Consume;
import protocol.Common.RewardSourceEnum;
import protocol.ExchangeHistory.ExchangeMessageEnum;
import protocol.ExchangeHistoryDB.ExchangeStoreEnum;
import protocol.ExchangeHistoryDB.RecoderStore;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Shop.GoodsInfo;
import util.LogUtil;
import util.TimeUtil;


/**
 * @author xiao_FL
 * @date 2019/11/7
 */
public class ExchangeHistoryServiceImpl implements IExchangeHistoryService {
    private exchangehistoryCache exchangeHistoryCacheInstance = exchangehistoryCache.getInstance();

    private static final IExchangeHistoryService exchangeHistoryService = new ExchangeHistoryServiceImpl();

    private ExchangeHistoryServiceImpl() {
    }

    public static IExchangeHistoryService getInstance() {
        return exchangeHistoryService;
    }

    @Override
    public RetCodeEnum exchangeGift(ExchangeStoreEnum storeEnum, String playerId, int goodId, int amount) {
        exchangehistoryEntity cache = exchangeHistoryCacheInstance.getExchangeHistoryCacheTempByPlayerId(playerId);
        if (cache == null || playerId == null || storeEnum == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        // 获取购买配置
        int buyLimit = Gift.getById(goodId).getLimit();
        int[]  price = Gift.getById(goodId).getConsume();

        if (price == null) {
            return RetCodeEnum.RCE_ErrorParam;
        } else {
            GoodsInfo oldRecord = getOldRecord(storeEnum, cache, goodId);
            boolean ifRecord = true;
            if (oldRecord == null) {
                // 未检查到配置记录，则是新添加的配置商品，初始化
                oldRecord = GoodsInfo.newBuilder().setGoodsCfgId(goodId).setAlreadBuyTimes(0).build();
                ifRecord = false;
            }
            if (buyLimit != -1 && oldRecord.getAlreadBuyTimes() + amount > buyLimit) {
                return RetCodeEnum.RCE_ErrorParam;
            } else {
                if (shopConsume(price, amount, playerId, storeEnum)) {
                    dateSave(cache, ifRecord, oldRecord.toBuilder().setAlreadBuyTimes(amount + oldRecord.getAlreadBuyTimes()).build(), storeEnum);
                    return RetCodeEnum.RCE_Success;
                } else {
                    return RetCodeEnum.RCE_Itembag_ItemNotEnought;
                }
            }
        }
    }

    private void dateSave(exchangehistoryEntity cache, boolean ifRecord, GoodsInfo refreshRecord, ExchangeStoreEnum exchangeStoreEnum) {
        if (cache != null && refreshRecord != null && exchangeStoreEnum != null) {
            SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
                List<GoodsInfo> recordList = null;
                switch (exchangeStoreEnum) {
                    case LIMIT_GIFT:
                        recordList = new ArrayList<>(cacheTemp.getExchangeHistoryEntity().getLimitStore().getRecordList());
                        break;
                    case WORTHY_GIFT:
                        recordList = new ArrayList<>(cacheTemp.getExchangeHistoryEntity().getWorthyStore().getRecordList());
                        break;
                    default:
                        return;
                }
                if (ifRecord) {
                    for (GoodsInfo goodsInfo : recordList) {
                        if (goodsInfo.getGoodsCfgId() == refreshRecord.getGoodsCfgId()) {
                            recordList.remove(goodsInfo);
                            break;
                        }
                    }
                }
                recordList.add(refreshRecord);
                RecoderStore recoderStore;
                switch (exchangeStoreEnum) {
                    case LIMIT_GIFT:
                        recoderStore = cacheTemp.getExchangeHistoryEntity().getLimitStore().toBuilder().clearRecord().addAllRecord(recordList).build();
                        cacheTemp.setExchangeHistoryEntity(cacheTemp.getExchangeHistoryEntity().toBuilder().setLimitStore(recoderStore).build());
                        // 发放购买结果
                        RewardManager.getInstance().doRewardByList(cacheTemp.getPlayeridx(), RewardUtil.parseRewardIntArrayToRewardList(Gift.getById(refreshRecord.getGoodsCfgId()).getReward()), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_LimitGift), true);
                        break;
                    case WORTHY_GIFT:
                        recoderStore = cacheTemp.getExchangeHistoryEntity().getWorthyStore().toBuilder().clearRecord().addAllRecord(recordList).build();
                        cacheTemp.setExchangeHistoryEntity(cacheTemp.getExchangeHistoryEntity().toBuilder().setWorthyStore(recoderStore).build());
                        // 发放购买结果
                        RewardManager.getInstance().doRewardByList(cacheTemp.getPlayeridx(), RewardUtil.parseRewardIntArrayToRewardList(Gift.getById(refreshRecord.getGoodsCfgId()).getReward()), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_WortyGift), true);
                        break;
                    default:
                        break;
                }
                // 持久化
                exchangeHistoryCacheInstance.flush(cacheTemp);
            });
        }
    }

    private boolean shopConsume(int[] price, int amount, String playerId, ExchangeStoreEnum exchangeStoreEnum) {
        // 扣除消耗
        Consume consume = ConsumeUtil.parseConsume(price);
        if (consume == null) {
            return false;
        }
        if (amount > 1) {
            consume = consume.toBuilder().setCount(consume.getCount() * amount).build();
        }
        switch (exchangeStoreEnum) {
            case LIMIT_GIFT:
                return ConsumeManager.getInstance().consumeMaterial(playerId, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_LimitGift));
            case WORTHY_GIFT:
                return ConsumeManager.getInstance().consumeMaterial(playerId, consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_WortyGift));
            default:
                return false;
        }
    }

    private GoodsInfo getOldRecord(ExchangeStoreEnum exchangeStoreEnum, exchangehistoryEntity cache, int goodId) {
        GoodsInfo oldInfo = null;
        if (exchangeStoreEnum != null && cache != null) {
            switch (exchangeStoreEnum) {
                case LIMIT_GIFT:
                    if (cache.getExchangeHistoryEntity().getLimitStore() != null && cache.getExchangeHistoryEntity().getLimitStore().getRecordList().size() > 0) {
                        for (GoodsInfo info : cache.getExchangeHistoryEntity().getLimitStore().getRecordList()) {
                            if (info.getGoodsCfgId() == goodId) {
                                oldInfo = info;
                                break;
                            }
                        }
                    }
                    break;
                case WORTHY_GIFT:
                    if (cache.getExchangeHistoryEntity().getWorthyStore() != null && cache.getExchangeHistoryEntity().getWorthyStore().getRecordList().size() > 0) {
                        for (GoodsInfo info : cache.getExchangeHistoryEntity().getWorthyStore().getRecordList()) {
                            if (info.getGoodsCfgId() == goodId) {
                                oldInfo = info;
                                break;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return oldInfo;
    }

    @Override
    public ExchangeHistoryResult queryStoreHistory(String playerId, ExchangeStoreEnum exchangeStoreEnum) {
        ExchangeHistoryResult result = new ExchangeHistoryResult();
        exchangehistoryEntity cache = exchangeHistoryCacheInstance.getExchangeHistoryCacheTempByPlayerId(playerId);
        if (cache == null || cache.getExchangeHistoryEntity() == null) {
            cache = new exchangehistoryEntity(playerId);
            exchangeHistoryCacheInstance.add(cache);
        }
        long nowTime = GlobalTick.getInstance().getCurrentTime();
        LogUtil.debug("queryStoreHistory get now time :" + nowTime);
        switch (exchangeStoreEnum) {
            case LIMIT_GIFT:
                RecoderStore limitStoreRecord = cache.getExchangeHistoryEntity().getLimitStore();
                SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
                    GiftConfigObject cfg = GiftConfig.getById(ExchangeMessageEnum.LIMIT_GIFT_VALUE);
                    if (limitStoreRecord.getRecordList().size() == 0 || ((long) cfg.getBegintime() * 1000 != limitStoreRecord.getStartTime() && (long) cfg.getEndtime() * 1000 != limitStoreRecord.getEndTime())) {
                        RecoderStore newLimitStoreRecord = RecoderStore.newBuilder().addAllRecord(getNewRecord(ExchangeStoreEnum.LIMIT_GIFT))
                                .setStartTime((long) cfg.getBegintime() * 1000)
                                .setEndTime((long) cfg.getEndtime() * 1000).build();
                        cacheTemp.setExchangeHistoryEntity(cacheTemp.getExchangeHistoryEntity().toBuilder().setLimitStore(newLimitStoreRecord).build());
                        exchangeHistoryCacheInstance.add(cacheTemp);
                    }
                });
                result.setGoodsInfoList(cache.getExchangeHistoryEntity().getLimitStore().getRecordList());
                result.setSuccess(true);
                return result;
            case WORTHY_GIFT:
                RecoderStore worthStoreRecord = cache.getExchangeHistoryEntity().getWorthyStore();
                SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
                    GiftConfigObject cfg = GiftConfig.getById(ExchangeMessageEnum.WORTY_GIFT_VALUE);
                    if (worthStoreRecord.getRecordList().size() == 0 || ((long) cfg.getBegintime() * 1000 != worthStoreRecord.getStartTime() && (long) cfg.getEndtime() * 1000 != worthStoreRecord.getEndTime())) {
                        RecoderStore newWorthStoreRecord = RecoderStore.newBuilder().addAllRecord(getNewRecord(ExchangeStoreEnum.WORTHY_GIFT))
                                .setStartTime((long) cfg.getBegintime() * 1000)
                                .setEndTime((long) cfg.getEndtime() * 1000).build();
                        cacheTemp.setExchangeHistoryEntity(cacheTemp.getExchangeHistoryEntity().toBuilder().setWorthyStore(newWorthStoreRecord).build());
                        exchangeHistoryCacheInstance.add(cacheTemp);
                    }
                });
                result.setGoodsInfoList(cache.getExchangeHistoryEntity().getWorthyStore().getRecordList());
                result.setSuccess(true);
                return result;
            default:
                break;
        }
        result.setCode(RetCodeEnum.RCE_ErrorParam);
        return result;
    }

    private List<GoodsInfo> getNewRecord(ExchangeStoreEnum exchangeStoreEnum) {
        List<GoodsInfo> result = new ArrayList<>();
        List<ShopSellObject> shopCfg = null;
        List<GiftObject> gifCfg = null;
        switch (exchangeStoreEnum) {
            case LIMIT_GIFT:
                gifCfg = Gift.Limit_Gift;
                break;
            case WORTHY_GIFT:
                gifCfg = Gift.Worth_Gift;
                break;
            default:
                break;
        }
        if (shopCfg != null) {
            for (ShopSellObject shopSellObject : shopCfg) {
                GoodsInfo info = GoodsInfo.newBuilder()
                        .setGoodsCfgId(shopSellObject.getId())
                        .setAlreadBuyTimes(0)
                        .build();
                result.add(info);
            }
        }
        if (gifCfg != null) {
            for (GiftObject giftObject : gifCfg) {
                GoodsInfo info = GoodsInfo.newBuilder()
                        .setGoodsCfgId(giftObject.getId())
                        .setAlreadBuyTimes(0)
                        .build();
                result.add(info);
            }
        }
        return result;
    }

    @Override
    public ClientActivity buildGiftClientActivity(ActivityTypeEnum activityType) {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        long endTime;
        if (activityType == ActivityTypeEnum.ATE_LimitGift) {
            endTime = GiftConfig.getById(ExchangeMessageEnum.LIMIT_GIFT_VALUE).getDisplayendtime() * TimeUtil.MS_IN_A_S;
        } else {
            endTime = GiftConfig.getById(ExchangeMessageEnum.WORTY_GIFT_VALUE).getDisplayendtime() * TimeUtil.MS_IN_A_S;
        }

        if (currentTime >= endTime) {
            return null;
        }

        Builder builder = ClientActivity.newBuilder();
        builder.setActivityType(ActivityTypeEnum.ATE_BossBattle);

        Cycle_TimeLimit.Builder cycle = Cycle_TimeLimit.newBuilder();
        cycle.setBeginTimestamp(0);
        cycle.setEndTimestamp(endTime);

        ActivityTime.Builder timeBuilder = ActivityTime.newBuilder();
        timeBuilder.setTimeType(CycleTypeEnum.CTE_TimeLimit);
        timeBuilder.setTimeContent(cycle.build().toByteString());
        builder.setCycleTime(timeBuilder);
        return builder.build();
    }


    /**
     * 寻找下一个刷新月份
     *
     * @param cfgMonths 配置刷新月份（有序）
     * @param nowMonth  当前月份
     * @return 下个刷新月
     */
    private int findNextRefreshMonth(int[] cfgMonths, int nowMonth) {
        for (int cfgMonth : cfgMonths) {
            if (cfgMonth >= nowMonth) {
                return cfgMonth;
            }
        }
        return 12 + cfgMonths[0];
    }
}
