/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import model.mistforest.trigger.TriggerManager;

@annationInit(value = "MistBuffConfig", methodname = "initConfig")
public class MistBuffConfig extends baseConfig<MistBuffConfigObject> {


    private static MistBuffConfig instance = null;

    public static MistBuffConfig getInstance() {

        if (instance == null)
            instance = new MistBuffConfig();
        return instance;

    }


    public static Map<Integer, MistBuffConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistBuffConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistBuffConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistBuffConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MistBuffConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setLifetime(MapHelper.getInt(e, "LifeTime"));

        config.setCycletime(MapHelper.getInt(e, "CycleTime"));

        config.setMaxstackcount(MapHelper.getInt(e, "MaxStackCount"));

        config.setIsprogressbuff(MapHelper.getBoolean(e, "IsProgressBuff"));

        config.setIsdebuff(MapHelper.getBoolean(e, "IsDebuff"));

        config.setInterrupttype(MapHelper.getInts(e, "InterruptType"));

        config.setPausedecreasetime(MapHelper.getInt(e, "PauseDecreaseTime"));

        config.setIsofflinebuff(MapHelper.getBoolean(e, "IsOfflineBuff"));

        config.setBuffAddTriggers(TriggerManager.getInstance().loadTriggerData(MapHelper.getStr(e, "AddEffectCmd")));

        config.setBuffDelTriggers(TriggerManager.getInstance().loadTriggerData(MapHelper.getStr(e, "DelEffectCmd")));

        config.setBuffCycleTriggers(TriggerManager.getInstance().loadTriggerData(MapHelper.getStr(e, "CycleEffectCmd")));

        config.setInterruptTriggers(TriggerManager.getInstance().loadTriggerData(MapHelper.getStr(e, "InterruptEffectCmd")));

        config.setBuffrobbedeffectcmd(TriggerManager.getInstance().loadTriggerData(MapHelper.getStr(e, "BuffRobbedEffectCmd")));


        _ix_id.put(config.getId(), config);


    }
}
