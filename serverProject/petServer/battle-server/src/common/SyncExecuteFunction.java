package common;

import model.obj.BaseObj;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
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
}
