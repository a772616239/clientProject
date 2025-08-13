package petrobot.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.TypeFilter;

/**
 * 基于spring框架实现实现
 *
 * @author huhan
 * @date 2020.02.28
 */
public class ClassUtil {

    /**
     * 只返回具体类,不包括抽象类和接口
     * @param packageName
     * @param annotationClass
     * @return
     */
    public static <T extends Annotation> List<Class<T>> getClassByAnnotation(String packageName, Class<T> annotationClass) {
        return getClass(packageName, annotationClass,
                (metadataReader, metadataReaderFactory) ->
                        metadataReader.getAnnotationMetadata().hasAnnotation(annotationClass.getName()));
    }

    /**
     * 只返回具体类,不包括抽象类和接口
     * @param packageName
     * @param interfaceClass
     * @return
     */
    public static <T> List<Class<T>> getClassByInterface(String packageName, Class<T> interfaceClass) {
        return getClass(packageName, interfaceClass,
                (metadataReader, metadataReaderFactory) ->
                        ArrayUtil.contain(metadataReader.getClassMetadata().getInterfaceNames(), interfaceClass.getName()));
    }

    /**
     * 只返回具体类,不包括抽象类和接口
     * @param packageName
     * @param clazz
     * @return
     */
    public static <T> List<Class<T>> getSubClass(String packageName, Class<T> clazz) {
        return getClass(packageName, clazz,
                (metadataReader, metadataReaderFactory) ->
                        clazz.getName().equals(metadataReader.getClassMetadata().getSuperClassName()));
    }

    /**
     * 只返回具体类,不包括抽象类和接口
     * @param packageName
     * @param clazz
     * @param includeFilter
     * @return
     */
    private static <T> List<Class<T>> getClass(String packageName, Class<T> clazz, TypeFilter... includeFilter) {
        if (!ObjectUtil.requireNotNull(packageName, clazz, includeFilter)) {
            return null;
        }

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        for (TypeFilter typeFilter : includeFilter) {
            provider.addIncludeFilter(typeFilter);
        }

        Set<BeanDefinition> candidateComponents = provider.findCandidateComponents(packageName);
        List<Class<T>> classes = new ArrayList<>();
        if (candidateComponents != null) {
            for (BeanDefinition candidateComponent : candidateComponents) {
                try {
                    classes.add((Class<T>)Class.forName(candidateComponent.getBeanClassName()));
                } catch (Exception e) {
                    System.out.println("can not load class for name: " + candidateComponent.getBeanClassName());
                }
            }
        }
        return classes;
    }
}
