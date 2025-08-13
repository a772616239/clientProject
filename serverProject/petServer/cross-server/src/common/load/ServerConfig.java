package common.load;

import common.HttpRequestUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import servertool.ServerCommonConfig;
import util.GameUtil;
import util.LogUtil;

@Getter
public class ServerConfig extends ServerCommonConfig {

    @PropertySkip
    private final String propertiesPath = "./app.properties";

    @PropertySkip
    public static final String LIST_SPLIT = ",";

    @PropertySkip
    private Properties properties;

    @PropertySkip
    private static ServerConfig instance = new ServerConfig();

    private ServerConfig() {

    }

    public static ServerConfig getInstance() {
        return instance;
    }

    @PropertyValue("gameConfig.TimeTickCycle")
    private long timeTickCycle;

    @PropertyValue("gameConfig.MistSnapShotCycle")
    private long mistSnapShotCycle;

    @PropertyValue("gameConfig.PrintServerInfoCycle")
    private long printSvrInfoCycle;

    @PropertyValue("ip")
    private String ip;

    @PropertyValue("maxOnlinePlayerNum")
    private int maxOnlinePlayerNum;

    @PropertyValue("logsaddress")
    private String logsAddress;

    @PropertyValue("gameConfig.threadCount")
    private int threadCount;

    @PropertyValue("handlerPath")
    private String handlerPath;

    @PropertyValue("jsonPath")
    private String jsonPath;

    @PropertyValue("gameConfig.mineFightServer")
    private boolean isMineFightServer;

    @PropertyValue("gameConfig.isArenaServer")
    private boolean isArenaServer;

    @PropertyValue("debug")
    private boolean debug;

    @PropertyValue("gameConfig.isMainCross")
    private boolean isMainCross;

    @PropertyValue("clientId")
    private String clientId;

    @PropertyValue("clientSecret")
    private String clientSecret;

    @PropertyValue("platform.protocol.salt")
    private String platformProtocolSalt;

    @PropertyValue("platform.protocol.version")
    private String platformProtocolVersion;

    @PropertyValue("timeZone")
    private String timeZone;

    @PropertyValue("gameConfig.ArenaEventTickCycle")
    private long arenaEventTickCycle;

    @CheckHttpConnect
    @PropertyValue("platform.ranking.update")
    private String platformRankUpdate;

    @CheckHttpConnect
    @PropertyValue("platform.ranking.query")
    private String platformRankPage;

    @CheckHttpConnect
    @PropertyValue("platform.ranking.clear")
    private String platformRankClear;

    /**
     * ==========================redis start ============
     */

    @PropertyValue("redis.host")
    private String redisHost;


    @PropertyValue("redis.password")
    private String redisPassword;

    @PropertyValue(value = "redis.index", allowZero = true)
    private int redisDbIndex;

    /**
     * ==========================redis end ============
     */

    public boolean init() {
        initSuperConfig();
        if (!loadProperties() || !initAllConfig()) {
            return false;
        }

        LogUtil.info("ServerConfig.init finished, details:" + toString());
        return checkConfig() && checkHttpConnect();
    }

    private void initSuperConfig() {
        setPort();
        setServer();
        setToday();
        setLanguage();
        setMaxActive();
        setMaxIdle();
        setMaxWait();
        setHost();
        setRedisport();
        setPassword();
        setRediskey();
    }

