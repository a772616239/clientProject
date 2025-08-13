/*CREATED BY TOOL*/

package model.mistplayer.threader;

import annotation.annationInit;
import common.GlobalThread;
import common.GlobalTick;
import model.mistplayer.cache.MistPlayerCache;
import server.PetAPP;
import timetool.TimeHelper;
import util.LogUtil;

@annationInit(value = "cachePlayerThreadL", methodname = "Start")
public class MistPlayerThreadL implements Runnable {
    private static boolean bRun = true;
    private static MistPlayerThreadL _instance = null;


    public synchronized static void Start(Object o) {
        if (_instance == null)
            _instance = (MistPlayerThreadL) o;
        Start();
    }

    public static void Start() {

        if (_instance == null)
            _instance = new MistPlayerThreadL();
        bRun = true;
        GlobalThread.getInstance().getExecutor().execute(_instance);

    }

    private static void Sleep(long elapsed) {

        if (elapsed <= 0)

            return;
        try {
            Thread.sleep(elapsed);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }


    public void run() {
        LogUtil.info("*************cachePlayerThreadL******************* RUN");
        while (bRun) {
            try {
                process();
                Sleep(TimeHelper.SEC * 1);
            } catch (Exception e) {
                if (!bRun)
                    return;
                LogUtil.printStackTrace(e);
                Sleep(TimeHelper.SEC * 1);
            }
        }
    }


    public static void process() {
        try {
            if (!PetAPP.loadFinish) {
                return;
            }
            long curTime = GlobalTick.getInstance().getCurrentTime();
            MistPlayerCache.getInstance().onTick(curTime);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
