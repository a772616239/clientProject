/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PlayerSkillConfig", methodname = "initConfig")
public class PlayerSkillConfig extends baseConfig<PlayerSkillConfigObject> {


    private static PlayerSkillConfig instance = null;

    public static PlayerSkillConfig getInstance() {

        if (instance == null)
            instance = new PlayerSkillConfig();
        return instance;

    }


    public static Map<String, PlayerSkillConfigObject> _ix_key = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PlayerSkillConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PlayerSkillConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PlayerSkillConfigObject getByKey(String key) {

        return _ix_key.get(key);

    }


    public void putToMem(Map e, PlayerSkillConfigObject config) {

        config.setKey(MapHelper.getStr(e, "key"));

        config.setUptype(MapHelper.getInt(e, "upType"));

        config.setPlayerlv(MapHelper.getInt(e, "playerLv"));

        config.setSkillid(MapHelper.getInt(e, "skillId"));

        config.setLevel(MapHelper.getInt(e, "level"));

        config.setUpconsume(MapHelper.getInts(e, "upConsume"));

        config.setIncreaseproperty(MapHelper.getIntArray(e, "increaseProperty"));

        config.setExtraproperty(MapHelper.getIntArray(e, "extraProperty"));

        config.setCumuproperty(MapHelper.getIntArray(e, "cumuProperty"));


        _ix_key.put(config.getKey(), config);


    }

    private static final int upStar = 1;
    private static final int upLv = 2;

    public static PlayerSkillConfigObject getBySkillIdAndStar(int skillCfgId, int skillStar) {

        return getBySkillIdTypeAndLv(skillCfgId, upStar, skillStar);
    }

    public static PlayerSkillConfigObject getBySkillIdTypeAndLv(int skillId, int type, int lv) {
        return _ix_key.values().stream().filter(e ->
                e.getSkillid() == skillId && e.getUptype() == type && e.getLevel() == lv).findFirst().orElse(null);

    }

}
