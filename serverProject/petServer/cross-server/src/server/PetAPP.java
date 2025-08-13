package server;

import clazz.PackageUtil;
import common.GlobalThread;
import common.GlobalTick;
import common.HttpRequestUtil;
import common.IdGenerator;
import common.entity.RankingScore;
import common.entity.RankingUpdateRequest;
import common.load.ServerConfig;
import common.load.Sysload;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.TimeUnit;
import model.arena.ArenaManager;
import model.cacheprocess.baseUapteCacheL;
import model.thewar.TheWarManager;
import model.timer.dbCache.timerCache;
import server.event.EventManager;
import server.net.GameServerBootstrap;
import util.GameUtil;
import util.JedisUtil;
import util.LogUtil;
import util.TimeUtil;

public class PetAPP {
    static final Runtime runtime = Runtime.getRuntime();

    public static boolean loadFinish = false;

    public static void main(String[] args) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            InstantiationException {
        long l1 = System.currentTimeMillis();
        try {
            if (!initServer()) {
                System.exit(0);
                return;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }

        long l2 = System.currentTimeMillis();

        StringBuffer sb = new StringBuffer();
        sn(sb, "");
        sn(sb, "/////////////////////////////////////////");
        sn(sb, "// Server Port   :%s", ServerConfig.getInstance().getPort() + "");

        sn(sb,
                "// Used Memory   :%s",
                ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024))
                        + "MB");
        sn(sb, "// Free Memory       :%s",
                ((getFreeMem(runtime)) / (1024 * 1024)) + "MB");
        sn(sb, "// Total Memory          :%s",
                ((runtime.maxMemory()) / (1024 * 1024)) + "MB");
        sn(sb, "// Begin Time        :%s", TimeUtil.formatStamp(l1) + "");
        sn(sb, "// Used Time           :%s", GameUtil.useTime(l1, l2));
        sn(sb, "// Server Star Time     :%s", TimeUtil.formatStamp(l2) + "");
        sn(sb, "/////////////////////////////////////////");
        LogUtil.info(sb.toString());
        LogUtil.info("################onServerStart   9-1*********");

//		test();

    }

    public static void sn(StringBuffer sb, String format, String... strs) {
        String msg = String.format(format, strs) + "\n";
        sb.append(msg);
    }

    private static long getFreeMem(final Runtime runtime) {
        return runtime.maxMemory() - runtime.totalMemory()
                + runtime.freeMemory();
    }

    protected static boolean initServer() {
        try {
            // load ServerConfig
            Sysload.SysInitConfig();

            ServerConfig config = ServerConfig.getInstance();
            if (!config.init()) {
                LogUtil.error("*****init serverConfig failed*****");
                return false;
            }
            // ServerBoot

            if (!GlobalThread.getInstance().init(config.getThreadCount())) {
                LogUtil.error("*****GlobalThread init failed*****");
                return false;
            }
            if (!JedisUtil.init()) {
                LogUtil.error("*****JedisUtil init failed*****");
                return false;
            }

            GlobalTick.getInstance().start();
            if (!EventManager.getInstance().init()) {
                LogUtil.error("*****EventManager init failed*****");
                return false;
            }

            if (!IdGenerator.getInstance().init(config.getServer())) {
                LogUtil.error("IdGenerator init error");
                return false;
            }

            /**
             * 检查配置
             */
            if (!checkCfg()) {
                LogUtil.error("check cfg failed");
                return false;
            }

            //load Game
            Sysload.onServerStart();

            if (!ArenaManager.getInstance().init()) {
                LogUtil.error("ArenaManager init error");
                return false;
            }

            if (!timerCache.getInstance().init()) {
                LogUtil.error("ArenaManager init error");
                return false;
            }
            if (!TheWarManager.getInstance().init()) {
                LogUtil.error("TheWarManager init error");
                return false;
            }
            // ServerBoot 初始化完成后开启
            if (!GameServerBootstrap.start(config, false, true)) {
                LogUtil.error("****APP SERVER START IS ERROR*********");
                return false;
            }

            loadFinish = true;
            LogUtil.info("**************CrossServer init success*********");
            addShutDownHook();
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    private static boolean checkCfg() {
        return checkRankingCfg();
    }

    private static final String CHECK_RANKING_NAME = "rankingTest";

    private static boolean checkRankingCfg() {
        try {
            RankingUpdateRequest updateRequest = new RankingUpdateRequest(CHECK_RANKING_NAME, false);
            updateRequest.addItems(new RankingScore("testIdx", 10));
            if (!HttpRequestUtil.updateRanking(updateRequest)) {
                LogUtil.error("can not update test ranking");
                return false;
            }
            if (!HttpRequestUtil.clearRanking(CHECK_RANKING_NAME, ServerConfig.getInstance().getServer())) {
                LogUtil.error("clear test ranking failed");
                return false;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
        return true;
    }

    public static void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    ArenaManager.getInstance().stopArenaTick();
                    GlobalThread.getInstance().getExecutor().shutdown();
                    if (GlobalThread.getInstance().getExecutor().awaitTermination(5, TimeUnit.SECONDS)) {
                        GlobalThread.getInstance().getExecutor().shutdownNow();
                    }
                } catch (InterruptedException e) {
                    LogUtil.printStackTrace(e);
                }
                shutDownAllModel();
                LogUtil.info("Crossserver close finished");
            }
        });
    }

    protected static void shutDownAllModel() {
        List<Class<?>> classList = PackageUtil.getClasses("model");
        if (classList == null || classList.isEmpty()) {
            LogUtil.error("cross server shutdown Model is null");
            return;
        }
        try {
            for (Class<?> clazz : classList) {
                if (Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                if (!clazz.getPackage().getName().endsWith("cache")) {
                    continue;
                }
                if (!clazz.getName().endsWith("CacheL")) {
                    continue;
                }
                Method getInstance = clazz.getMethod("getInstance");
                if (getInstance == null) {
                    continue;
                }
                baseUapteCacheL instance = (baseUapteCacheL) getInstance.invoke(null);
                if (instance == null) {
                    continue;
                }
                LogUtil.info("server shut save instance=" + instance.getClass().getSimpleName());
                instance.dealUpdateCache();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
