/*CREATED BY TOOL*/

package model.player.threader;

import annotation.annationInit;
import common.GlobalThread;
import common.GlobalTick;
import model.player.cache.PlayerCache;
import timetool.TimeHelper;
import util.LogUtil;

@annationInit(value = "cachePlayerThreadL", methodname = "Start")
public class cachePlayerThreadL implements Runnable {
    private static boolean bRun = true;
    private static cachePlayerThreadL _instanse = null;


    public synchronized static void Start(Object o) {
        if (_instanse == null)
            _instanse = (cachePlayerThreadL) o;
        Start();
    }

    public static void Start() {

        if (_instanse == null)
            _instanse = new cachePlayerThreadL();
        bRun = true;
        GlobalThread.getInstance().getExecutor().execute(_instanse);

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
            long curTime = GlobalTick.getInstance().getCurrentTime();
            PlayerCache.getInstance().onTick(curTime);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
