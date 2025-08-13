package server;

import clazz.subClassUtil;
import common.GlobalThread;
import common.GlobalTick;
import common.IdGenerator;
import common.TimeUtil;
import common.load.ServerConfig;
import common.load.Sysload;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.Iterator;
import java.util.Set;
import model.matchArena.MatchArenaNormalManager;
import model.matchArena.MatchArenaRankManager;
import org.apache.commons.lang.time.DateFormatUtils;
import server.event.EventManager;
import server.net.GameServerBootstrap;
import util.GameUtil;
import util.JedisUtil;
import util.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

public class PetAPP {
	static final Runtime runtime = Runtime.getRuntime();

	public static void main(String[] args) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			InstantiationException {
		long l1 = System.currentTimeMillis();

		if (!initServer()) {
			System.exit(0);
			return;
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
		sn(sb, "// Begin Time        :%s", DateFormatUtils.format(l1, TimeUtil.DEFAULT_TIME_FORMAT) + "");
		sn(sb, "// Used Time           :%s", GameUtil.useTime(l1, l2));
		sn(sb, "/" +
				"/ Server Star Time     :%s", DateFormatUtils.format(l2, TimeUtil.DEFAULT_TIME_FORMAT) + "");
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

			//load Game
			Sysload.onServerStart();

            // ServerBoot 初始化完成后开启
            if (!GameServerBootstrap.start(config, false, true)) {
                LogUtil.error("****APP SERVER START IS ERROR*********");
                return false;
            }

			if (!MatchArenaNormalManager.getInstance().init()) {
				LogUtil.error("MatchArenaNormalManager init error");
				return false;
			}
			if (!MatchArenaRankManager.getInstance().init()) {
				LogUtil.error("MatchArenaRankManager init error");
				return false;
			}

			addShutDownHook();
			return true;
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			return false;
		}
	}


	public static void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					GlobalThread.getInstance().getExecutor().shutdown();
					if (GlobalThread.getInstance().getExecutor().awaitTermination(5, TimeUnit.SECONDS)) {
						GlobalThread.getInstance().getExecutor().shutdownNow();
					}
				} catch (InterruptedException e) {
					LogUtil.printStackTrace(e);
				}
				LogUtil.info("Battleserver close finished");
			}
		});
	}
}
