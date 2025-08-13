package model.exchangehistory.entity;

import protocol.Shop.GoodsInfo;
import entity.CommonResult;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/11/8
 */
public class ExchangeHistoryResult extends CommonResult {
    /**
     * 兑换记录
     */
    private List<GoodsInfo> goodsInfoList;

    /**
     * 刷新时间
     */
    private long endTime;

    public List<GoodsInfo> getGoodsInfoList() {
        return goodsInfoList;
    }

    public void setGoodsInfoList(List<GoodsInfo> goodsInfoList) {
        this.goodsInfoList = goodsInfoList;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
