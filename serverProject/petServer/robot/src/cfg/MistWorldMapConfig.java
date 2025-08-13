/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import petrobot.util.ServerConfig;
import protocol.MistForest.EnumMistRuleKind;

@annationInit(value = "MistWorldMapConfig", methodname = "initConfig")
public class MistWorldMapConfig extends baseConfig<MistWorldMapConfigObject> {


    private static MistWorldMapConfig instance = null;

    public static MistWorldMapConfig getInstance() {

        if (instance == null)
            instance = new MistWorldMapConfig();
        return instance;

    }


    public static Map<Integer, MistWorldMapConfigObject> _ix_mapid = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistWorldMapConfig) o;
        initConfig();
    }


    public void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistWorldMapConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistWorldMapConfigObject getByMapid(int mapid) {

        return _ix_mapid.get(mapid);

    }


    public void putToMem(Map e, MistWorldMapConfigObject config) {

        config.setMapid(MapHelper.getInt(e, "MapId"));

        config.setMaprule(MapHelper.getInt(e, "MapRule"));

        config.setLevel(MapHelper.getInt(e, "Level"));

        config.setMaxplayercount(MapHelper.getInt(e, "MaxPlayerCount"));

        config.setMapsize(MapHelper.getInts(e, "MapSize"));

        config.setAoiarea(MapHelper.getInts(e, "AoiArea"));

        config.setSaferegion(MapHelper.getInts(e, "SafeRegion"));

        config.setTeleporterlist(MapHelper.getIntArray(e, "TeleporterList"));

        _ix_mapid.put(config.getMapid(), config);

        if (config.getMaprule() == EnumMistRuleKind.EMRK_Common_VALUE) {
            if (defaultCommonMistLevel <= 0 || config.getLevel() < defaultCommonMistLevel) {
                defaultCommonMistLevel = config.getLevel();
            }

            if (config.getLevel() > maxCommonMistLevel) {
                maxCommonMistLevel = config.getLevel();
            }
        }
    }

    public MistWorldMapConfigObject getCfgByRuleAndLevel(int mistRule, int mistLevel) {
        for (MistWorldMapConfigObject cfg : _ix_mapid.values()) {
            if (cfg.getMaprule() == mistRule && cfg.getLevel() == mistLevel) {
                return cfg;
            }
        }
        return null;
    }

    public int getMapIdByRuleAndLevel(int mistRule, int mistLevel) {
        for (MistWorldMapConfigObject cfg : _ix_mapid.values()) {
            if (cfg.getMaprule() == mistRule && cfg.getLevel() == mistLevel) {
                return cfg.getMapid();
            }
        }
        return 0;
    }

    protected int defaultCommonMistLevel;

    public int getDefaultCommonMistLevel() {
        return defaultCommonMistLevel;
    }

    protected int maxCommonMistLevel;

    public int getMaxCommonMistLevel() {
        return maxCommonMistLevel;
    }
}
