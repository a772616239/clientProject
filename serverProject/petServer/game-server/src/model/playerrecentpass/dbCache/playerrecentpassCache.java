/*CREATED BY TOOL*/

package model.playerrecentpass.dbCache;

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
import model.playerrecentpass.cache.playerrecentpassUpdateCache;
import model.playerrecentpass.entity.playerrecentpassEntity;
import model.recentpassed.RecentPassedUtil;
import org.apache.commons.lang.StringUtils;
import protocol.Common.EnumFunction;
import protocol.RecentPassedDB.DB_RecentPlayerInfo;

@annationInit(value = "playerrecentpassCache", methodname = "load")
public class playerrecentpassCache extends baseCache<playerrecentpassCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static playerrecentpassCache instance = null;

    public static playerrecentpassCache getInstance() {

        if (instance == null) {
            instance = new playerrecentpassCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "playerrecentpassDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("playerrecentpassDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (playerrecentpassCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(playerrecentpassEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static playerrecentpassEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (playerrecentpassEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return playerrecentpassUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        playerrecentpassEntity t = (playerrecentpassEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    public playerrecentpassEntity getEntity(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        playerrecentpassEntity entity = getByIdx(playerIdx);
        if (entity == null) {
            entity = new playerrecentpassEntity(playerIdx);
            put(entity);
        }
        return entity;
    }

    public void updateRecentPassTeam(String playerIdx, EnumFunction function) {
        if (StringUtils.isEmpty(playerIdx) || function == null) {
            return;
        }

        DB_RecentPlayerInfo.Builder recentPlayerInfo = RecentPassedUtil.buildPlayerRecentInfo(playerIdx, function);
        if (recentPlayerInfo == null) {
            return;
        }

        playerrecentpassEntity entity = getEntity(playerIdx);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.updateRecentPassTeam(function, recentPlayerInfo.build());
        });
    }
}
