package util;

import com.hyz.platform.jedis.config.RedisClusterProperties;
import com.hyz.platform.jedis.config.RedisConfig;
import com.hyz.platform.jedis.utils.RedisUtils;
import common.load.ServerConfig;
import datatool.StringHelper;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

public class JedisUtil extends RedisUtils {
    public static JedisUtil jedis;
    private static JedisCluster jedisCluster = null;
    private static final String OK = "OK";

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

}
