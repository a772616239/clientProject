/**
 * created by tool DAOGenerate
 */
package model.exchangehistory.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import common.IdGenerator;
import model.obj.BaseObj;
import protocol.ExchangeHistoryDB.ExchangeHistory;


/**
 * created by tool
 */
@SuppressWarnings("serial")
public class exchangehistoryEntity extends BaseObj {
    exchangehistoryEntity() {
    }

    @Override
    public String getClassType() {
        return "exchangehistoryEntity";
    }

    @Override
    public void putToCache() {

    }

    /**
     * 主键
     */
    private String idx;

    /**
     * 信息所属玩家idx
     */
    private String playeridx;

    /**
     * 兑换记录
     */
    private byte[] exchangehistory;


    /**
     * 获得主键
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置主键
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得信息所属玩家idx
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置信息所属玩家idx
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得兑换记录
     */
    public byte[] getExchangehistory() {
        return exchangehistory;
    }

    /**
     * 设置兑换记录
     */
    public void setExchangehistory(byte[] exchangehistory) {
        this.exchangehistory = exchangehistory;
    }

    @Override
    public String getBaseIdx() {
        return idx;
    }

    @Override
    public void transformDBData() {

    }

    /***************************分割**********************************/
    private ExchangeHistory exchangeHistoryEntity;

    public ExchangeHistory getExchangeHistoryEntity() {
        return exchangeHistoryEntity;
    }

    public void setExchangeHistoryEntity(ExchangeHistory exchangeHistoryEntity) {
        this.exchangeHistoryEntity = exchangeHistoryEntity;
    }

    public exchangehistoryEntity(String initPlayerId) {
        idx = IdGenerator.getInstance().generateId();
        playeridx = initPlayerId;
        exchangeHistoryEntity = ExchangeHistory.newBuilder().build();
    }

    public void refresh() {
        exchangehistory = exchangeHistoryEntity.toByteArray();
    }

    public void toBuilder() throws InvalidProtocolBufferException {
        exchangeHistoryEntity = ExchangeHistory.parseFrom(exchangehistory);
    }
}