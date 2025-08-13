/*CREATED BY TOOL*/

package model.timer.threader;

import annotation.annationInit;
import model.timer.cache.timerUpdateCacheL;
import normaltool.CommonLog;
import threadtool.ThreadEx;
import timetool.TimeHelper;

@annationInit(value = "cacheTimerThreadL", methodname = "Start")
public class cacheTimerThreadL implements Runnable {
    private static boolean bRun = true;
    private static cacheTimerThreadL _instanse = null;

    public synchronized static void Start(Object o) {
        if (_instanse == null)
            _instanse = (cacheTimerThreadL) o;
        Start();
    }

    public static void Start() {

        if (_instanse == null)
            _instanse = new cacheTimerThreadL();
        bRun = true;
        ThreadEx.execute(_instanse);

    }

    private static void Sleep(long elapsed) {

        if (elapsed <= 0)

            return;
        try {
            Thread.sleep(elapsed);
        } catch (Exception e) {
            CommonLog.log.info(e.toString(), e);
        }
    }


    public void run() {
        CommonLog.log.info("*************cacheTimerThreadL******************* RUN");
        while (bRun) {
            try {
                process();
                Sleep(TimeHelper.SEC * 1);
            } catch (Exception e) {
                if (!bRun)
                    return;
                CommonLog.log.info(e.toString(), e);
                Sleep(TimeHelper.SEC * 1);
            }
        }
    }


    public static void process() {

        timerUpdateCacheL.getInstance().dealUpdateCache();
    }
}
