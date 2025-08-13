package common;

import model.obj.BaseObj;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SyncExecuteFunction {
    public static <T extends BaseObj> void executeConsumer(T obj, Consumer<T> func) {
        if (obj == null) {
            return;
        }
        try {
            obj.lockObj();
            func.accept(obj);
        } finally {
            obj.unlockObj();
        }
    }

    public static <T extends BaseObj> boolean executeSupplier(T obj, BooleanSupplier func) {
        if (obj == null) {
            return false;
        }
        try {
            obj.lockObj();
            return func.getAsBoolean();
        } finally {
            obj.unlockObj();
        }
    }

    public static <T extends BaseObj> boolean executePredicate(T obj, Predicate<T> func) {
        if (obj == null) {
            return false;
        }
        try {
            obj.lockObj();
            return func.test(obj);
        } finally {
            obj.unlockObj();
        }
    }

    public static <T extends BaseObj, U, R> R executeBitFunction(T target, U source, BiFunction<T, U, R> func) {
        if (target == null) {
            return null;
        }
        try {
            target.lockObj();
            return func.apply(target, source);
        } finally {
            target.unlockObj();
        }
    }

    public static <T extends BaseObj, R> R executeFunction(T obj, Function<T, R> func) {
        if (obj == null) {
            return null;
        }
        try {
            obj.lockObj();
            return func.apply(obj);
        } finally {
            obj.unlockObj();
        }
    }

    /**
     * 专用于Tick并发函数,需要手动添加是否修改
     *
     * @param obj 并发对象
     * @param func 并发函数
     * @return 并发对象是否发生修改
     */
    public static <T extends BaseObj> void executeTickConsumer(T obj, Consumer<T> func) {
        if (obj == null) {
            return;
        }
        try {
            obj.lockObj();
            func.accept(obj);
        } finally {
            obj.unlockTickObj();
        }
    }

}
