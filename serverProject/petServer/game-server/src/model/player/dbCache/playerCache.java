/*CREATED BY TOOL*/

package model.player.dbCache;

import annotation.annationInit;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import entity.UpdateDailyData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.player.cache.playerUpdateCache;
import model.player.entity.playerEntity;
import protocol.Common.LanguageEnum;
import util.LogUtil;
import util.ObjUtil;
import util.TimeUtil;

@annationInit(value = "playerCache", methodname = "load")
public class playerCache extends baseCache<playerCache> implements IbaseCache, Tickable, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static playerCache instance = null;

    private Map<String, String> nameMap = new ConcurrentHashMap<>();

    private Map<String, String> userIdMap = new ConcurrentHashMap<>();

    private Map<Integer, String> shortIdPlayerIdxMap = new ConcurrentHashMap<>();

    public static playerCache getInstance() {
        if (instance == null) {
            instance = new playerCache();
        }
        return instance;
    }


    public String getDaoName() {

        return "playerDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("playerDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (playerCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(playerEntity v) {
        v.setUpdatetime(new Date(GlobalTick.getInstance().getCurrentTime()));
        v.updateDBClaimMap();
        getInstance().putBaseEntity(v);
    }

    public static playerEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;
        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        return v instanceof playerEntity ? (playerEntity) v : null;
    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return playerUpdateCache.getInstance();

    }

    public void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    public Map<String, String> getNameMap() {
        return getInstance().nameMap;
    }

    public void addToNameMap(playerEntity entity) {
        getInstance().nameMap.put(entity.getName(), entity.getIdx());
    }

    public void removeFromNameMap(String name) {
        getInstance().nameMap.remove(name);
    }

    public String getIdxByName(String name) {
        if (StringHelper.isNull(name)) {
            return null;
        }
        return getInstance().getNameMap().get(name);
    }

    public boolean isNameDuplicated(String name) {
        if (StringHelper.isNull(name)) {
            return false;
        }
        return getInstance().getNameMap().get(name) != null;
    }

    public Map<String, String> getUserIdMap() {
        return getInstance().userIdMap;
    }

    public String getIdxByUserId(String userId) {
        if (StringHelper.isNull(userId)) {
            return null;
        }
        return getInstance().getUserIdMap().get(userId);
    }

    public void addToUserIdMap(playerEntity entity) {
        if (entity != null) {
            getInstance().userIdMap.put(entity.getUserid(), entity.getIdx());
        }
    }

    public void removeFromUserIdMap(String userId) {
        getInstance().userIdMap.remove(userId);
    }

    public void putToMem(BaseEntity v) {
        if (v instanceof playerEntity) {
            playerEntity player = (playerEntity) v;
            if (getIdxByName(player.getName()) == null) {
                getInstance().addToNameMap(player);
            }
            if (getIdxByUserId(player.getUserid()) == null) {
                getInstance().addToUserIdMap(player);
            }

            if (!shortIdPlayerIdxMap.containsKey(player.getShortid())) {
                shortIdPlayerIdxMap.put(player.getShortid(), player.getIdx());
            }
        }
    }

    /*******************MUST HAVE END ********************************/

    public boolean hasPlayerIdx(String playerIdx) {
    	return getAll().containsKey(playerIdx);
    }

    public playerEntity getPlayerByShortId(int shortId) {
        return getByIdx(shortIdPlayerIdxMap.get(shortId));
    }

    public String getPlayerIdxByShortId(int shortId) {
        return shortIdPlayerIdxMap.get(shortId);
    }

    public boolean isNameDuplicate(String name) {
        String player = getIdxByName(name);
        return !StringHelper.isNull(player);
    }

    public List<String> getAllPlayerIdx() {
        return Collections.unmodifiableList(new ArrayList<>(userIdMap.values()));
    }

    @Override
    public void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        for (BaseEntity entity : getInstance().getAll().values()) {
            if (entity instanceof playerEntity) {
                playerEntity player = (playerEntity) entity;
                if (player.tryLockObj()) {
                    try {
                        player.onTick(currentTime);
                    } catch (Exception e) {
                        LogUtil.printStackTrace(e);
                    } finally {
                        player.unlockTickObj();
                    }
                } else {
                    LogUtil.warn("player[{}] tryLock onTick failed, name={}", player.getIdx(), player.getName());
                }
            }
        }
    }

    public String getPlayerIdxByName(String nameStr) {
        if (nameStr == null) {
            return null;
        }

        if (nameMap.containsKey(nameStr)) {
            return nameMap.get(nameStr);
        }

        return null;
    }

    public playerEntity getPlayerByName(String name) {
        return getByIdx(getPlayerIdxByName(name));
    }


    /**
     * 好友模糊查询结果
     */
    private Map<String, List<String>> nameLikeMap = new ConcurrentHashMap<>();
    /**
     * 模糊查询更新时间
     */
    private Map<String, Long> nameLikeUpdateTime = new ConcurrentHashMap<>();

    /**
     * 通过名字模糊查询playerIdx
     *
     * @param nameStr
     * @return
     */
    public List<String> getPlayerIdxByNameLike(String nameStr) {
        if (StringHelper.isNull(nameStr)) {
            return null;
        }

        //先判断是否有记录,且在有效时间内
        if (nameLikeMap.containsKey(nameStr)
                && (GlobalTick.getInstance().getCurrentTime() - nameLikeUpdateTime.get(nameStr)) < TimeUtil.MS_IN_A_MIN) {
            return nameLikeMap.get(nameStr);
        }

        List<String> result = new ArrayList<>();
        for (Entry<String, String> entry : nameMap.entrySet()) {
            if (entry.getKey().contains(nameStr)) {
                result.add(entry.getValue());
            }
        }

        nameLikeMap.put(nameStr, result);
        nameLikeUpdateTime.put(nameStr, GlobalTick.getInstance().getCurrentTime());
        return result;
    }

    /**
     * 根据姓名模糊查询玩家
     *
     * @param nameStr
     * @return
     */
    public List<playerEntity> getPlayerByNameLike(String nameStr) {
        List<String> nameLikeList = getPlayerIdxByNameLike(nameStr);
        if (nameLikeList != null && !nameLikeList.isEmpty()) {
            List<playerEntity> result = new ArrayList<>();
            for (String idx : nameLikeList) {
                playerEntity player = getByIdx(idx);
                if (player != null) {
                    result.add(player);
                }
            }
            return result;
        }
        return null;
    }

    public int getUserIdMapSize() {
        return userIdMap.size();
    }

    public playerEntity getPlayerByUserId(String userId) {
        return getByIdx(getPlayerIdxByUserId(userId));
    }

    public String getPlayerIdxByUserId(String userId) {
        if (StringHelper.isNull(userId)) {
            return null;
        }

        return userIdMap.get(userId);
    }

    public Collection<String> getAllPlayerName() {
        return Collections.unmodifiableSet(nameMap.keySet());
    }

    public static final int RANDOM_GET_NAME_MAX_TYR_TIMES = 20;

    public String randomGetName() {
        if (nameMap.isEmpty()) {
            return ObjUtil.createRandomName(LanguageEnum.forNumber(ServerConfig.getInstance().getLanguage()));
        }
        Set<String> nameSet = nameMap.keySet();
        Random random = new Random();
        for (int i = 0; i < RANDOM_GET_NAME_MAX_TYR_TIMES; i++) {
            Optional<String> any = nameSet.stream().skip(random.nextInt(nameSet.size() - 1)).findAny();
            if (any.isPresent()) {
                return any.get();
            }
        }
        LogUtil.error("playerCache.randomGetName, random get name try times max than 20, randomFailed");
        return null;
    }

    @Override
    public void updateDailyData() {
        Map<String, BaseEntity> all = getAll();
        for (BaseEntity entity : all.values()) {
            if (!(entity instanceof playerEntity)) {
                return;
            }
            try {
                playerEntity player = (playerEntity) entity;
                SyncExecuteFunction.executeConsumer(player, e -> player.updateDailyData(player.isOnline()));
            } catch (Exception e) {
                LogUtil.error("playerCache.updateDailyData error by playerId:[{}]", ((playerEntity) entity).getIdx());
                LogUtil.printStackTrace(e);
            }
        }
    }

    public void updateWeekData() {
        Map<String, BaseEntity> all = getAll();
        for (BaseEntity entity : all.values()) {
            if (!(entity instanceof playerEntity)) {
                return;
            }

            playerEntity player = (playerEntity) entity;
            SyncExecuteFunction.executeConsumer(player, e -> player.updateWeeklyData(player.isOnline()));
        }
    }

    public long getOfflineTime(String playerId) {
        playerEntity player = getByIdx(playerId);
        if (player == null || player.isOnline() || player.getLogouttime() == null) {
            return -1;
        }
        return GlobalTick.getInstance().getCurrentTime() - player.getLogouttime().getTime();

    }

    public int queryTodayRecharge(String playerIdx) {
        playerEntity player = getByIdx(playerIdx);
        if (player == null) {
            return 0;
        }
        return player.getDb_data().getTodayRecharge();
    }

    public void clearAllPlayerActivityData(long activityId) {
        LogUtil.info("clear all player activityData on Player, clear id:" + activityId);

        for (BaseEntity value : _ix_id.values()) {
            playerEntity player = (model.player.entity.playerEntity) value;
            SyncExecuteFunction.executeConsumer(player, v -> player.clearActivitiesData(activityId));
        }
    }
}
