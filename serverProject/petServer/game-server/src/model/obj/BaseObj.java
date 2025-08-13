package model.obj;


import db.entity.BaseEntity;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import util.LogUtil;

public abstract class BaseObj extends BaseEntity {
    private Lock lock = null;
    private volatile boolean modified;

    public static boolean useLock = true;

    public BaseObj() {
        initLock();
    }

    public abstract String getBaseIdx();
    public abstract String getClassType();
    public void initLock() {
        if (useLock) {
            if (this.lock == null) {
                this.lock = new ReentrantLock();
            }
        }
    }

    // 目前仅用于可重复取锁的tick中
    public boolean tryLockObj() {
        if (this.lock == null || getClassType().equals("system")) {
            return false;
        }
        return this.lock.tryLock();
    }

    public boolean lockObj() {
        if (this.lock == null || getClassType().equals("system")) {
            return false;
        }
        this.lock.lock();
        return true;
    }

    public boolean unlockObj() {
        try {
            transAndPut();
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        } finally {
            if (this.lock == null || getClassType().equals("system")) {
                return false;
            }
            this.lock.unlock();
        }
    }

    public boolean unlockTickObj() {
        try {
            if (isModified()) {
                transAndPut();
                setModified(false);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        } finally {
            if (this.lock == null || getClassType().equals("system")) {
                return false;
            }
            this.lock.unlock();
        }
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * 转化二进制字段并putToCache
     */
    private void transAndPut() {
        transformDBData();
        putToCache();
    }

    /**
     * 当对象加锁调用后解锁时会调用此方法put到对应的Cache
     */
    public abstract void putToCache();

    /**
     * 将临时的DBBuilder转为二进制存储到数据库字段中
     */
    public abstract void transformDBData();
}
