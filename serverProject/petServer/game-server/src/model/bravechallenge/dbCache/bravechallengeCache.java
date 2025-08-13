/*CREATED BY TOOL*/

package model.bravechallenge.dbCache;

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
import model.bravechallenge.cache.bravechallengeUpdateCache;
import model.bravechallenge.entity.bravechallengeEntity;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import protocol.Battle.BattleRemainPet;
import protocol.BraveChallenge.ChallengeProgress;
import protocol.BraveChallengeDB;
import util.LogUtil;

@annationInit(value = "bravechallengeCache", methodname = "load")
public class bravechallengeCache extends baseCache<bravechallengeCache> implements IbaseCache, UpdateDailyData {
    /******************* MUST HAVE ********************************/

    private static bravechallengeCache instance = null;

    public static bravechallengeCache getInstance() {
        if (instance == null) {
            instance = new bravechallengeCache();
        }
        return instance;
    }

    @Override
    public String getDaoName() {
        return "bravechallengeDAO";
    }

    @Override
    public BaseDAO getDao() {
        return AppContext.getBean("bravechallengeDAO");
    }

    @Override
    public void load(baseCache o) {
        if (instance == null) {
            instance = (bravechallengeCache) o;
        }
        super.loadAllFromDb();
    }

    public static void put(bravechallengeEntity v) {
        getInstance().putBaseEntity(v);
    }

    public static bravechallengeEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx)) {
            return null;
        }
        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null) {
            return null;
        }
        return (bravechallengeEntity) v;
    }

    @Override
    public Map<String, BaseEntity> getAll() {
        return getInstance()._ix_id;
    }

    @Override
    public baseUpdateCache getUpdateCache() {
        return bravechallengeUpdateCache.getInstance();
    }

    public static void remove(String idx) {
        getInstance().BaseRemove(idx);
    }

    @Override
    public void putToMem(BaseEntity v) {
        bravechallengeEntity entity = (bravechallengeEntity) v;
        if (entity != null && StringUtils.isNotBlank(entity.getPlayeridx())) {
            entityMap.put(entity.getPlayeridx(), entity);
        }
    }

    /***************************分割**********************************/

    private final Map<String, bravechallengeEntity> entityMap = new ConcurrentHashMap<>();

    public bravechallengeEntity getEntityByPlayer(String playerId) {
        if (StringHelper.isNull(playerId)) {
            return null;
        }

        bravechallengeEntity entity = entityMap.get(playerId);
        if (entity == null && PlayerUtil.playerIsExist(playerId)) {
            entity = new bravechallengeEntity(playerId);
            entity.putToCache();
        }
        return entity;
    }

    @Override
    public void updateDailyData() {
        for (bravechallengeEntity entity : entityMap.values()) {
            try {
                SyncExecuteFunction.executeConsumer(entity,
                        e -> entity.updateDailyDate(GlobalData.getInstance().checkPlayerOnline(entity.getPlayeridx())));
            } catch (Exception ex) {
                LogUtil.error("braveChallengeCache.updateDailyData error by playerId:[{}]", entity.getPlayeridx());
                LogUtil.printStackTrace(ex);
            }
        }
    }

    public ChallengeProgress getPlayerProgress(String playerIdx) {
        if (playerIdx == null) {
            return null;
        }

        bravechallengeEntity cacheByPlayer = getEntityByPlayer(playerIdx);
        if (cacheByPlayer == null) {
            return null;
        }

        return cacheByPlayer.getClientProgress();
    }

    public int getPetRemainHpRate(String playerId, String petId) {
        bravechallengeEntity entity = getEntityByPlayer(playerId);
        if (entity == null || StringHelper.isNull(petId)) {
            return 0;
        }
        BattleRemainPet battleRemainPet = entity.getProgressBuilder().getPetsRemainHpMap().get(petId);
        if (battleRemainPet == null) {
            return 1000;
        }
        return battleRemainPet.getRemainHpRate();
    }

    public static boolean isFirstFail(int winnerCamp, BraveChallengeDB.DB_ChallengeProgress.Builder progressBuilder) {
        return winnerCamp == 2 && !progressBuilder.getGiftFirstBlood();
    }

    public void initPoint(String playerIdx) {
        bravechallengeEntity entity = getEntityByPlayer(playerIdx);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executePredicate(entity, e -> entity.initAllPoint());
    }
}
