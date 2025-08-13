package model.mistplayer.cache;

import datatool.StringHelper;
import model.mistplayer.entity.MistPlayer;
import model.obj.ObjCache;
import model.obj.ObjPool;

import java.util.Iterator;
import java.util.Map;

public class MistPlayerCache extends ObjCache<MistPlayer> {
    private static MistPlayerCache instance = null;

    public static MistPlayerCache getInstance() {
        if (instance == null) {
            instance = new MistPlayerCache();
            instance.setObjPool(new ObjPool<>(() -> new MistPlayer()));
        }
        return instance;
    }

    public void onTick(long curTime) {
        MistPlayer player;
        Iterator<Map.Entry<String, MistPlayer>> iter = objMap.entrySet().iterator();
        while (iter.hasNext()) {
            player = iter.next().getValue();
            if (player.getServerIndex() <= 0 && !player.isRobot()) {
                removeObject(player);
            } else {
                try {
                    player.lockObj();
                    player.onTick(curTime);
                } finally {
                    player.unlockTickObj();
                }
            }
        }
    }
}
