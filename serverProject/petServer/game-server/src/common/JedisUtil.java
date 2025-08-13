package common;

import com.hyz.platform.jedis.config.RedisClusterProperties;
import com.hyz.platform.jedis.config.RedisConfig;
import com.hyz.platform.jedis.utils.RedisUtils;
import common.load.ServerConfig;
import datatool.StringHelper;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;
import util.LogUtil;

/**
 * @author xiao_FL
 * @date 2019/7/8
 */
public class JedisUtil extends RedisUtils {
    public static JedisUtil jedis;
    private static JedisCluster jedisCluster = null;
    /**
     * 从新获得锁等待时间, ms
     */
    public static final long TRY_LOCK_INTERVAL_TIME = 10000;

    private static final String OK = "OK";


    /**
     * 锁过期时间, ms
     */
    public static final long DEFAULT_LOCK_EXPIRED_TIME = 10000;

    /**
     * 获取锁重试次数
     */
    public static final int DEFAULT_TRY_LOCK_TIMES = 5;


    public JedisUtil(JedisCluster jedisCluster, RedisClusterProperties redisClusterProperties) {
        super(jedisCluster, redisClusterProperties);
    }

    // 初始化Redis连接池
    public static boolean init() {
        initCluster();
        return true;
    }

    public static void initCluster(){
        int timeout = 10000;
        String host = ServerConfig.getInstance().getRedisHost();
        String password = ServerConfig.getInstance().getRedisPassword();
        int database = ServerConfig.getInstance().getRedisDbIndex();
        String clientId = ServerConfig.getInstance().getClientId();
        int serverIndex = ServerConfig.getInstance().getServer();

        int connectionTimeout = 2000;
        int soTimeout = 2000;
        int maxAttempts = 5;
        RedisClusterProperties redisClusterProperties = new RedisClusterProperties(host.split(","),password,database,timeout,connectionTimeout,soTimeout,maxAttempts, clientId,serverIndex);
        redisClusterProperties.setMaxTotal(1024);
        redisClusterProperties.setMaxIdle(200);
        redisClusterProperties.setMaxWait(10000);
        RedisConfig redisConfig = new RedisConfig(redisClusterProperties);
        jedisCluster = redisConfig.getJedisCluster();
        jedis = new JedisUtil(jedisCluster, redisClusterProperties);
    }

    public static boolean lockRedisKey(String key, long expireTime) {
        int serverIndex = ServerConfig.getInstance().getServer();
        SetParams params = SetParams.setParams();
        params.px(expireTime);
        params.nx();
        return OK.equals(jedis.set(key, StringHelper.IntTostring(serverIndex, "0"), params));
    }

    public static boolean tryLockRedisKey(String key, long expireTime, int tryTimes) {
        if (tryTimes <= 0) {
            tryTimes = 1;
        }
        return tryLockWithRetry(key, generateLockParam(), expireTime, tryTimes);
    }

    public static boolean unlockRedisKey(String key) {
        long expireTime = jedis.pttl(key);
        if (expireTime <= 200l) {
            return false;
        }
        int serverIndex = ServerConfig.getInstance().getServer();
        int redisServerIndex = StringHelper.stringToInt(jedis.get(key), 0);
        if (redisServerIndex == serverIndex) {
            jedis.del(key);
            return true;
        }
        return false;
    }


    /**
     * @param lockKey
     * @param supplier
     * @return
     */
    public static <T> T syncExecSupplier(String lockKey, Supplier<T> supplier) {
        return syncExecSupplier(lockKey, DEFAULT_LOCK_EXPIRED_TIME, DEFAULT_TRY_LOCK_TIMES, supplier);
    }

    /**
     * @param lockKey
     * @param booleanSupplier
     * @return
     */
    public static boolean syncExecBooleanSupplier(String lockKey, BooleanSupplier booleanSupplier) {
        return syncExecBooleanSupplier(lockKey, DEFAULT_LOCK_EXPIRED_TIME, DEFAULT_TRY_LOCK_TIMES, booleanSupplier);
    }


