package util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author xiao_FL
 * @date 2019/7/8
 */
public class JedisUtil {
    private static JedisPool pool = null;

    // 初始化Redis连接池
    static {
        int timeout = 10000;
        try {
            // Redis服务配置
            JedisPoolConfig config = new JedisPoolConfig();
            // 可用连接实例的最大数目，默认值为8
            // 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)
            config.setMaxTotal(1024);
            // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值8
            config.setMaxIdle(200);
            // 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException
            config.setMaxWaitMillis(10000);
            // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的
            config.setTestOnBorrow(true);

            // Redis服务器配置
            // Redis服务器ip
            String ip = ServerConfig.getInstance().getRedisHost();
            //Redis的端口号
            int port = ServerConfig.getInstance().getRedisPort();
            // 访问密码
            String password = ServerConfig.getInstance().getRedisPassword();
            // 指定库
            int index = ServerConfig.getInstance().getRedisIndex();
            pool = new JedisPool(config, ip, port, timeout, password, index);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * 返还资源
     *
     * @param jedis redis操作对象
     */
    public static void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * 获得资源
     *
     * @return redis操作对象
     */
    public static Jedis getResource() {
        return pool.getResource();
    }

}
