package common.load;

import annotation.annationInit;
import clazz.ClassUtil;
import clazz.PackageUtil;
import clazz.classTool;
import com.bowlong.lang.StrEx;
import datatool.StringHelper;
import db.config.AppContext;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import model.base.cache.baseUpdateCache;
import model.global.globalCache;
import util.LogUtil;
import util.TimeUtil;

public class Sysload {

    public static void onServerStart() {

//		// 初始化系统的配置文件
//		SysInitConfig();
//		LogUtil.info("################onServerStart   1 *********");

        // 初始化DAO相关设置
        if (!initGameDAO()) {
            System.exit(0);
        }
        LogUtil.info("################onServerStart   1 *********");
        // 检测数据库是否正确（例如：字段是否添加）
        if (!DbCheck.check()) {
            LogUtil.info(">>>>>>Check DB ERROR>>>>>>>>>>>>>>");
            System.exit(0);
        }
        LogUtil.info("################onServerStart   2*********");
        // 游戏的数据(配置CONFIG文件)加载以及线程启动
        if (!initGameRun()) {
            System.exit(0);
        }
        LogUtil.info("################onServerStart   3*********");
    }

    public static boolean sysInitConfig() {
        LogUtil.info("##################CONFIG INIT START##################");
        boolean result = ServerConfig.getInstance().init();
        LogUtil.info("##################CONFIG INIT END##################");
        return result;
    }

    private static boolean initGameDAO() {
        LogUtil.info("##################initGameDAO START##################");
        List<String> ret;
        try {
            ret = PackageUtil.getClassName("daoMaster");
            if (ret == null || ret.isEmpty()) {
                LogUtil.error("initGameDAO is error>>>>db.daoMaster  null>> " + ret);
                System.exit(0);
                return false;
            }
            LogUtil.info("initGameDAO_ret>>>>>>>>>>>" + ret);
            for (String name : ret) {
                String daoName = ClassUtil.getShortClassName(name);
//				if(!daoname.endsWith("DAO"))
//					continue;

                LogUtil.info("daoName>>>>" + name);
                if (StringHelper.isNull(daoName)) {
                    LogUtil.error("daoName is null>>>>" + name);
                    System.exit(0);
                    return false;
                }

                if (!AppContext.setDao(daoName)) {
                    System.exit(0);
                    return false;
                }
            }

            LogUtil.info("INIT DAO>>>>>>>>>>>>" + AppContext.daos);
            LogUtil.info("################## initGameDAO END##################");
            return true;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            LogUtil.error("######initGameConfig is error"
                    + AppContext.daos);
            LogUtil.error("######initGameConfig is error  END"
                    + e.toString());
            return false;
        }

    }

    /*** initGameRun ************/

    private static boolean initGameRun() {
        try {
            long l1 = System.currentTimeMillis();

            LogUtil.info("initGameRun BeginTime   " + TimeUtil.formatStamp(l1));

            List<Class<?>> classList = PackageUtil.getClasses("model");
            classList.addAll(PackageUtil.getClasses("cfg"));
            if (classList.isEmpty()) {
                LogUtil.error("initGameRun is error>>>>classList is null>> " + classList);
                return false;
            }

            for (Class<?> clazz : classList) {
                // 判断是否为抽象类
                if (Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                // 设置所有操作db的缓存
                if (!setAllCache(clazz)) {
                    return false;
                }

                String methodName = getMethodName(clazz);
                if (StringHelper.isNull(methodName)) {
                    continue;
                }
                LogUtil.info("className:" + clazz.getName() + ",exeMethod:" + methodName);
                if (!classTool.exeMethod(clazz, methodName)) {
                    return false;
                }
                if (!excelInitRealData(clazz)) {
                    return false;
                }
            }

            // PetLog.log.info("globalCache>>>>"+globalCache._allCacheSize);

            long l2 = System.currentTimeMillis();

            LogUtil.info("initGameRun END TIME   " + TimeUtil.formatStamp(l2));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    private static boolean excelInitRealData(Class<?> clazz) throws IllegalAccessException {
        if (!baseConfig.class.isAssignableFrom(clazz)) {
            return true;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().startsWith("_ix_") && Map.class == field.getType()) {
                Map data = (Map) field.get(clazz);
                if (data.size() <= 0) {
                    LogUtil.error("excel data error from " + clazz.getName() + " is empty ,please make sure primary key is configured");
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean setAllCache(Class<?> clazz) {

        if (!baseUpdateCache.class.isAssignableFrom(clazz))
            return true;

        String classname = ClassUtil.getShortClassName(clazz);
        if (StrEx.isSame("baseUpdateCache", classname))
            return true;
        return globalCache.put(classname, clazz);

    }

    private static String getMethodName(Class<?> clazz) {
        annationInit impl = clazz.getAnnotation(annationInit.class);
        if (impl != null)
            return impl.methodname();
        return null;

    }

    private static int getSort(Class<?> clazz) {
        annationInit impl = clazz.getAnnotation(annationInit.class);
        if (impl != null)
            return impl.sort();
        return 0;

    }

    /******** initGameRunEND ************/

    public static void sleep(long elapsed) {
        if (elapsed <= 0) {
            return;
        }
        try {
            Thread.sleep(elapsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