    /**
     * @param lockKey  加锁的key
     * @param lockMs   加锁时间
     * @param tryTimes 重试次数
     * @param supplier
     * @return
     */
    public static <T> T syncExecSupplier(String lockKey, long lockMs, int tryTimes, Supplier<T> supplier) {
        if (supplier == null || StringUtils.isBlank(lockKey)) {
            LogUtil.warn("util.JedisUtil.syncExecFunction, error params, supplier:" + supplier + ", lockKey:" + lockKey);
            return null;
        }
        String lockParam = generateLockParam();
        try {
            if (!tryLockWithRetry(lockKey, lockParam, lockMs, tryTimes)) {
                LogUtil.error("util.JedisUtil.syncExecFunction, try lock failed, lock key:" + lockKey);
                //未获取锁不执行
                return null;
            }
            long startExec = Instant.now().toEpochMilli();
            T execResult = supplier.get();
            unlockEntry(lockKey,lockParam);
            assertExecTime(startExec, lockMs, "util.JedisUtil.syncExecFunction");
            //执行成功释放锁资源
            unlockEntry(lockKey, lockParam);
            return execResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成redisLockParam
     *
     * @return
     */
    public static String generateLockParam() {
        return IdGenerator.getInstance().generateId()
                + "_" + Thread.currentThread().getName()
                + "_" + ServerConfig.getInstance().getServer();
    }

    /**
     * 执行时间判断
     *
     * @param startTime
     * @param msg
     */
    public static void assertExecTime(long startTime, long lockMs, String msg) {
        long useTime = Instant.now().toEpochMilli() - startTime;
        if (useTime > lockMs) {
            LogUtil.warn("util.JedisUtil.assertExecTime, method exec time use " + useTime + " max than lockMs:" + lockMs + ",msg" + msg);
        }
    }

    /**
     * @param lockKey   加锁的key
     * @param lockParam 加锁参数(由逻辑控制)
     * @param lockMs    加锁时间
     * @param tryTimes  重试次数
     * @return
     */
    public static boolean tryLockWithRetry(String lockKey, String lockParam, long lockMs, int tryTimes) {
        if (StringUtils.isBlank(lockKey) || StringUtils.isBlank(lockParam)) {
            LogUtil.error("ArenaRedisUtil.tryLock, error params, lockKey:" + lockKey + ", lockParams:" + lockParam);
            return false;
        }
        for (int i = 0; i < tryTimes; i++) {
            try {
                SetParams setParams = SetParams.setParams();
                setParams.px(lockMs);
                setParams.nx();
                boolean result = OK.equals(jedis.set(lockKey, lockParam, setParams));
                if (result) {
                    return true;
                }

                Thread.sleep(TRY_LOCK_INTERVAL_TIME);
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
                //如果报错直接解锁
                unlockEntry(lockKey, lockParam);
            }
        }
        LogUtil.error("util.JedisUtil.tryLock, lock failed , lock key:" + lockKey + ", try lock times:" + tryTimes);
        return false;
    }

    public static boolean unlockEntry(String lockKey, String lockParam) {
        if (StringUtils.isBlank(lockKey) || StringUtils.isBlank(lockParam)) {
            LogUtil.error("ArenaRedisUtil.unlock, error params, lockKey:" + lockKey + ", lockParams:" + lockParam);
            return false;
        }

        try {
            String value = jedis.get(lockKey);
            if (lockParam.equals(value)) {
                jedis.del(lockKey);
                LogUtil.debug("util.JedisUtil.unlock, success delete lock key:" + lockKey + ", lock param:" + lockParam);
                return true;
            } else {
                LogUtil.error("util.JedisUtil.unlock, can not delete lock key:" + lockKey + ", lock param:"
                        + lockParam + ", lock params is not equals, redis value:" + value);
                return false;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    /**
     * 自定参数加锁执行
     *
     * @param lockKey
     * @param lockMs          加锁时间
     * @param tryTimes        重试次数
     * @param booleanSupplier
     * @return
     */
    public static boolean syncExecBooleanSupplier(String lockKey, long lockMs, int tryTimes, BooleanSupplier booleanSupplier) {
        if (booleanSupplier == null || StringUtils.isBlank(lockKey) || lockMs <= 0 || tryTimes <= 0) {
            LogUtil.warn("util.JedisUtil.syncExecConsumer, error params, consumer is null:"
                    + Objects.isNull(booleanSupplier) + ", lockKey:" + lockKey);
            return false;
        }
        String lockParam = generateLockParam();
        try {
            if (!tryLockWithRetry(lockKey, lockParam, lockMs, tryTimes)) {
                LogUtil.error("util.JedisUtil.syncExecConsumer, try lock failed, lock key:" + lockKey);
                //未获取锁不执行
                return false;
            }
            long startExec = Instant.now().toEpochMilli();

            boolean result = booleanSupplier.getAsBoolean();

            assertExecTime(startExec, lockMs, "util.JedisUtil.syncExecConsumer");
            //执行成功释放锁资源
            unlockEntry(lockKey, lockParam);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void hdelAllByKey(String hKey) {
        Set<String> hkeys = jedis.hkeys(hKey);
        if (CollectionUtils.isNotEmpty(hkeys)){
            jedis.hdel(GameConst.RedisKey.TopPlayBSSid,hkeys.toArray(new String[0]));
        }

    }
}
