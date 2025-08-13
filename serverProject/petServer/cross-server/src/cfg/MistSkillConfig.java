/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import model.mistforest.trigger.TriggerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "MistSkillConfig", methodname = "initConfig")
public class MistSkillConfig extends baseConfig<MistSkillConfigObject> {


    private static MistSkillConfig instance = new MistSkillConfig();

    public static MistSkillConfig getInstance() {
        if (instance == null)
            instance = new MistSkillConfig();
        return instance;
    }


    public static Map<Integer, MistSkillConfigObject> _ix_id = new HashMap<>();

    public static List<MistSkillConfigObject> initSkillList = new ArrayList<>();

    public static Map<Integer, Integer> itemType_Skills = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistSkillConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistSkillConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistSkillConfigObject getById(int id) {

        return _ix_id.get(id);

    }

    public static List<MistSkillConfigObject> getInitSkillList() {
        return initSkillList;
    }

    public static int getSkillByItemType(int type) {
        Integer skillId = itemType_Skills.get(type);
        return skillId != null ? skillId : 0;
    }

    public void putToMem(Map e, MistSkillConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setIsinitialskill(MapHelper.getBoolean(e, "IsInitialSkill"));

        config.setSourceitemtype(MapHelper.getInt(e, "SourceItemType"));

        config.setSkilltriggertiming(MapHelper.getInt(e, "SkillTriggerTiming"));

        config.setCooldown(MapHelper.getInt(e, "CoolDown"));

        config.setTriggerList(TriggerManager.getInstance().loadTriggerData(MapHelper.getStr(e, "SkillEffectCmd")));

        _ix_id.put(config.getId(), config);

        if (config.getIsinitialskill()) {
            initSkillList.add(config);
        }

        if (config.getSourceitemtype() > 0) {
            itemType_Skills.put(config.getSourceitemtype(), config.getId());
        }
    }
}
