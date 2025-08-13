/*CREATED BY TOOL*/

package model.foreignInvasion.dbCache;

import annotation.annationInit;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Collection;
import java.util.Map;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.foreignInvasion.cache.foreigninvasionUpdateCache;
import model.foreignInvasion.entity.foreigninvasionEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

@annationInit(value = "foreigninvasionCache", methodname = "load")
public class foreigninvasionCache extends baseCache<foreigninvasionCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static foreigninvasionCache instance = null;

    public static foreigninvasionCache getInstance() {

        if (instance == null) {
            instance = new foreigninvasionCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "foreigninvasionDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("foreigninvasionDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (foreigninvasionCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(foreigninvasionEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static foreigninvasionEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (foreigninvasionEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return foreigninvasionUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        foreigninvasionEntity t = (foreigninvasionEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    public foreigninvasionEntity getEntity(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }

        foreigninvasionEntity entity = getByIdx(playerIdx);
        if (entity == null) {
            entity = new foreigninvasionEntity((playerIdx));
            put(entity);
        }
        return entity;
    }

    public int getPlayerCurScore(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return 0;
        }
        foreigninvasionEntity entity = getByIdx(playerIdx);
        if (entity != null) {
            return entity.getDbBuilder().getTotalScore();
        }
        return 0;
    }


    public void clearAllPlayerBuildingsInfo() {
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof foreigninvasionEntity)) {
                continue;
            }

            foreigninvasionEntity entity = (foreigninvasionEntity) value;
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.getDbBuilder().clear();
            });
        }
    }

    public void sendPlayerBuildingInfo(Collection<String> needSendPlayer) {
        if (CollectionUtils.isEmpty(needSendPlayer)) {
            return;
        }

        for (String playerIdx : needSendPlayer) {
            foreigninvasionEntity entity = getEntity(playerIdx);
            if (entity != null) {
                SyncExecuteFunction.executeConsumer(entity, e -> {
                    entity.sendBuildingsInfo();
                });
            }
        }
    }

    public int getPlayerPetRemainHp(String playerId, String petId) {
        if (StringUtils.isEmpty(playerId) || StringUtils.isEmpty(petId)) {
            return 0;
        }
        return getEntity(playerId).getPetRemainHp(petId);
    }
}
