/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.GameConst;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.base.baseConfig;
import model.player.util.PlayerUtil;
import protocol.Common;

@annationInit(value = "FunctionOpenLvConfig", methodname = "initConfig")
public class FunctionOpenLvConfig extends baseConfig<FunctionOpenLvConfigObject> {


    private static final int defaultFunction = 0;
    private static final int lvUnLock = 1;
    private static final int chapterMissionUnlock = 2;
    private static final int chapterUnlock = 3;


    private static FunctionOpenLvConfig instance = null;

    public static FunctionOpenLvConfig getInstance() {

        if (instance == null)
            instance = new FunctionOpenLvConfig();
        return instance;

    }


    public static Map<Integer, FunctionOpenLvConfigObject> _ix_id = new HashMap<>();
    public static Map<Integer, List<FunctionOpenLvConfigObject>> typeMap;

    public static List<Common.EnumFunction> queryCanUnlockFunctionByLvUp(int curLv) {
        return _ix_id.values().stream().filter(e ->
                e.getUnlocktype() == lvUnLock && e.getUnlockneed() <= curLv)
                .map(FunctionOpenLvConfigObject::getId).map(Common.EnumFunction::forNumber).collect(Collectors.toList());

    }

    public static List<Common.EnumFunction> queryCanUnlockFunctionByMission(List<Integer> missionIds) {
        List<FunctionOpenLvConfigObject> cfg = typeMap.get(chapterMissionUnlock);
        if (cfg==null){
            return Collections.emptyList();
        }

        return cfg.stream().map(FunctionOpenLvConfigObject::getId).filter(missionIds::contains).map(Common.EnumFunction::forNumber).collect(Collectors.toList());

    }

    public static List<Common.EnumFunction> queryCanUnlockFunctionByKeyNode(int keyNodeId) {
        List<FunctionOpenLvConfigObject> cfg = typeMap.get(chapterUnlock);
        if (cfg==null){
            return Collections.emptyList();
        }
        return cfg.stream().filter(e -> e.getUnlockneed() <= keyNodeId).map(FunctionOpenLvConfigObject::getId).map(Common.EnumFunction::forNumber).collect(Collectors.toList());

    }

    public static List<Common.EnumFunction> queryDefaultFunction() {
        List<FunctionOpenLvConfigObject> cfg = typeMap.get(defaultFunction);
        if (cfg == null) {
            return Collections.emptyList();
        }
        return cfg.stream().map(FunctionOpenLvConfigObject::getId).map(Common.EnumFunction::forNumber).collect(Collectors.toList());
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (FunctionOpenLvConfig) o;
        initConfig();
        typeMap = _ix_id.values().stream().collect(Collectors.groupingBy(FunctionOpenLvConfigObject::getUnlocktype));
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "FunctionOpenLvConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static FunctionOpenLvConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, FunctionOpenLvConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setUnlocktype(MapHelper.getInt(e, "unlockType"));

        config.setUnlockneed(MapHelper.getInt(e, "unlockNeed"));

        config.setNeedchapter(MapHelper.getInt(e, "needChapter"));


        _ix_id.put(config.getId(), config);

    }

    public static int getOpenLv(Common.EnumFunction function) {
        int defaultLv = GameConfig.getById(GameConst.CONFIG_ID).getDefaultlv();
        if (function == null) {
            return defaultLv;
        }

        FunctionOpenLvConfigObject cfg = getById(function.getNumber());
        if (cfg == null) {
            return defaultLv;
        }
        return cfg.getUnlockneed();
    }

}
