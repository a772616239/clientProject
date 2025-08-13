/*CREATED BY TOOL*/

package model.petgem.dbCache;

import annotation.annationInit;
import cfg.PetGemConfig;
import cfg.PetGemConfigObject;
import common.IdGenerator;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petgem.cache.petgemUpdateCache;
import model.petgem.entity.petgemEntity;
import model.player.util.PlayerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StringUtils;
import platform.logs.ReasonManager.Reason;
import platform.logs.statistics.GemStatistics;
import protocol.PetMessage.Gem;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.LogUtil;
import util.MapUtil;

@annationInit(value = "petgemCache", methodname = "load")
public class petgemCache extends baseCache<petgemCache> implements IbaseCache {
    /******************* MUST HAVE ********************************/

    private static petgemCache instance = null;

    public static petgemCache getInstance() {

        if (instance == null) {
            instance = new petgemCache();
        }
        return instance;

    }

    @Override
    public String getDaoName() {

        return "petgemDAO";
    }

    @Override
    public BaseDAO getDao() {

        return AppContext.getBean("petgemDAO");
    }

    @Override
    public void load(baseCache o) {

        if (instance == null) {
            instance = (petgemCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(petgemEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static petgemEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx)) {
            return null;
        }


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null) {
            return null;
        }

        return (petgemEntity) v;

    }

    @Override
    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    @Override
    public baseUpdateCache getUpdateCache() {

        return petgemUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    @Override
    public void putToMem(BaseEntity v) {
        petgemEntity entity = (petgemEntity) v;
        if (entity.getPlayeridx() != null) {
            gemEntity.put(entity.getPlayeridx(), entity);
        }
    }

    /***************************分割**********************************/

    /**
     * <playerIdx, entity>
     */
    private final Map<String, petgemEntity> gemEntity = new ConcurrentHashMap<>();

    public petgemEntity getEntityByPlayer(String playerId) {
        if (StringHelper.isNull(playerId)) {
            return null;
        }

        petgemEntity entity = gemEntity.get(playerId);
        if (entity == null && PlayerUtil.playerIsExist(playerId)) {
            entity = new petgemEntity(playerId);
            entity.putToCache();
            return entity;
        }

        return entity;
    }

    public Gem buildNewGem(int gemCfgId) {
        PetGemConfigObject config = PetGemConfig.getById(gemCfgId);
        if (config == null) {
            LogUtil.warn("buildNewGem, gemCfgId is not exist, id:{} ", gemCfgId);
            return null;
        }
        return Gem.newBuilder().setGemConfigId(gemCfgId).setId(IdGenerator.getInstance().generateId()).build();
    }


    public boolean playerObtainGem(petgemEntity cache, Map<Integer, Integer> gemMap, Reason reason) {
        if (cache == null) {
            return false;
        }
        List<Gem> addGemList = new ArrayList<>();
        for (Entry<Integer, Integer> entry : gemMap.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                Gem newGem = buildNewGem(entry.getKey());
                if (newGem == null) {
                    continue;
                }
                addGemList.add(newGem);
            }

            //目标：累计获得X个X品质的宝石(额外条件:宝石品质),
            EventUtil.triggerUpdateTargetProgress(cache.getPlayeridx(), TargetTypeEnum.TEE_Gem_CumuGain, entry.getValue()
                    , PetGemConfig.queryRarity(entry.getKey()));
        }
        cache.putAllGem(addGemList);
        addGemMap2Statistics(gemMap);
        petgemEntity.sendGemGet(cache.getPlayeridx(), addGemList);
        return true;
    }

    private void addGemMap2Statistics(Map<Integer, Integer> gemMap) {
        if (MapUtils.isEmpty(gemMap)) {
            return;
        }
        Map<Integer, Long> map = new HashMap<>();
        for (Entry<Integer, Integer> entry : gemMap.entrySet()) {

            PetGemConfigObject config = PetGemConfig.getById(entry.getKey());
            if (config == null) {
                continue;
            }
            MapUtil.add2LongMapValue(map, config.getRarity(), (long) entry.getValue());
        }
        GemStatistics.getInstance().updateOwnGemRarityMap(map);
    }

    public int getRemainCapacity(String playerIdx) {
        petgemEntity cache = getEntityByPlayer(playerIdx);
        if (cache != null) {
            return Math.max(0, cache.getCapacity() - cache.getOccupancy());
        }
        return 0;
    }

    public void resetPetGemStatus(String playerIdx, List<String> gemIds) {

        petgemEntity cache = petgemCache.getInstance().getEntityByPlayer(playerIdx);
        if (cache == null || CollectionUtils.isEmpty(gemIds)) {
            return;
        }
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (petEntity == null) {
            return;
        }
        for (String gemId : gemIds) {
            Gem gem = cache.getGemById(gemId);
            if (gem == null) {
                continue;
            }
            Gem newGem = gem.toBuilder().clearGemPet().build();
            cache.putGem(newGem);
            cache.sendGemUpdate(newGem);
        }
    }

    public List<Gem> getGemListByPets(String playerId, List<String> petIdList) {
        if (StringUtils.isEmpty(petIdList)) {
            return Collections.emptyList();
        }
        petgemEntity entity = getEntityByPlayer(playerId);
        if (entity == null) {
            return Collections.emptyList();
        }

        List<Gem> result = new ArrayList<>();
        for (String petId : petIdList) {
            for (Gem gem : entity.getGemListBuilder().getGemsMap().values()) {
                if (petId.equals(gem.getGemPet())) {
                    result.add(gem);
                }
            }
        }
        return result;
    }
/*
    public boolean removeGem(petgemEntity cache, List<String> idList, Reason reason) {
        if (cache == null) {
            return false;
        }

        if (idList == null || idList.isEmpty()) {
            LogUtil.warn("PetGemServiceImpl.removeGem, remove gem id list is null");
            return true;
        }

        return SyncExecuteFunction.executeFunction(cache, cacheTemp -> {
            if (!cacheTemp.gemListCanRemove(idList)) {
                return false;
            }

            //统计删除之前符文的拥有量
            Map<Integer, Integer> ownedCountMap = new HashMap<>();
            for (String gemIdx : idList) {
                Gem gem = cache.getGemById(gemIdx);
                if (gem == null) {
                    continue;
                }
                ownedCountMap.put(gem.getGemConfigId(), cache.getSameGemBookIdCountByBookId(gem.getGemConfigId()));
            }

            //统计日志re
            for (Entry<Integer, Integer> entry : ownedCountMap.entrySet()) {
                LogService.getInstance().submit(new DailyDateLog(cache.getPlayeridx(), true, RewardTypeEnum.RTE_Gem,
                        entry.getKey(), entry.getValue(), entry.getValue(), cacheTemp.getSameGemBookIdCountByBookId(entry.getKey()), reason));
            }

            petgemEntity.sendGemRemove(cache.getPlayeridx(), idList);
            return true;
        });
    }*/

    public Gem getGemByGemIdx(String playerIdx, String gemIdx) {
        if (StringUtils.isEmpty(gemIdx) || StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        petgemEntity entity = getEntityByPlayer(playerIdx);
        if (entity == null) {
            return null;
        }
        return entity.getGemById(gemIdx);
    }

}
