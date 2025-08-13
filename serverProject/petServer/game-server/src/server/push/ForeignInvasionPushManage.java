/*
package server.push;

import common.tick.GlobalTick;
import model.foreignInvasion.newVersion.NewForeignInvasionManager;

*/
/**
 * @Description
 * @Author hanx
 * @Date2020/8/6 0006 20:09
 **//*

public class ForeignInvasionPushManage extends PushManage {

    public ForeignInvasionPushManage() {
        super(PushMsgIdEnum.ForeignInvasion);
    }

    private static ForeignInvasionPushManage instance = new ForeignInvasionPushManage();

    public static ForeignInvasionPushManage getInstance() {
        if (instance == null) {
            synchronized (ForeignInvasionPushManage.class) {
                if (instance == null) {
                    instance = new ForeignInvasionPushManage();
                }
            }
        }
        return instance;
    }

    @Override
    public void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (nextTickTime > currentTime) {
            return;
        }
        nextTickTime = calculateNextTickTime();
        executePush();
    }

    private long calculateNextTickTime() {
//        return ForeignInvasionManager.getInstance().getNextOpenDayCurStamp(GlobalTick.getInstance().getCurrentTime()+ leadTime) -leadTime;
        return NewForeignInvasionManager.getInstance().getNextOpenDayCurStamp(GlobalTick.getInstance().getCurrentTime()+ leadTime) -leadTime;
    }

    @Override
    public void executePush() {
        sendPushMsgToAll(getMsgIdEnum());
    }

    @Override
    protected void init() {
        super.init();
        nextTickTime = NewForeignInvasionManager.getInstance().getNextOpenDayCurStamp(GlobalTick.getInstance().getCurrentTime()+ leadTime)-leadTime;;
       // GlobalTick.getInstance().addTick(this);
    }
}
*/
