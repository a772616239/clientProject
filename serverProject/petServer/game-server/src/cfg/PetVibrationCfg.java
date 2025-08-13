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

@annationInit(value = "PetVibrationCfg", methodname = "initConfig")
public class PetVibrationCfg extends baseConfig<PetVibrationCfgObject> {


    private static PetVibrationCfg instance = null;

    public static PetVibrationCfg getInstance() {

        if (instance == null)
            instance = new PetVibrationCfg();
        return instance;

    }


    public static Map<Integer, PetVibrationCfgObject> _ix_id = new HashMap<>();

    //<petBookId,List<PetVibrationCfgObject>> 当前宠物id对应 包含当前宠物id的共鸣配置
    public static Map<Integer, List<PetVibrationCfgObject>> petVibrationMap = new HashMap<>();

    public static int queryPetBuff(PetVibrationCfgObject cfg, int petBookId) {
        int index = cfg.getNeedPetList().indexOf(petBookId);
        if (index == -1) {
            return 0;
        }
        return cfg.getBufflist()[index];

    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetVibrationCfg) o;
        initConfig();
        initPetVibrationMap();
    }

    private void initPetVibrationMap() {
        List<PetVibrationCfgObject> petVibrationCfgList;
        for (PetVibrationCfgObject cfg : _ix_id.values()) {
            for (Integer petId : cfg.getNeedPetList()) {
                petVibrationCfgList = petVibrationMap.computeIfAbsent(petId, a -> new ArrayList<>());
                petVibrationCfgList.add(cfg);
            }

        }

    }

    public static List<PetVibrationCfgObject> getPetVibrationCfg(int petBookId) {
        return petVibrationMap.get(petBookId);
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetVibrationCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetVibrationCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetVibrationCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setNeedpet(MapHelper.getIntArray(e, "needPet"));

        config.setBufflist(MapHelper.getInts(e, "buffList"));


        _ix_id.put(config.getId(), config);


    }
}
