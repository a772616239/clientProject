/*CREATED BY TOOL*/

package model.petmission.dbCache;

import annotation.annationInit;
import common.GlobalData;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import entity.UpdateDailyData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.petmission.cache.petmissionUpdateCache;
import model.petmission.entity.petmissionEntity;
import model.player.util.PlayerUtil;
import util.LogUtil;

@annationInit(value = "petmissionCache", methodname = "load")
public class petmissionCache extends baseCache<petmissionCache> implements IbaseCache, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static petmissionCache instance = null;

    public static petmissionCache getInstance() {

        if (instance == null) {
            instance = new petmissionCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "petmissionDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("petmissionDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (petmissionCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(petmissionEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static petmissionEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (petmissionEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return petmissionUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {
        petmissionEntity entity = (petmissionEntity) v;
        if (entity != null) {
            missionEntityMap.put(entity.getPlayeridx(), entity);
        }
    }

    /***************************分割**********************************/

    private static Map<String, petmissionEntity> missionEntityMap = new ConcurrentHashMap<>();

    public petmissionEntity getEntityByPlayerIdx(String playerIdx) {
        if (StringHelper.isNull(playerIdx)) {
            return null;
        }

        petmissionEntity entity = missionEntityMap.get(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new petmissionEntity(playerIdx);
            entity.transformDBData();
            entity.putToCache();
        }

        return entity;
    }

    /**
     * 更新每日数据
     */
    @Override
    public void updateDailyData() {
        for (BaseEntity value : _ix_id.values()) {
            if (value instanceof petmissionEntity) {
                petmissionEntity entity = (petmissionEntity) value;
                try {
                    SyncExecuteFunction.executeConsumer(entity,
                            e -> entity.updateDailyData(GlobalData.getInstance().checkPlayerOnline(entity.getPlayeridx())));
                } catch (Exception ex) {
                    LogUtil.error("petMissionCache.updateDailyData error by playerId:[{}]", entity.getPlayeridx());
                    LogUtil.printStackTrace(ex);
                }
            }
        }
    }
}
