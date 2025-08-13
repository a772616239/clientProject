/*CREATED BY TOOL*/

package model.inscription.dbCache;

import annotation.annationInit;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.inscription.cache.petinscriptionUpdateCache;
import model.inscription.petinscriptionEntity;
import model.player.util.PlayerUtil;
import platform.logs.ReasonManager.Reason;
import protocol.PetMessage.Inscription;

@annationInit(value = "petinscriptionCache", methodname = "load")
public class petinscriptionCache extends baseCache<petinscriptionCache> implements IbaseCache {
    /******************* MUST HAVE ********************************/

    private static petinscriptionCache instance = null;

    public static petinscriptionCache getInstance() {

        if (instance == null) {
            instance = new petinscriptionCache();
        }
        return instance;

    }

    @Override
    public String getDaoName() {

        return "petinscriptionDAO";
    }

    @Override
    public BaseDAO getDao() {

        return AppContext.getBean("petinscriptionDAO");
    }

    @Override
    public void load(baseCache o) {

        if (instance == null) {
            instance = (petinscriptionCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(petinscriptionEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static petinscriptionEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx)) {
            return null;
        }


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null) {
            return null;
        }

        return (petinscriptionEntity) v;

    }

    @Override
    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    @Override
    public baseUpdateCache getUpdateCache() {

        return petinscriptionUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    @Override
    public void putToMem(BaseEntity v) {
        petinscriptionEntity entity = (petinscriptionEntity) v;
        if (entity.getPlayeridx() != null) {
            inscriptionEntity.put(entity.getPlayeridx(), entity);
        }
    }

    /***************************分割**********************************/

    /**
     * <playerIdx, entity>
     */
    private final Map<String, petinscriptionEntity> inscriptionEntity = new ConcurrentHashMap<>();

    public petinscriptionEntity getEntityByPlayer(String playerId) {
        if (StringHelper.isNull(playerId)) {
            return null;
        }

        petinscriptionEntity entity = inscriptionEntity.get(playerId);
        if (entity == null && PlayerUtil.playerIsExist(playerId)) {
            entity = new petinscriptionEntity(playerId);
            return entity;
        }

        return entity;
    }


    public boolean playerObtainInscription(petinscriptionEntity cache, Map<Integer, Integer> inscriptionMap, Reason reason) {
        if (cache == null) {
            return false;
        }
        cache.playerObtainInscription(inscriptionMap,reason);
        return true;
    }

    private Inscription buildNewInscription(Integer key) {

        return null;
    }


}
