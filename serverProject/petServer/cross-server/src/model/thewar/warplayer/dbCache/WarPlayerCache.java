package model.thewar.warplayer.dbCache;

import common.Tickable;
import datatool.StringHelper;
import java.util.Iterator;
import java.util.Map.Entry;
import model.obj.ObjCache;
import model.obj.ObjPool;
import model.thewar.warplayer.entity.WarPlayer;

public class WarPlayerCache extends ObjCache<WarPlayer> implements Tickable {
    private static WarPlayerCache instance = null;

    public static WarPlayerCache getInstance() {
        if (instance == null) {
            instance = new WarPlayerCache();
            instance.setObjPool(new ObjPool<>(() -> new WarPlayer()));
        }
        return instance;
    }

    @Override
    public void onTick() {
        WarPlayer warPlayer;
        Iterator<Entry<String, WarPlayer>> iter = objMap.entrySet().iterator();
        while (iter.hasNext()) {
            warPlayer = iter.next().getValue();
            if (warPlayer.getServerIndex() <= 0) {
                iter.remove();
            } else {
                try {
                    warPlayer.lockObj();
                    warPlayer.onTick();
                } finally {
                    warPlayer.unlockTickObj();
                }
            }
        }
    }
}
