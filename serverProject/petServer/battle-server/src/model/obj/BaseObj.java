package model.obj;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseObj {
    private Lock lock = null;
    public static boolean useLock = true;

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
            this.lock.unlock();
            return true;
        } else {
            return false;
        }
    }
}
