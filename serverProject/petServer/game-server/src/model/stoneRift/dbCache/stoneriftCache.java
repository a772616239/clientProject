/*CREATED BY TOOL*/

package model.stoneRift.dbCache;

import annotation.annationInit;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.stoneRift.cache.stoneriftUpdateCache;
import model.stoneRift.stoneriftEntity;

@annationInit(value = "stoneriftCache", methodname = "load")
public class stoneriftCache extends baseCache<stoneriftCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static stoneriftCache instance = null;

    /*******************MUST HAVE END ********************************/

    public static stoneriftCache getInstance() {

        if (instance == null) {
            instance = new stoneriftCache();
        }
        return instance;

    }

    public static void put(stoneriftEntity v) {
        getInstance().putBaseEntity(v);
    }

    public static stoneriftEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (stoneriftEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    @Override
    public BaseDAO getDao() {
        return AppContext.getBean("stoneriftDAO");
    }

    @Override
    public baseUpdateCache getUpdateCache() {
        return stoneriftUpdateCache.getInstance();
    }

    @Override
    public void putToMem(BaseEntity baseEntity) {
        stoneriftEntity t = (stoneriftEntity) baseEntity;
    }

    @Override
    public void load(baseCache baseCache) {
        if (instance == null) {
            instance = (stoneriftCache) baseCache;
        }
        super.loadAllFromDb();
    }

    public String getDaoName() {

        return "stoneriftDAO";
    }

    public void createNewEntity(String idx) {
        stoneriftEntity stoneRift = new stoneriftEntity(idx);
        SyncExecuteFunction.executeConsumer(stoneRift,st ->{
            stoneRift.init();
        } );
    }
}
