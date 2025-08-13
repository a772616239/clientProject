package model.obj;

import db.entity.BaseEntity;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseObj extends BaseEntity {
    private Lock lock = null;
    public static boolean useLock = true;

    private volatile boolean modified;

    public BaseObj() {
        initLock();
    }

    public abstract String getIdx();
    public abstract void setIdx(String idx);
    public abstract String getClassType();
    public void initLock() {
        if (useLock) {
            if (this.lock == null) {
                this.lock = new ReentrantLock();
            }
        }
    }

    public boolean lockObj() {
        if (this.lock != null) {
            this.lock.lock();
            return true;
        } else {
            return false;
        }
    }

    public boolean unlockObj() {
        if (this.lock != null) {
            putToCache();
            this.lock.unlock();
            return true;
        } else {
            return false;
        }
    }

    public boolean unlockTickObj() {
        if (this.lock != null) {
            if (isModified()) {
                putToCache();
                setModified(false);
            }
            this.lock.unlock();
            return true;
        } else {
            return false;
        }
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    abstract public void putToCache();
    abstract public void transformDBData();
}
