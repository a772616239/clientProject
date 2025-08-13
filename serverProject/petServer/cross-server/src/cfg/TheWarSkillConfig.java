/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;

@annationInit(value = "TheWarSkillConfig", methodname = "initConfig")
public class TheWarSkillConfig extends baseConfig<TheWarSkillConfigObject> {


    private static TheWarSkillConfig instance = null;

    public static TheWarSkillConfig getInstance() {

        if (instance == null)
            instance = new TheWarSkillConfig();
        return instance;

    }


    public static Map<Integer, TheWarSkillConfigObject> _ix_id = new HashMap<Integer, TheWarSkillConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TheWarSkillConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TheWarSkillConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TheWarSkillConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, TheWarSkillConfigObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setSkillid(MapHelper.getInt(e, "SkillId"));

        config.setSkilllevel(MapHelper.getInt(e, "SkillLevel"));

        config.setBuffid(MapHelper.getInt(e, "BuffId"));


        _ix_id.put(config.getId(), config);

        if (skillIdMap == null) {
            skillIdMap = new HashMap<>();
        }
        List<TheWarSkillConfigObject> cfgList = skillIdMap.get(config.getSkillid());
        if (cfgList == null) {
            cfgList = new ArrayList<>();
            skillIdMap.put(config.getSkillid(), cfgList);
        }
        cfgList.add(config);
    }

    private Map<Integer, List<TheWarSkillConfigObject>> skillIdMap;

    public int getBuffListBySkillIdAndLevel(int skillId, int level) {
        List<TheWarSkillConfigObject> cfgList = skillIdMap.get(skillId);
        if (cfgList == null) {
            return 0;
        }
        TheWarSkillConfigObject config = cfgList.stream().filter(cfg -> cfg.getSkilllevel() == level).findFirst().orElse(null);
        return config != null ? config.getBuffid() : 0;
    }
}
