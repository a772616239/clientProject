/*CREATED BY TOOL*/

package model.training.dbCache;

import annotation.annationInit;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import common.tick.Tickable;
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
import model.training.cache.trainingUpdateCache;
import model.training.entity.trainingEntity;
import util.LogUtil;

@annationInit(value = "trainingDAO", methodname = "load")
public class trainingCache extends baseCache<trainingCache> implements IbaseCache, Tickable, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static trainingCache instance = null;

    public static trainingCache getInstance() {

        if (instance == null) {
            instance = new trainingCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "trainingDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("trainingDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (trainingCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(trainingEntity v) {

        getInstance().putBaseEntity(v);
    }

    public static trainingEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;
        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;
        return (trainingEntity) v;
    }

    public Map<String, BaseEntity> getAll() {
        return getInstance()._ix_id;
    }

    public baseUpdateCache getUpdateCache() {
        return trainingUpdateCache.getInstance();
    }

    public static void remove(String idx) {
        getInstance().BaseRemove(idx);
    }


    public void putToMem(BaseEntity v) {
        trainingEntity entity = (trainingEntity) v;
        entity.toBuilder();
        trainEntityMap.put(entity.getPlayeridx(), entity);
    }

    /***************************分割**********************************/
    public trainingEntity getCacheByPlayer(String playerId) {
        return trainEntityMap.get(playerId);
    }

    public void flush(trainingEntity entity) {
        entity.refresh();
        put(entity);
    }

    /*******************MUST HAVE END ********************************/
    private Map<String, trainingEntity> trainEntityMap = new ConcurrentHashMap<>();

    @Override
    public void updateDailyData() {
    	Map<String, BaseEntity> all = getAll();
        for (BaseEntity entity : all.values()) {
            if (!(entity instanceof trainingEntity)) {
                return;
            }
            try {
            	trainingEntity training = (trainingEntity) entity;
                SyncExecuteFunction.executeConsumer(training, e -> training.updateDailyData());
            } catch (Exception e) {
                LogUtil.error("trainingCache.updateDailyData error by playerId:[{}]", ((trainingEntity) entity).getPlayeridx());
                LogUtil.printStackTrace(e);
            }
        }
    }


	@Override
	public void onTick() {
		long currentTime = GlobalTick.getInstance().getCurrentTime();
        for (BaseEntity entity : getInstance().getAll().values()) {
            if (entity instanceof trainingEntity) {
            	trainingEntity training = (trainingEntity) entity;
                try {
                	training.lockObj();
                	training.onTick(currentTime);
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                } finally {
                	training.unlockTickObj();
                }
            }
        }
	}

}
