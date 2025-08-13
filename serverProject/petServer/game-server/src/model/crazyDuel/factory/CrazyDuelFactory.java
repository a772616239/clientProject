package model.crazyDuel.factory;

import model.crazyDuel.CrazyDuelManager;
import protocol.CrayzeDuel;
import protocol.CrazyDuelDB.CrazyDuelSettingDB;

import java.util.List;

public class CrazyDuelFactory {

    public static CrazyDuelSettingDB creteCrazyDuelPlayerDB(String playerIdx) {
        CrazyDuelSettingDB.Builder player = CrazyDuelSettingDB.newBuilder();
        player.setPlayerIdx(playerIdx);
        List<CrayzeDuel.CrazyDuelBuffSetting> crazyDuelBuffSettings = CrazyDuelManager.getInstance().initOrRefreshBuffSetting(null);
        for (CrayzeDuel.CrazyDuelBuffSetting setting : crazyDuelBuffSettings) {
            player.putBuffSetting(setting.getFloor(),setting);
        }
        return player.build();
    }
}