    private boolean initAllConfig() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(PropertySkip.class)) {
                LogUtil.warn("ServerConfig.initAllConfig, filed:" + field.getName() +
                        ", have annotation with PropertySkip, skip init this filed");
                continue;
            }

            if (!field.isAnnotationPresent(PropertyValue.class)) {
                LogUtil.error("ServerConfig.initAllConfig, filed:" + field.getName() +
                        ", have not annotation with PropertyValue, please check cfg");
                return false;
            }

            String property = field.getAnnotation(PropertyValue.class).value();
            if (!setFiledValue(field, getProperty(property))) {
                LogUtil.error("common.load.ServerConfig.initAllConfig, set filed value failed, filed name:" + field);
                return false;
            }
        }
        return true;
    }

    /**
     * 初始化属性配置
     *
     * @return
     */
    private boolean loadProperties() {
        if (this.properties == null) {
            this.properties = new Properties();
        }
        try {
            this.properties.load(new InputStreamReader(new FileInputStream(propertiesPath), StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获得配置的属性
     *
     * @param name
     * @return
     */
    public String getProperty(String name) {
        Object property = this.properties.get(name);
        return property == null ? null : property.toString();
    }


    /**
     * 检查配置是否为空或者值未设置
     *
     * @return
     */
    private boolean checkConfig() {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                PropertyValue annotation = field.getAnnotation(PropertyValue.class);
                if (annotation == null) {
                    LogUtil.info("ServerConfig.checkConfig, filed name :" + field.getName() + " annotation with out PropertySkip");
                    continue;
                }

                if (annotation.allowNull()) {
                    continue;
                }

                Object value = field.get(this);
                if (value == null) {
                    LogUtil.error("common.load.ServerConfig.checkConfig, filed:" + field.getName() + ", value is null");
                    return false;
                } else if (Objects.equals(field.getType(), Integer.class)
                        || (Objects.equals(field.getType(), int.class))) {
                    if (annotation.allowZero()) {
                        continue;
                    }

                    if ((int) value == 0) {
                        LogUtil.error("common.load.ServerConfig.checkConfig, filed:" + field.getName() + ", value is 0");
                        return false;
                    }
                } else if (Objects.equals(field.getType(), Long.class)
                        || (Objects.equals(field.getType(), long.class))) {
                    if (annotation.allowZero()) {
                        continue;
                    }

                    if ((long) value == 0) {
                        LogUtil.error("common.load.ServerConfig.checkConfig, filed:" + field.getName() + ", value is 0");
                        return false;
                    }
                } else if (Objects.equals(field.getType(), List.class)) {
                    if (CollectionUtils.size(value) <= 0) {
                        LogUtil.error("common.load.ServerConfig.checkConfig, filed:" + field.getName() + ", value is empty");
                        return false;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            LogUtil.printStackTrace(e);
            return false;
        }
        return true;
    }

    private boolean setFiledValue(Field field, String configValue) {
        if (field == null || StringUtils.isBlank(configValue)) {
            LogUtil.error("common.load.ServerConfig.setFiledValue, failed:" + field
                    + ", or config value：" + configValue);
            return false;
        }

        try {
            //设置访问权限
            field.setAccessible(true);

            if (Objects.equals(field.getType(), String.class)) {
                field.set(this, configValue);

            } else if (Objects.equals(field.getType(), Integer.class)
                    || Objects.equals(field.getType(), int.class)) {
                field.set(this, Integer.valueOf(configValue));

            } else if (Objects.equals(field.getType(), Long.class)
                    || Objects.equals(field.getType(), long.class)) {
                field.set(this, Long.valueOf(configValue));

            } else if (Objects.equals(field.getType(), Boolean.class)
                    || Objects.equals(field.getType(), boolean.class)) {
                field.set(this, Boolean.valueOf(configValue));

            } else if (Objects.equals(field.getType(), List.class)) {
                String[] split = configValue.split(LIST_SPLIT);
                field.set(this, GameUtil.parseArrayToList(split));

            } else {
                LogUtil.warn("ServerConfig.setFiledValue, unsupported set type, "
                        + field.getType().getSimpleName() + "please coding map relation");
                return true;
            }
        } catch (IllegalAccessException e) {
            LogUtil.printStackTrace(e);
            return false;
        }
        return true;
    }

    private boolean checkHttpConnect()  {
        Field[] fields = this.getClass().getDeclaredFields();
        LogUtil.info("##################before http connect test start##################");
        for (Field field : fields) {
            if (field.isAnnotationPresent(CheckHttpConnect.class)){
                String url = null;
                try {
                    url = (String) field.get(this);
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
                HttpRequestUtil.doPostConnectTest(url, "");
            }
        }
        LogUtil.info("##################before http connect test end##################");
        return true;
    }
}
