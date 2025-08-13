/*CREATED BY TOOL*/

package model.mainLine.dbCache;

import annotation.annationInit;
import common.GlobalData;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import entity.UpdateDailyData;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.mainLine.cache.mainlineUpdateCache;
import model.mainLine.entity.mainlineEntity;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import protocol.Common.Reward;
import util.LogUtil;

@annationInit(value = "mainlineCache", methodname = "load")
public class mainlineCache extends baseCache<mainlineCache> implements IbaseCache, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static mainlineCache instance = null;

    public static mainlineCache getInstance() {

        if (instance == null) {
            instance = new mainlineCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "mainlineDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("mainlineDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (mainlineCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(mainlineEntity v) {
        getInstance().putBaseEntity(v);
    }

    public static mainlineEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (mainlineEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return mainlineUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {
        mainlineEntity t = (mainlineEntity) v;
        String linkPlayerIdx = t.getLinkplayeridx();
        if (linkPlayerIdx != null) {
            mainLineMap.put(linkPlayerIdx, t);
        }

    }

    /*******************MUST HAVE END ********************************/

    private static Map<String, mainlineEntity> mainLineMap = new ConcurrentHashMap<>();

    public mainlineEntity getMainLineEntityByPlayerIdx(String playerIdx) {
        if (playerIdx == null) {
            return null;
        }

        mainlineEntity entity = mainLineMap.get(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new mainlineEntity(playerIdx);
            put(entity);
        }

        return entity;
    }

    /**
     * 获取玩家的当前关卡,
     *
     * @param playerIdx
     * @return
     */
    public int getPlayerCurCheckPoint(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return -1;
        }

        mainlineEntity entity = getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return -1;
        }
        return entity.getDBBuilder().getMainLinePro().getCurCheckPoint();
    }

    /**
     * 获取当前玩家的所在节点，取当前挂机节点
     *
     * @param playerIdx
     * @return
     */
    public int getPlayerCurNode(String playerIdx) {
        mainlineEntity entity = getMainLineEntityByPlayerIdx(playerIdx);
        int curNode = 0;
        if (entity != null) {
            curNode = entity.getDBBuilder().getOnHookIncome().getCurOnHookNode();
        }
        return Math.max(curNode, 1);
    }

    /**
     * 获取玩家当前正在挂机的节点
     *
     * @param playerIdx
     * @return
     */
    public int getCurOnHookNode(String playerIdx) {
        int node = 1;
        if (StringUtils.isBlank(playerIdx)) {
            return node;
        }
        mainlineEntity entity = getMainLineEntityByPlayerIdx(playerIdx);
        node = entity == null ? 1 : entity.getDBBuilder().getOnHookIncome().getCurOnHookNode();
        return Math.max(node, 1);
    }

    /**
     * 挂机完成时间
     *
     * @param playerIdx
     * @return 负数为还差多少时间完成, 整数已完成时间
     */
    public long OnHookCompleteTime(String playerIdx) {
        mainlineEntity entity = getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return -Integer.MAX_VALUE;
        }
        return entity.OnHookCompleteTime();
    }

    @Override
    public void updateDailyData() {
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof mainlineEntity)) {
                continue;
            }

            mainlineEntity entity = (mainlineEntity) value;
            SyncExecuteFunction.executeConsumer(entity,
                    e -> e.updateDailyData(GlobalData.getInstance().checkPlayerOnline(e.getLinkplayeridx())));
        }
    }

    public List<Reward> calculateOnHookRewards(String playerIdx, long validTime) {
        if (StringUtils.isEmpty(playerIdx) || validTime <= 0) {
            return null;
        }

        mainlineEntity entity = getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null || !entity.canQuickOnHook()) {
            LogUtil.error("model.mainLine.dbCache.mainlineCache.calculateOnHookRewards, player can not onHook, playerIdx:" + playerIdx);
            return null;
        }
        return entity.calculateOnHookReward(validTime);
    }

    public long findMaxPassAbility(String playerIdx) {
        mainlineEntity entity = getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return 0;
        }
        return entity.getDBBuilder().getMaxPassAbility();

    }

    public boolean isClosePlot(String playerIdx) {
        mainlineEntity entity = getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return true;
        }
        return entity.getDBBuilder().getClosePlot();
    }
}
