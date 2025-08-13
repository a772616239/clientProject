package petrobot.util;


import db.entity.BaseEntity;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
            transformDBData();
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
                transformDBData();
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

    public abstract void putToCache();

    /**
     * 将临时的DBBuilder转为二进制存储到数据库字段中
     */
    public abstract void transformDBData();
}
