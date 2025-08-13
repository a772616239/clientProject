/*CREATED BY TOOL*/

package model.farmmine.dbCache;

import annotation.annationInit;
import com.google.protobuf.InvalidProtocolBufferException;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.farmmine.cache.farmmineUpdateCache;
import model.farmmine.entity.farmmineEntity;
import model.farmmine.util.FarmMineUtil;
import util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@annationInit(value = "farmmineCache", methodname = "load")
public class farmmineCache extends baseCache<farmmineCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static farmmineCache instance = null;

    public static farmmineCache getInstance() {

        if (instance == null) {
            instance = new farmmineCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "farmmineDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("farmmineDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (farmmineCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(farmmineEntity v) {

        getInstance().putBaseEntity(v);
    }

    public static farmmineEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;
        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (farmmineEntity) v;

    }

    public Map<String, BaseEntity> getAll() {
        return getInstance()._ix_id;
    }

    public baseUpdateCache getUpdateCache() {

        return farmmineUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    public void putToMem(BaseEntity v) {
        farmmineEntity entity = (farmmineEntity) v;
        try {
            entity.toBuilder();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error(e.toString());
        }
        if (!StringHelper.isNull(entity.getOccplayerid())) {
            playerKeyMap.put(entity.getOccplayerid(), entity);
        }
    }

    /***************************分割**********************************/

    public Map<String, BaseEntity> getAllNotPub() {
        Map<String, BaseEntity> temp = new HashMap<>();
        temp.putAll(_ix_id);
        temp.remove(FarmMineUtil.KEY_PUB);
        return temp;
    }

    private Map<String, farmmineEntity> playerKeyMap = new ConcurrentHashMap<>();

    public farmmineEntity getByPlayerIdx(String playerIdx) {
        return playerKeyMap.get(playerIdx);
    }

    public void clearPlayerKeyMap() {
        playerKeyMap.clear();
    }

    public boolean hasData() {
        return _ix_id.isEmpty();
    }

    public void flush(farmmineEntity entity) {
        entity.transformDBData();
        put(entity);
    }

}
