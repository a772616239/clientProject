package platform.logs.statistics;

import util.ClassUtil;
import util.LogUtil;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @Description  动态日志统计manager
 * @Author hanx
 * @Date2020/10/22 0022 11:11
 **/
public class StatisticsManager {

    private static StatisticsManager instance;

    public static StatisticsManager getInstance() {
        if (instance == null) {
            instance = new StatisticsManager();
        }
        return instance;
    }

    private StatisticsManager() {
    }


    public boolean init() {
        List<Class<AbstractStatistics>> classes = ClassUtil.getSubClass("platform.logs.statistics", AbstractStatistics.class);
        for (Class<AbstractStatistics> clazz : classes) {
            try {
                Method getInstance = clazz.getMethod("getInstance");
                Object instance = getInstance.invoke(null);
                Method init = clazz.getMethod("init");
                init.invoke(instance);
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
                return false;
            }

        }
        return true;

    }

}
