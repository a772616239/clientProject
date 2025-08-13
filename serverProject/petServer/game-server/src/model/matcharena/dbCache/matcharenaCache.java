/*CREATED BY TOOL*/

package model.matcharena.dbCache;

import annotation.annationInit;
import com.hyz.platform.sdk.utils.string.StringUtils;
import common.GameConst;
import common.GameConst.RedisKey;
import static common.JedisUtil.jedis;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.matcharena.MatchArenaUtil;
import model.matcharena.cache.matcharenaUpdateCache;
import model.matcharena.entity.matcharenaEntity;
import model.player.util.PlayerUtil;
import protocol.MatchArenaDB.RedisMatchArenaPlayerInfo;

@annationInit(value = "matcharenaCache", methodname = "load")
public class matcharenaCache extends baseCache<matcharenaCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static matcharenaCache instance = null;

    public static matcharenaCache getInstance() {

        if (instance == null) {
            instance = new matcharenaCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "matcharenaDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("matcharenaDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (matcharenaCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(matcharenaEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static matcharenaEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (matcharenaEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return matcharenaUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        matcharenaEntity t = (matcharenaEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    public matcharenaEntity getEntity(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }

        matcharenaEntity entity = getByIdx(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new matcharenaEntity();
            entity.setIdx(playerIdx);

            put(entity);
        }
        return entity;
    }

    /**
     * 更新玩家的baseinfo和teaminfo到redis
     *
     * @param playerIdx
     * @param curArenaType
     */
    public void updatePlayerInfoToRedis(String playerIdx, GameConst.ArenaType curArenaType) {
        RedisMatchArenaPlayerInfo playerInfo = MatchArenaUtil.buildRedisPlayerInfo(playerIdx,curArenaType);
        if (playerInfo == null) {
            return;
        }

        jedis.hset(RedisKey.MatchArenaPlayerInfo.getBytes(StandardCharsets.UTF_8),
                playerIdx.getBytes(StandardCharsets.UTF_8), playerInfo.toByteArray());

        matcharenaCache.getInstance().updatePlayerScoreToRedis(playerIdx);
    }


    public void updatePlayerScoreToRedis(String playerIdx) {
        matcharenaEntity entity = getEntity(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, matcharenaEntity::updateScoreToRedis);
        }
    }
}
