/*CREATED BY TOOL*/

package model.gameplay.dbCache;

import annotation.annationInit;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import io.netty.util.internal.ConcurrentSet;
import java.util.Map;
import java.util.Set;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.cache.gameplayUpdateCache;
import model.gameplay.entity.gameplayEntity;
import protocol.GameplayDB.GameplayTypeEnum;

@annationInit(value = "gameplayCache", methodname = "load")
public class gameplayCache extends baseCache<gameplayCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static gameplayCache instance = null;

    public static gameplayCache getInstance() {

        if (instance == null) {
            instance = new gameplayCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "gameplayDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("gameplayDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (gameplayCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(gameplayEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static gameplayEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (gameplayEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return gameplayUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        gameplayEntity t = (gameplayEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    private final Set<GamePlayerUpdate> updateSet = new ConcurrentSet<>();

    public boolean addToUpdateSet(GamePlayerUpdate gamePlayerUpdate) {
        if (gamePlayerUpdate == null) {
            return false;
        }
        return updateSet.add(gamePlayerUpdate);
    }

    public void update() {
        for (GamePlayerUpdate gamePlayerUpdate : updateSet) {
            gamePlayerUpdate.update();
        }
    }

    public gameplayEntity getByGamePlayType(GameplayTypeEnum gameplayType) {
        if (gameplayType == null || gameplayType == GameplayTypeEnum.GTE_Null) {
            return null;
        }

        String gamePlayIdx = String.valueOf(gameplayType.getNumber());
        gameplayEntity entity = getByIdx(gamePlayIdx);
        if (entity == null) {
            entity = new gameplayEntity();
            entity.setIdx(gamePlayIdx);
        }
        return entity;
    }
}
