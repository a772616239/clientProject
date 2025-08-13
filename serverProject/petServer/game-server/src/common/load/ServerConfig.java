package common.load;

import common.HttpRequestUtil;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import servertool.ServerCommonConfig;
import sun.net.util.IPAddressUtil;
import util.GameUtil;
import util.LogUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;


/**
 * 暂时只支持一下类型， int,Integer,long,Long,boolean,Boolean,List
 */

@ToString
@Getter
public class ServerConfig extends ServerCommonConfig {

    @PropertySkip
    private final String propertiesPath = "./app.properties";

    @PropertySkip
    private Properties properties;

    @PropertySkip
    public static final String LIST_SPLIT = ",";

    @PropertySkip
    private static ServerConfig instance = new ServerConfig();

    private ServerConfig() {
    }

    public static ServerConfig getInstance() {
        return instance;
    }


    @PropertyValue("sensiWordsLanguage")
    private String sensiWordsLanguage;


    @PropertyValue("ip")
    private String ip;

    @PropertyValue("logsaddress")
    private String logsAddress;

    @PropertyValue("maxOnlinePlayerNum")
    private int maxOnlinePlayerNum;

    @PropertyValue("gameConfig.threadCount")
    private int threadCount;

    @PropertyValue("handlerPath")
    private String handlerPath;

    @PropertyValue("jsonPath")
    private String jsonPath;

    @PropertyValue("http.serverPort")
    private int httpPort;

    @PropertyValue("http.platform.purchase")
    private String httpPlatformPurchase;

    @PropertyValue("http.platform.activity")
    private String httpPlatformActivity;

    @PropertyValue("http.platform.chat")
    private String httpPlatformChat;

    @PropertyValue("GM")
    private boolean GM;

    @PropertyValue("debug")
    private boolean debug;

    @PropertyValue("maxRankingSize")
    private int maxRankingSize;

    @PropertyValue("timeZone")
    private String timeZone;

    @PropertyValue("tickCycle")
    private long tickCycle;

    @PropertyValue("logicTickCycle")
    private long logicTickCycle;

    @PropertyValue("updateTickCycle")
    private long updateTickCycle;

    @PropertyValue("systemInfoPrint")
    private long systemInfoPrint;

    @PropertyValue("singleRun")
    private boolean singleRun;

    @PropertyValue("zone")
    private String zone;

    @PropertyValue("battleCheck.open")
    private boolean openBattleCheck;

    @PropertyValue("battleCheck.url")
    @CheckHttpConnect
    private String battleCheckUrl;

    @PropertyValue("battleCheck.tickCycle")
    private int battleTickCycle;

    @PropertyValue("battleCheck.canGmEndBattle")
    private boolean canGmEndBattle;

    /**
     * ==========================platform start ============
     */

    @PropertyValue("platform.protocol.salt")
    private String platformProtocolSalt;

    @PropertyValue("platform.protocol.version")
    private String platformProtocolVersion;

    @PropertyValue("platform.login")
    private String platformLogin;

    @PropertyValue("platform.extraInfo")
    private String platformExtraInfo;

    @PropertyValue("platform.ranking.update")
    @CheckHttpConnect
    private String platformRankUpdate;

    @PropertyValue("platform.ranking.query")
    @CheckHttpConnect
    private String platformRankPage;

    @PropertyValue("platform.ranking.clear")
    @CheckHttpConnect
    private String platformRankClear;

    @PropertyValue("platform.activeCode")
    @CheckHttpConnect
    private String platformActiveCode;

    @PropertyValue("platform.chat")
    private String platformChat;

    @PropertyValue("platform.ipInfo")
    @CheckHttpConnect
    private String platformIpInfo;

    @PropertyValue("platform.anti.login")
    @CheckHttpConnect
    private String platformAntiLogIn;

    @PropertyValue("platform.anti.logout")
    @CheckHttpConnect
    private String platformAntiLogOut;

    @PropertyValue("platform.activity.pull")
    @CheckHttpConnect
    private String platformActivityPull;

    @PropertyValue("platform.activity.pullReturn")
    @CheckHttpConnect
    private String platformActivityPullReturn;

    @PropertyValue("platform.activityNotice.pull")
    @CheckHttpConnect
    private String platformActivityNoticePull;

    @PropertyValue("platform.activityNotice.pullReturn")
    @CheckHttpConnect
    private String platformActivityNoticePullReturn;

    @PropertyValue("platform.activeCode")
    @CheckHttpConnect
    private String activeCodeUrl;

    @CheckHttpConnect
    @PropertyValue("platform.newChat")
    private String platformNewChat;

    @CheckHttpConnect
    @PropertyValue("platform.push.tadAdd")
    private String pushTagAdd;

    @CheckHttpConnect
    @PropertyValue("platform.push.notification")
    private String pushNotification;

    @CheckHttpConnect
    @PropertyValue("platform.push.tadDelete")
    private String pushTagDelete;

    @CheckHttpConnect
    @PropertyValue("platform.appsflyer.login")
    private String platformAppsflyerLogin;

    @CheckHttpConnect
    @PropertyValue("platform.appsflyer.level")
    private String platformAppsflyerLevel;

    @CheckHttpConnect
    @PropertyValue("platform.appsflyer.tutorial")
    private String platformAppsflyerTutorial;

    @CheckHttpConnect
    @PropertyValue("platform.appsflyer.watchAdFinish")
    private String platformAppsflyerWatchAdFinish;

    /**
     * ==========================platform end ============
     */


    /**
     * ==========================redis start ============
     */

