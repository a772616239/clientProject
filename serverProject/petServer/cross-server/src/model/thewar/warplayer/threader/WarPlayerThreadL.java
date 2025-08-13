/*CREATED BY TOOL*/

package model.thewar.warplayer.threader;

import annotation.annationInit;
import common.GlobalThread;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import server.PetAPP;
import timetool.TimeHelper;
import util.LogUtil;

@annationInit(value = "WarPlayerThreadL", methodname = "Start")
public class WarPlayerThreadL implements Runnable {
    private static boolean bRun = true;
    private static WarPlayerThreadL _instance = null;


    public synchronized static void Start(Object o) {
        if (_instance == null)
            _instance = (WarPlayerThreadL) o;
        Start();
    }

    public static void Start() {

        if (_instance == null)
            _instance = new WarPlayerThreadL();
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
        LogUtil.info("*************WarPlayerThreadL******************* RUN");
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
            WarPlayerCache.getInstance().onTick();
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
