package model.player.cache;

import common.SyncExecuteFunction;
import common.load.ServerConfig;
import datatool.StringHelper;
import model.obj.ObjCache;
import model.obj.ObjPool;
import model.player.entity.Player;

import java.util.Iterator;
import java.util.Map;

public class PlayerCache extends ObjCache<Player> {
    private static PlayerCache instance = null;

    public static PlayerCache getInstance() {
        if (instance == null) {
            instance = new PlayerCache();
            instance.setObjPool(new ObjPool<>(() -> new Player()));
        }
        return instance;
    }

    public boolean isServerFull() {
        return objMap.size() >= ServerConfig.getInstance().getMaxOnlinePlayerNum() - 10;
    }

    public void onTick(long curTime) {
        Player player;
        Iterator<Map.Entry<String, Player>> iter = objMap.entrySet().iterator();
        while (iter.hasNext()) {
            player = iter.next().getValue();
            if (player.getFromServerIndex() <= 0) {
                removeObject(player);
            } else {
                SyncExecuteFunction.executeConsumer(player, player1 -> player1.onTick(curTime));
            }
        }
    }
}
