/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import org.apache.commons.collections4.CollectionUtils;
import util.LogUtil;

@annationInit(value = "NewForeignInvasionWaveConfig", methodname = "initConfig")
public class NewForeignInvasionWaveConfig extends baseConfig<NewForeignInvasionWaveConfigObject> {


    private static NewForeignInvasionWaveConfig instance = null;

    public static NewForeignInvasionWaveConfig getInstance() {

        if (instance == null)
            instance = new NewForeignInvasionWaveConfig();
        return instance;

    }


    public static Map<Integer, NewForeignInvasionWaveConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (NewForeignInvasionWaveConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "NewForeignInvasionWaveConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static NewForeignInvasionWaveConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, NewForeignInvasionWaveConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setLinkbuildingid(MapHelper.getInt(e, "linkBuildingId"));

        config.setWave(MapHelper.getInt(e, "wave"));

        config.setLvaddition(MapHelper.getInt(e, "lvAddition"));

        config.setExpropertyaddition(MapHelper.getInt(e, "exPropertyAddition"));

        config.setBattlerewards(MapHelper.getInt(e, "battleRewards"));


        _ix_id.put(config.getId(), config);


        putToMap(config);
    }

    private static final Map<Integer, List<NewForeignInvasionWaveConfigObject>> BUILDING_ID_WAVE_MAP = new HashMap<>();

    private static boolean SORTED = false;

    private void putToMap(NewForeignInvasionWaveConfigObject object) {
        BUILDING_ID_WAVE_MAP.computeIfAbsent(object.getLinkbuildingid(), e -> new ArrayList<>()).add(object);
    }

    private void sortMapList() {
        for (List<NewForeignInvasionWaveConfigObject> value : BUILDING_ID_WAVE_MAP.values()) {
            value.sort(Comparator.comparingInt(NewForeignInvasionWaveConfigObject::getWave));
        }
        SORTED = true;
    }

    public NewForeignInvasionWaveConfigObject getBuildingWaveCfg(int buildingId, int wave) {
        if (!SORTED) {
            sortMapList();
        }
        NewForeignInvasionWaveConfigObject result = null;
        List<NewForeignInvasionWaveConfigObject> cfgList = BUILDING_ID_WAVE_MAP.get(buildingId);
        if (CollectionUtils.isNotEmpty(cfgList)) {
            if (wave >= cfgList.size()) {
                result = cfgList.get(cfgList.size() - 1);
            } else {
                result = cfgList.get(wave - 1);
            }
        }

        if (result == null) {
            LogUtil.error("cfg.NewForeignInvasionWaveConfig.getBuildingWaveCfg," +
                    " can not find building wave, buildingId:" + buildingId + ", wave:" + wave);
        }
        return result;
    }
}
