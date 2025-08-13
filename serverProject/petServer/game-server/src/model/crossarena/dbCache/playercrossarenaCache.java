/*CREATED BY TOOL*/

package model.crossarena.dbCache;

import annotation.annationInit;
import com.google.protobuf.InvalidProtocolBufferException;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import entity.UpdateDailyData;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.crossarena.cache.playercrossarenaUpdateCache;
import model.crossarena.entity.playercrossarenaEntity;
import model.training.entity.trainingEntity;
import util.LogUtil;

import java.util.Map;

@annationInit(value = "playercrossarenaCache", methodname = "load")
public class playercrossarenaCache extends baseCache<playercrossarenaCache> implements IbaseCache, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static playercrossarenaCache instance = null;

    public static playercrossarenaCache getInstance() {

        if (instance == null) {
            instance = new playercrossarenaCache();
        }
        return instance;

    }

    public String getDaoName() {

        return "playercrossarenaDAO";
    }

    public BaseDAO getDao() {
        return AppContext.getBean("playercrossarenaDAO");
    }

    public void load(baseCache o) {
        if (instance == null) {
            instance = (playercrossarenaCache) o;
        }
        super.loadAllFromDb();
    }

    public static void put(playercrossarenaEntity v) {
        getInstance().putBaseEntity(v);
    }

    public static playercrossarenaEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;
        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;
        return (playercrossarenaEntity) v;
    }

    public Map<String, BaseEntity> getAll() {
        return getInstance()._ix_id;
    }

    public baseUpdateCache getUpdateCache() {
        return playercrossarenaUpdateCache.getInstance();
    }

    public static void remove(String idx) {
        getInstance().BaseRemove(idx);
    }

    public void putToMem(BaseEntity v) {
        playercrossarenaEntity entity = (playercrossarenaEntity) v;
        try {
            entity.toBuilder();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error(e.toString());
        }
    }

    /*******************MUST HAVE END ********************************/

    @Override
    public void updateDailyData() {
        for (BaseEntity entity : _ix_id.values()) {
            try {
                playercrossarenaEntity training = (playercrossarenaEntity) entity;
                SyncExecuteFunction.executeConsumer(training, e -> training.updateDailyData());
            } catch (Exception e) {
                LogUtil.error("playercrossarenaCache.updateDailyData error by playerId:[{}]", ((playercrossarenaEntity) entity).getIdx());
                LogUtil.printStackTrace(e);
            }
        }
    }

}
