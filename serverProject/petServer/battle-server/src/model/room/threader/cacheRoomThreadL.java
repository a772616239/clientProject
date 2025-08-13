package model.room.threader;

import annotation.annationInit;
import common.GlobalThread;
import common.GlobalTick;
import model.room.cache.RoomCache;
import timetool.TimeHelper;
import util.LogUtil;

@annationInit(value = "cachePlayerThreadL", methodname = "Start")
public class cacheRoomThreadL implements Runnable {
    private static boolean bRun = true;
    private static cacheRoomThreadL _instanse = null;


    public synchronized static void Start(Object o) {
        if (_instanse == null)
            _instanse = (cacheRoomThreadL) o;
        Start();
    }

    public static void Start() {

        if (_instanse == null)
            _instanse = new cacheRoomThreadL();
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
        LogUtil.info("*************cacheRoomThreadL******************* RUN");
        while (bRun) {
            try {
                process();
                Sleep(100l);
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
            RoomCache.getInstance().onTick(curTime);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
