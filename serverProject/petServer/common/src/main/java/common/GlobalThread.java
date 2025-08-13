package common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalThread {
    private static GlobalThread instance = new GlobalThread();
    private ExecutorService executor;

    public static GlobalThread getInstance() {
        return instance;
    }

    public boolean init(int threadCount) {
        if (executor == null) {
            if (threadCount > 1) {
                this.executor = Executors.newCachedThreadPool();
            } else {
                this.executor = Executors.newSingleThreadExecutor();
            }
        }
        return this.executor != null;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void execute(Runnable run) {
        if (run != null) {
           this.executor.execute(run);
        }
    }
}
