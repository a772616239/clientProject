package model.exchangehistory.dbCache.service;

import model.exchangehistory.entity.ExchangeHistoryResult;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.ClientActivity;
import protocol.ExchangeHistoryDB.ExchangeStoreEnum;
import protocol.RetCodeId.RetCodeEnum;

/**
 * @author xiao_FL
 * @date 2019/11/7
 */
public interface IExchangeHistoryService {
    /**
     * 处理宠物/勇气试炼/流浪者小屋商店购买请求
     *
     * @param storeEnum 商店类型
     * @param playerId  玩家id
     * @param goodId    商品id
     * @param amount    购买数量
     * @return 购买结果
     */
    RetCodeEnum exchangeGift(ExchangeStoreEnum storeEnum, String playerId, int goodId, int amount);

    /**
     * 查询购买记录
     *
     * @param playerId          玩家id
     * @param exchangeStoreEnum 玩家查询兑换记录枚举
     * @return 购买记录
     */
    ExchangeHistoryResult queryStoreHistory(String playerId, ExchangeStoreEnum exchangeStoreEnum);

    /**
     * 查询礼包活动是否开启
     *
     * @param activityTypeEnum 礼包活动类型
     * @return 未开启返回-1；开启返回结束时间戳
     */
    ClientActivity buildGiftClientActivity(ActivityTypeEnum activityTypeEnum);
}
