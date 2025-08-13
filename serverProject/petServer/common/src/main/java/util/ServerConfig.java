package util;

import datatool.StringHelper;
import servertool.Config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {
    /**
     * 区服标识
     */
    private Integer server;

    /**
     * 平台登录验证服务地址
     */
    private String platformLogin;

    /**
     * 平台排行榜服务地址
     */
    private String platformRank;

    /**
     * 平台聊天服务地址
     */
    private String platformChat;

    /**
     * 激活码验证服务地址
     */
    private String platformActiveCode;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 平台ip地址验证服务地址
     */
    private String platformIpInfo;

    /**
     * 战斗验证服务地址
     */
    private String battleCheckUrl;

    /**
     * 平台服务请求参数
     */
    private String platformProtocolVersion;

    /**
     * 平台服务请求参数
     */
    private String platformProtocolSalt;

    /**
     * 客户端id
     */
    private String clientId;

    /**
     * redis host
     */
    private String redisHost;

    /**
     * redis port
     */
    private Integer redisPort;

    /**
     * redis password
     */
    private String redisPassword;

    /**
     * redis index
     */
    private Integer redisIndex;

    /**
     * 时区
     */
    private String timeZone;

    /**
     * 系统打印时间
     */
    private Integer systemInfoPrint;

    /**
     * tick
     */
    private Integer tickCycle;

    /**
     * logic tick
     */
    private Integer logicTickCycle;

    /**
     * update tick
     */
    private Integer updateTickCycle;

    public Integer getServer() {
        if (server == null) {
            setServer();
        }
        return server;
    }

    public void setServer() {
        server = ServerDefaultCfg.get("server", 3);
    }

    public String getPlatformLogin() {
        if (platformLogin == null) {
            setPlatformLogin();
        }
        return platformLogin;
    }

    public void setPlatformLogin() {
        platformLogin = ServerDefaultCfg.get("platform.login");
    }

    public String getPlatformRank() {
        if (platformRank == null) {
            setPlatformRank();
        }
        return platformRank;
    }

    public void setPlatformRank() {
        platformRank = ServerDefaultCfg.get("platform.rank");
    }

    public String getPlatformChat() {
        if (platformChat == null) {
            setPlatformChat();
        }
        return platformChat;
    }

    public void setPlatformChat() {
        platformChat = ServerDefaultCfg.get("platform.chat");
    }

    public String getPlatformActiveCode() {
        if (platformActiveCode == null) {
            setPlatformActiveCode();
        }
        return platformActiveCode;
    }

    public void setPlatformActiveCode() {
        platformActiveCode = ServerDefaultCfg.get("platform.activeCode");
    }

    public String getClientSecret() {
        if (clientSecret == null) {
            setClientSecret();
        }
        return clientSecret;
    }

    public void setClientSecret() {
        clientSecret = ServerDefaultCfg.get("clientSecret");
    }

    public String getPlatformIpInfo() {
        if (platformIpInfo == null) {
            setPlatformIpInfo();
        }
        return platformIpInfo;
    }

    public void setPlatformIpInfo() {
        platformIpInfo = ServerDefaultCfg.get("platform.ipInfo");
    }

    public String getBattleCheckUrl() {
        if (battleCheckUrl == null) {
            setBattleCheckUrl();
        }
        return platformIpInfo;
    }

    public void setBattleCheckUrl() {
        battleCheckUrl = ServerDefaultCfg.get("battleCheck.url");
    }

    public String getPlatformProtocolVersion() {
        if (platformProtocolVersion == null) {
            setPlatformProtocolVersion();
        }
        return platformProtocolVersion;
    }

    public void setPlatformProtocolVersion() {
        platformProtocolVersion = ServerDefaultCfg.get("platform.protocol.version");
    }

    public String getPlatformProtocolSalt() {
        if (platformProtocolSalt == null) {
            setPlatformProtocolSalt();
        }
        return platformProtocolSalt;
    }

    public void setPlatformProtocolSalt() {
        platformProtocolSalt = ServerDefaultCfg.get("platform.protocol.salt");
    }

    public String getClientId() {
        if (clientId == null) {
            setClientId();
        }
        return clientId;
    }

    public void setClientId() {
        clientId = ServerDefaultCfg.get("clientId");
    }

    public String getRedisHost() {
        if (redisHost == null) {
            setRedisHost();
        }
        return redisHost;
    }

    public void setRedisHost() {
        redisHost = Config.get("redis.host");
    }

    public int getRedisPort() {
        if (redisPort == null) {
            setRedisPort();
        }
        return redisPort;
    }

    public void setRedisPort() {
        redisPort = StringHelper.stringToInt(Config.get("redis.port"), 6379);
    }

    public String getRedisPassword() {
        if (redisIndex == null) {
            setRedisPassword();
        }
        return redisPassword;
    }

    public void setRedisPassword() {
        redisPassword = Config.get("redis.password");
    }

    public int getRedisIndex() {
        if (redisIndex == null) {
            setRedisIndex();
        }
        return redisIndex;
    }

    public void setRedisIndex() {
        redisIndex = StringHelper.stringToInt(Config.get("redis.index"), 11);
    }

    public String getTimeZone() {
        if (timeZone == null) {
            setTimeZone();
        }
        return timeZone;
    }

    private void setTimeZone() {
        timeZone = Config.get("timeZone");
    }

    public long getSystemInfoPrint() {
        if (systemInfoPrint == null) {
            setSystemInfoPrint();
        }
        return systemInfoPrint;
    }

    private void setSystemInfoPrint() {
        systemInfoPrint = ServerDefaultCfg.get("systemInfoPrint", 0);
    }

    public long getTickCycle() {
        if (tickCycle == null) {
            setTickCycle();
        }
        return tickCycle;
    }

    public void setTickCycle() {
        tickCycle = ServerDefaultCfg.get("tickCycle", 0);
    }

    public long getLogicTickCycle() {
        if (logicTickCycle == null) {
            setLogicTickCycle();
        }
        return logicTickCycle;
    }

    private void setLogicTickCycle() {
        logicTickCycle = ServerDefaultCfg.get("logicTickCycle", 0);
    }

    public long getUpdateTickCycle() {
        if (updateTickCycle == null) {
            setUpdateTickCycle();
        }
        return updateTickCycle;
    }

    private void setUpdateTickCycle() {
        updateTickCycle = ServerDefaultCfg.get("updateTickCycle", 0);
    }

    /**
     * =============================================================================
     */

    private ServerConfig() {

    }

    private static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public static ServerConfig getInstance() {
        return SERVER_CONFIG;
    }

    static class ServerDefaultCfg {
        private static ServerConfig instance;
        private static Properties properties;
        private static final Object LOCK = new Object();

        private ServerDefaultCfg() throws Exception {
            Properties prop = new Properties();
            String pro = System.getProperty("user.dir") + "/" + "app.properties";
            InputStream in = new BufferedInputStream(new FileInputStream(pro));
            prop.load(in);
            in.close();
            properties = prop;
        }

        public static ServerConfig getInstance() throws Exception {
            if (instance == null) {
                synchronized (LOCK) {
                    if (instance != null) {
                        instance = new ServerConfig();
                    }
                }
            }
            return instance;
        }

        private static String getAppRoot() {
            return System.getProperty("user.dir");
        }

        private static String get(String key) {
            String result = properties.getProperty(key);
            if (result != null) {
                result = result.trim();
            }
            return result;
        }

        private static Integer get(String key, Integer def) {
            String result = properties.getProperty(key, def.toString());
            if (result != null) {
                result = result.trim();
                return Integer.parseInt(result);
            }
            return def;
        }
    }
}
