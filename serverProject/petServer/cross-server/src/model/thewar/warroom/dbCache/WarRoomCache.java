package model.thewar.warroom.dbCache;

import common.GlobalTick;
import common.Tickable;
import java.util.Iterator;
import java.util.Map.Entry;
import model.obj.ObjCache;
import model.obj.ObjPool;
import model.thewar.warroom.entity.WarRoom;

public class WarRoomCache extends ObjCache<WarRoom> implements Tickable {
    private static WarRoomCache instance = null;

    private long tickTime;

    public static WarRoomCache getInstance() {
        if (instance == null) {
            instance = new WarRoomCache();
            instance.setObjPool(new ObjPool<>(() -> new WarRoom()));
        }
        return instance;
    }

    public int getWarRoomCount() {
        return objMap.size();
    }

    @Override
    public void onTick() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (tickTime > curTime) {
            return;
        }
        WarRoom room;
        Iterator<Entry<String, WarRoom>> iter = objMap.entrySet().iterator();
        while (iter.hasNext()) {
            room = iter.next().getValue();
            if (room.needClear()) {
                removeObject(room);
            } else {
                try {
                    room.lockObj();
                    room.onTick();
                } finally {
                    room.unlockTickObj();
                }
            }
        }
    }
}
