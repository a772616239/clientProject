/*CREATED BY TOOL*/

package cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "CrossArenaEvent", methodname = "initConfig")
public class CrossArenaEvent extends baseConfig<CrossArenaEventObject> {


    private static CrossArenaEvent instance = null;

    public static CrossArenaEvent getInstance() {

        if (instance == null)
            instance = new CrossArenaEvent();
        return instance;

    }


    public static Map<Integer, CrossArenaEventObject> _ix_id = new HashMap<>();

    public static Map<Integer,List< CrossArenaEventObject>> bossEventMap = new HashMap<>();

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CrossArenaEvent) o;
        initConfig();
        groupBossEvent();
    }

    private void groupBossEvent() {
        for (CrossArenaEventObject cfg : _ix_id.values()) {
            if (cfg.getType() == 3) {
                List<CrossArenaEventObject> eventList = bossEventMap.computeIfAbsent(getBossEventMapKey(cfg.getSceneid(), cfg.getHonrlv()), a -> new ArrayList<>());
                eventList.add(cfg);
            }
        }
    }

    public List<CrossArenaEventObject> getBossEventCfg(int sceneId, int honrLv) {
        return bossEventMap.get(getBossEventMapKey(sceneId, honrLv));
    }

    private int getBossEventMapKey(int sceneId, int honrLv) {
        return sceneId * 100 + honrLv;
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CrossArenaEvent");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CrossArenaEventObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, CrossArenaEventObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setType(MapHelper.getInt(e, "type"));

        config.setSceneid(MapHelper.getInt(e, "sceneId"));

        config.setHonrlv(MapHelper.getInt(e, "honrLv"));

        config.setExetype(MapHelper.getInt(e, "exeType"));

        config.setTriggertype(MapHelper.getInt(e, "triggerType"));

        config.setTriggerparm1(MapHelper.getInt(e, "triggerParm1"));

        config.setTriggerparm2(MapHelper.getInt(e, "triggerParm2"));

        config.setIseq(MapHelper.getInt(e, "isEq"));

        config.setRate(MapHelper.getInt(e, "rate"));

        config.setCycletype(MapHelper.getInt(e, "cycleType"));

        config.setCoutime(MapHelper.getInt(e, "couTime"));

        config.setFlishnum(MapHelper.getInt(e, "flishNum"));

        config.setFightmakeid(MapHelper.getInt(e, "fightmakeid"));

        config.setAward(MapHelper.getIntArray(e, "award"));

        config.setAward2(MapHelper.getIntArray(e, "award2"));

        config.setMarquee(MapHelper.getInt(e, "marquee"));


        _ix_id.put(config.getId(), config);


    }
}
