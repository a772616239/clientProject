package petrobot.robot.index;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import petrobot.robot.RobotConfig;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.util.ClassUtil;
import petrobot.util.ExcelUtil;
import petrobot.util.LogUtil;

public class IndexManager {

    private static class LazyHolder {
        private static final IndexManager INSTANCE = new IndexManager();
    }

    private IndexManager() {

    }

    public static IndexManager getIns() {
        return LazyHolder.INSTANCE;
    }

    private String indexPackage = "petrobot.system";

    public Map<Integer, MethodStruct> METHOD_MAPPING = new HashMap<>();

    public Map<Integer, Integer> DEAL_INDEX_MAPPING = new HashMap<>();

    public int maxIndex = 0;

    /**
     * 保存有注册的顺序index
     */
    public List<Integer> executeIndex;

    public String getIndexPackage() {
        return indexPackage;
    }

    public void setIndexPackage(String indexPackage) {
        this.indexPackage = indexPackage;
    }

    public void loadExcel(String file) {
        try {
            File configFile = new File(file);
            if (configFile.exists()) {
                DEAL_INDEX_MAPPING = ExcelUtil.showExcelFileDialog(configFile);
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!checkMethod()) {
            throw new RuntimeException();
        }
    }

    /**
     * @param curIndex
     * @return -1 未找到下一个index
     */
    public int getNextIndex(Integer curIndex) {
        int nextIndex = curIndex == null ? 1 : curIndex + 1;
        for (; nextIndex <= this.maxIndex; nextIndex++) {
            Integer linkMethodId = DEAL_INDEX_MAPPING.get(nextIndex);
            if (linkMethodId != null && this.METHOD_MAPPING.get(linkMethodId) != null) {
                return nextIndex;
            }
        }

        if (RobotConfig.getInstance().isRobotCycle()) {
            return 1;
        } else {
            return -1;
        }
    }

    public void load() {
        //加载所有@Path的方法
        Set<Class<?>> controllerClasses = new HashSet<>(ClassUtil.getClassByAnnotation(indexPackage, Controller.class));
        if (controllerClasses.isEmpty()) {
            LogUtil.error("没有发现 controller, 包名：" + indexPackage + ",CurClass=" + getClass().getName() + ",package=" + getClass().getPackage().getName());
        }
        for (Class<?> clazz : controllerClasses) {
            Method[] methods = clazz.getMethods();
            Object handlerInstance = null;
            for (Method method : methods) {
                Index annotation = method.getAnnotation(Index.class);
                if (annotation == null) {
                    continue;
                }

                Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length != 1) {
                    LogUtil.error("[" + clazz.getName() + "." + method.getName() + "]方法存在Index注解，但是参数不止一个");
                    continue;
                }

                int index = annotation.value();
                Integer indexInt = Integer.valueOf(index);
                if (METHOD_MAPPING.containsKey(indexInt)) {
                    MethodStruct old = METHOD_MAPPING.get(indexInt);
                    LogUtil.error("[" + clazz.getName() + "." + method.getName() + "]路径重复," + index + "已经指向[" + old.getManger().getClass().getName() + "." + old.getMethod().getName() + "]");
                    continue;
                }

                if (handlerInstance == null) {
                    handlerInstance = createHandler(clazz);
                }

                MethodStruct handler = new MethodStruct(handlerInstance, method);
                METHOD_MAPPING.put(indexInt, handler);
                LogUtil.info("发现controller," + index + "->" + clazz.getName() + "." + method.getName());
            }
        }
    }

    public static Object createHandler(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建handler失败,class:[" + clazz.getName() + "]", e);
        }
    }

    public boolean checkMethod() {
        boolean result = true;
        int max = 1000;
        for (int i = 1; i < max; i++) {
            if (!DEAL_INDEX_MAPPING.containsKey(i)) {
                LogUtil.info("操作步骤一共" + (i - 1) + "步！");
                maxIndex = i - 1;
                break;
            }
        }

        for (Entry<Integer, Integer> entry : DEAL_INDEX_MAPPING.entrySet()) {
            if (!METHOD_MAPPING.containsKey(entry.getValue())) {
                LogUtil.info("操作步骤" + entry.getKey() + "步不存在执行方法ID:" + entry.getValue());
//                result = false;
            }
            if (entry.getKey() > maxIndex) {
                LogUtil.error("操作步骤不连续！断点为：" + entry.getKey());
                result = false;
            }
        }

        return result;
    }
}
