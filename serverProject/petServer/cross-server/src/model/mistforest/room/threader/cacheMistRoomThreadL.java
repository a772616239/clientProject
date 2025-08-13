/*CREATED BY TOOL*/

package model.mistforest.room.threader;

import annotation.annationInit;
import common.GlobalThread;
import model.mistforest.room.cache.MistRoomCache;
import server.PetAPP;
import util.LogUtil;

@annationInit(value = "cacheMistRoomThreadL", methodname = "Start")
public class cacheMistRoomThreadL implements Runnable {
    private static boolean bRun = true;
    private static cacheMistRoomThreadL _instanse = null;


    public synchronized static void Start(Object o) {
        if (_instanse == null)
            _instanse = (cacheMistRoomThreadL) o;
        Start();
    }

    public static void Start() {

        if (_instanse == null)
            _instanse = new cacheMistRoomThreadL();
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
        LogUtil.info("*************cacheMistRoomThreadL******************* RUN");
        while (bRun) {
            try {
                process();
                Sleep(67);
            } catch (Exception e) {
                if (!bRun)
                    return;
                LogUtil.printStackTrace(e);
                Sleep(67);
            }
        }
    }


    public static void process() {
        if (!PetAPP.loadFinish) {
            return;
        }
        MistRoomCache.getInstance().onTick();
    }
}
