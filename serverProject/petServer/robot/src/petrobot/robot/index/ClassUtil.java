package petrobot.robot.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petrobot.util.LogUtil;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

    private static final String PROTOCOL_FILE = "file";

    private static final String PROTOCOL_JAR = "jar";

    private static Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);


    public interface ClassSelector {
        boolean select(Class<?> clazz);
    }

    /**
     * 从classpath下面获取指定包下的具有指定注解的类
     * @param packageName 指定包名，注意，这里只能是包名，不能是具体class，否则找不到该具体class
     * @param annotationClass 指定注解类型
     * @param classLoader 类加载器
     * @return
     */
    public static Set<Class<?>> findClassWithAnnotation(ClassLoader classLoader,
                                                        String packageName,
                                                        Class<? extends Annotation> annotationClass) {

        Set<Class<?>> ret = getFromClassPath(classLoader, packageName,
                clazz -> clazz.getAnnotation(annotationClass) != null);
        for (Class<?> clazz : ret) {
            LogUtil.info("找到持有[" + annotationClass.getName() + "]注解的类:" + clazz.getName());
        }
        return ret;
    }

    /**
     * 从给定的JarFile中获取指定包下的具有指定注解的类
     * @param packageName 指定包名，注意，这里只能是包名，不能是具体class，否则找不到该具体class
     * @param annotationClass 指定注解类型
     * @param classLoader 类加载器
     * @param file jar文件
     * @return
     */
    public static Set<Class<?>> findClassWithAnnotation(ClassLoader classLoader,
                                                        String packageName,
                                                        JarFile file,
                                                        Class<? extends Annotation> annotationClass ) {

        Set<Class<?>> ret = getClassFromJarFile(classLoader, packageName, file, clazz -> clazz.getAnnotation(annotationClass) != null);
        for (Class<?> clazz : ret) {
            LogUtil.info("找到持有[" + annotationClass.getName() + "]注解的类:" + clazz.getName());
        }
        return ret;
    }


    /**
     * 从classpath下面获取指定父类的所有子类
     * @param packageName 指定包名，注意，这里只能是包名，不能是具体class，否则找不到该具体class
     * @param parentClass 指定的父类
     * @return
     */
    public static  Set<Class<?>> findClassWithSuperClass(String packageName, Class<?> parentClass) {
        Set<Class<?>> ret = getFromClassPath(null, packageName,
                clazz -> parentClass.isAssignableFrom(clazz) && !parentClass.equals(clazz));
        for (Class<?> clazz : ret) {
            LogUtil.info("发现[" + parentClass.getName() + "]的子类:" + clazz.getName());
        }
        return ret;
    }

    /**
     * 获取classpath指定包名下符合ClassSelector选择的所有类
     * @return
     */

    public static Set<Class<?>> getFromClassPath(ClassLoader classLoader, String packageName, ClassSelector selector) {
        if(classLoader == null) {
            classLoader = ClassUtil.class.getClassLoader();
        }
        Set<Class<?>> allClazz = new LinkedHashSet<>();
        String packageDir = packageName.replace('.', File.separatorChar);
        LogUtil.info("Get FormClass packageDir="+packageDir);
        try {
            Enumeration<URL> dirs = classLoader.getResources(packageDir);
            if (dirs == null) {
                LogUtil.error("read enumeration null");
            }
            if (!dirs.hasMoreElements()) {
                LogUtil.error("read enumeration empty");
            }
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                LogUtil.info(" read enumeration:"+url);
                if (PROTOCOL_FILE.equals(protocol)) {
                    //文件夹路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    allClazz.addAll(findClassFromDir(classLoader, packageName, filePath));
                } else if (PROTOCOL_JAR.equals(protocol)) {
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    allClazz.addAll(findClassFromJar(classLoader, jar, packageDir));
                }
            }
        } catch (Throwable e) {
            LogUtil.error("读取日志Class文件出错:" + e.getMessage());
        }

        Set<Class<?>> ret = new LinkedHashSet<>();

        for(Class<?> clazz : allClazz) {
            String clzName = clazz.getSimpleName();
            LogUtil.info("read class:"+clazz.getName());
            if (clzName.endsWith("Manager")) {
                LogUtil.info("read manager getController:"+clazz.getAnnotations());
            }
           if(selector.select(clazz)) {
                ret.add(clazz);
            }
        }

        return ret;

    }


    /**
     * 获取指定jar文件指定包名下符合ClassSelector选择的所有类
     * @return
     */
    private static Set<Class<?>> getClassFromJarFile(ClassLoader classLoader,
                                                        String packageName,
                                                        JarFile file,
                                                      ClassSelector selector ) {

        if(classLoader == null) {
            classLoader = ClassUtil.class.getClassLoader();
        }
        String packageDir = packageName.replace('.', '/');
        Set<Class<?>> allClazz = findClassFromJar(classLoader,file, packageDir);


        Set<Class<?>> ret = new LinkedHashSet<>();

        for(Class<?> clazz : allClazz) {
            if(selector.select(clazz)) {
                ret.add(clazz);
            }
        }

        return ret;
    }

    /**
     *  获取指定包下的所有类
     * @param jar jar归档文件
     * @param packageDir package转换成文件目录格式的字符串
     * @return
     */
    private static Set<Class<?>> findClassFromJar(ClassLoader classLoader, JarFile jar, String packageDir) {

        Set<Class<?>> ret = new LinkedHashSet<>();

        Enumeration<JarEntry> entries = jar.entries();
        while(entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                //jar中的entry是所有层级的文件都列出来的，所以文件夹实际上一点用都没有
                continue;
            }
            String name = entry.getName();
            //以packageDir开头并且是class文件
            if (!name.startsWith(packageDir) || !name.endsWith(".class")) {
                continue;
            }

            name = name.replaceAll("/", ".");
            name = name.substring(0, name.length() - 6);
            try {
                Class<?> clazz = Class.forName(name, false, classLoader);
                ret.add(clazz);
            } catch (Throwable e) {
                LogUtil.error("读取Jar中的Class文件出错:" + name + "," + e.getMessage());
            }
        }
        return ret;

    }

    /**
     * 获取文件夹下所有的类
     * @param packageName 包名,该目录下的class文件对应的包名，因为文件是绝对路径的，无法计算包名，所以从外部传入
     * @param filePath 文件夹路径
     * @return
     */
    private static Set<Class<?>> findClassFromDir(ClassLoader classLoader,String packageName, String filePath) {

        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptySet();
        }

        Set<Class<?>> ret = new LinkedHashSet<>();

        File[] files = dir.listFiles(file -> (file.isDirectory()) || file.getName().endsWith(".class"));

        for (File file : files) {

            if (file.isDirectory()) {
                ret.addAll(findClassFromDir(classLoader,packageName + "." + file.getName(), file.getAbsolutePath()));
                continue;
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(packageName + '.' + className, false,
                            classLoader);
                    ret.add(clazz);
                } catch (Throwable e) {
                    LogUtil.error("读取Jar中的Class文件出错:" + e.getMessage());
                }
            }

        }
        return ret;
    }

    /**
     * 获取一个参数类型的具体类型
     * @param parameterizedType
     * @return
     */
    private static List<Class<?>> getActualTypeArgumentClass(ParameterizedType parameterizedType) {
        List<Class<?>> ret = new ArrayList<>();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        for (int i = 0; i < actualTypeArguments.length; i++) {
            Type actualTypeArgument = actualTypeArguments[i];
            if (actualTypeArgument instanceof ParameterizedType) {
                //继续递归查询嵌套泛型
                ParameterizedType childParameterizedType = (ParameterizedType) actualTypeArgument;
                ret.add((Class<?>) childParameterizedType.getRawType());
                ret.addAll(getActualTypeArgumentClass(childParameterizedType));

            }  else if(actualTypeArgument instanceof WildcardType) {
                //WildcardType wildcardType = (WildcardType) genericType;
                throw new RuntimeException("存储类中不允许使用通配符->field:" + parameterizedType.getTypeName());
            } else {
                Class<?> clazz = (Class<?>) actualTypeArgument;
                ret.add(clazz);
            }
        }
        return ret;
    }



}
