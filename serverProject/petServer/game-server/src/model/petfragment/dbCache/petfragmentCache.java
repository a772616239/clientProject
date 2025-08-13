/*CREATED BY TOOL*/

package model.petfragment.dbCache;

import annotation.annationInit;
import com.google.protobuf.InvalidProtocolBufferException;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.petfragment.cache.petfragmentUpdateCache;
import model.petfragment.entity.petfragmentEntity;
import util.LogUtil;

import java.util.Map;

@annationInit(value = "petfragmentCache", methodname = "load")
public class petfragmentCache extends baseCache<petfragmentCache> implements IbaseCache {
    /******************* MUST HAVE ********************************/

    private static petfragmentCache instance = null;

    public static petfragmentCache getInstance() {

        if (instance == null) {
            instance = new petfragmentCache();
        }
        return instance;

    }

    @Override
    public String getDaoName() {

        return "petfragmentDAO";
    }

    @Override
    public BaseDAO getDao() {

        return AppContext.getBean("petfragmentDAO");
    }

    @Override
    public void load(baseCache o) {

        if (instance == null) {
            instance = (petfragmentCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(petfragmentEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static petfragmentEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (petfragmentEntity) v;

    }

    @Override
    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    @Override
    public baseUpdateCache getUpdateCache() {

        return petfragmentUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    @Override
    public void putToMem(BaseEntity v) {
        petfragmentEntity entity = (petfragmentEntity) v;
        try {
            entity.toBuilder();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error(e.toString());
        }
    }

    /***************************分割**********************************/

    public petfragmentEntity getCacheByPlayer(String playerId) {
        for (BaseEntity value : _ix_id.values()) {
            petfragmentEntity entity = (petfragmentEntity) value;
            if (entity.getPlayeridx().equals(playerId)) {
                return entity;
            }
        }
        return null;
    }

    public void add(petfragmentEntity entity) {
        entity.refresh();
        getInstance().putToDb(entity);
        getInstance()._ix_id.put(entity.getIdx(), entity);
    }

    public void flush(petfragmentEntity entity) {
        entity.refresh();
        getInstance().putToDb(entity);
    }

/*******************MUST HAVE END ********************************/
}