    @PropertyValue(value = "redis.index", allowZero = true)
    private int redisDbIndex;

    @PropertyValue("redis.host")
    private String redisHost;

    @PropertyValue("redis.password")
    private String redisPassword;

    /**
     * ==========================redis end ============
     */

    @PropertyValue("clientId")
    private String clientId;

    @PropertyValue("clientSecret")
    private String clientSecret;

    @PropertyValue("gamePlayUpdateCycle")
    private long gamePlayUpdateCycle;

    @PropertyValue("platformLogBaseDir")
    private String platformLogBaseDir;

    @PropertyValue("platformLogSaveInterval")
    private long platformLogSaveInterval;

    @PropertyValue("platformPageMaxSize")
    private int platformPageMaxSize;

    @PropertyValue("http.containLocal")
    private boolean containLocal;

    @PropertyValue("http.allowIps")
    private final List<String> allowIps = new ArrayList<>();


    /**
     * ===============================httpServer================================
     */

    @PropertyValue("httpServer.ban")
    private String httpServerBan;

    @PropertyValue("httpServer.cancelBan")
    private String httpServerCancelBan;

    @PropertyValue("httpServer.ban.kickOut")
    private String httpServerBanKickOut;

    @PropertyValue("httpServer.mail.add")
    private String httpServerMailAdd;

    @PropertyValue("httpServer.mail.delete")
    private String httpServerMailDelete;

    @PropertyValue("httpServer.mail.cancel")
    private String httpServerMailCancel;

    @PropertyValue("httpServer.player.baseInfo")
    private String httpServerPlayerBaseInfo;

    @PropertyValue("httpServer.player.details")
    private String httpServerPlayerDetails;

    @PropertyValue("httpServer.player.crossArenaInfo")
    private String crossArenaInfoList;

    @PropertyValue("httpServer.player.rune")
    private String httpServerPlayerRune;


    @PropertyValue("httpServer.player.gem")
    private String httpServerPlayerGem;

    @PropertyValue("httpServer.player.pet")
    private String httpServerPlayerPet;

    @PropertyValue("httpServer.activityNotice.add")
    private String httpServerActivityNoticeAdd;

    @PropertyValue("httpServer.activityNotice.delete")
    private String httpServerActivityNoticeDelete;

    @PropertyValue("httpServer.report.query")
    private String httpServerReportQuery;

    @PropertyValue("httpServer.report.operate")
    private String httpServerReportOperate;

    @PropertyValue("httpServer.report.autoDeal.cancelBan")
    private String httpServerReportAutoDealCancelBan;

    @PropertyValue("httpServer.comment.queryUnReport")
    private String httpServerQueryUnReportedComment;

    @PropertyValue("httpServer.marquee.add")
    private String httpServerMarqueeAdd;

    @PropertyValue("httpServer.marquee.delete")
    private String httpServerMarqueeDelete;

    @PropertyValue("httpServer.player.skipNewBeeGuide")
    private String playerSkipNewBeeGuide;

    @PropertyValue("httpServer.newChat")
    private String httpServerNewChat;


    @PropertyValue("httpServer.statistics.artifact")
    private String artifactStatistics;

    @PropertyValue("httpServer.statistics.endlessSpire")
    private String endlessSpireStatistics;

    @PropertyValue("httpServer.statistics.petGem")
    private String petGemStatistics;

    @PropertyValue("httpServer.statistics.pet")
    private String petStatistics;

    @PropertyValue("httpServer.statistics.petRune")
    private String petRuneStatistics;

    @PropertyValue("platform.function.findAll")
    private String functionFindAll;

    @PropertyValue("httpServer.player.queryPlayerOwnedWarGrids")
    private String queryPlayerOwnedWarGrids;

    @PropertyValue("httpServer.train.query")
    private String queryTrainPlayerData;

    @PropertyValue("httpServer.train.rank")
    private String queryTrainRank;

    /**
     * ===============================httpServer================================
     */

    public boolean init() {
        initSuperConfig();
        if (!loadProperties() || !initAllConfig()) {
            return false;
        }
        specialInitFiled();
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

    private void specialInitFiled() {
        setAllowIps();
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
                    LogUtil.info("ServerConfig.checkConfig, filed name :" + field.getName() + " annotation with out PropertyValue");
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


    private boolean checkHttpConnect() {
        Field[] fields = this.getClass().getDeclaredFields();
        LogUtil.info("##################before http connect test start##################");
        for (Field field : fields) {
            if (field.isAnnotationPresent(CheckHttpConnect.class)) {
                String url = null;
                try {
                    url = (String) field.get(this);
                    HttpRequestUtil.doPostConnectTest(url, "");
                } catch (Exception e) {
                    LogUtil.error("common.load.ServerConfig.checkHttpConnect, fileName:" + field.getName() + ":url:" + url);
                    LogUtil.printStackTrace(e);
                }
            }
        }
        LogUtil.info("##################before http connect test end##################");
        return true;
    }


    public void setAllowIps() {
        if (isContainLocal()) {
            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                if (networkInterfaces != null) {
                    while (networkInterfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = networkInterfaces.nextElement();
                        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                        while (inetAddresses.hasMoreElements()) {
                            InetAddress inetAddress = inetAddresses.nextElement();
                            String hostAddress = inetAddress.getHostAddress();
                            if (IPAddressUtil.isIPv4LiteralAddress(hostAddress) || IPAddressUtil.isIPv6LiteralAddress(hostAddress)) {
                                allowIps.add(hostAddress);
                                LogUtil.info("localHost = " + inetAddress.getHostAddress());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
    }
}
