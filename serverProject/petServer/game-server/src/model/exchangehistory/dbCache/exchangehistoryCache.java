/*CREATED BY TOOL*/

package model.exchangehistory.dbCache;

import annotation.annationInit;
import com.google.protobuf.InvalidProtocolBufferException;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.exchangehistory.cache.exchangehistoryUpdateCache;
import model.exchangehistory.entity.exchangehistoryEntity;
import util.LogUtil;

import java.util.Map;

@annationInit(value = "exchangehistoryCache", methodname = "load")
public class exchangehistoryCache extends baseCache<exchangehistoryCache> implements IbaseCache {
    /******************* MUST HAVE ********************************/

    private static exchangehistoryCache instance = null;

    public static exchangehistoryCache getInstance() {

        if (instance == null) {
            instance = new exchangehistoryCache();
        }
        return instance;

    }

    @Override
    public String getDaoName() {

        return "exchangehistoryDAO";
    }

    @Override
    public BaseDAO getDao() {

        return AppContext.getBean("exchangehistoryDAO");
    }

    @Override
    public void load(baseCache o) {

        if (instance == null) {
            instance = (exchangehistoryCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(exchangehistoryEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static exchangehistoryEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (exchangehistoryEntity) v;

    }

    @Override
    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    @Override
    public baseUpdateCache getUpdateCache() {

        return exchangehistoryUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    @Override
    public void putToMem(BaseEntity v) {
        exchangehistoryEntity entity = (exchangehistoryEntity) v;
        try {
            entity.toBuilder();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error(e.toString());
        }
    }

    /***************************分割**********************************/

    public exchangehistoryEntity getExchangeHistoryCacheTempByPlayerId(String playerId) {
        for (BaseEntity value : _ix_id.values()) {
            exchangehistoryEntity entity = (exchangehistoryEntity) value;
            if (entity.getPlayeridx().equals(playerId)) {
                return entity;
            }
        }
        return null;
    }

    public void add(exchangehistoryEntity entity) {
        entity.refresh();
        getInstance().putToDb(entity);
        getInstance()._ix_id.put(entity.getIdx(), entity);
    }

    public void flush(exchangehistoryEntity entity) {
        entity.refresh();
        getInstance().putToDb(entity);
    }

/*******************MUST HAVE END ********************************/
}
