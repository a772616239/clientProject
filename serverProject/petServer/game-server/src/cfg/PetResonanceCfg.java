/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.Arrays;
import model.base.baseConfig;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@annationInit(value = "PetResonanceCfg", methodname = "initConfig")
public class PetResonanceCfg extends baseConfig<PetResonanceCfgObject> {


    private static PetResonanceCfg instance = null;

    public static PetResonanceCfg getInstance() {

        if (instance == null)
            instance = new PetResonanceCfg();
        return instance;

    }


    public static Map<Integer, PetResonanceCfgObject> _ix_id = new HashMap<>();


    public static Map<Integer, List<PetResonanceCfgObject>> resonanceMap;

    public static Map<Integer, List<Integer>> resonanceGroupMap=new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetResonanceCfg) o;
        initConfig();

        resonanceMap = _ix_id.values().stream().filter(e->e.getId()>0).collect(Collectors.groupingBy(PetResonanceCfgObject::getGroupid));

        for (Map.Entry<Integer, List<PetResonanceCfgObject>> entry : resonanceMap.entrySet()) {
            entry.getValue().sort(Comparator.comparingInt(PetResonanceCfgObject::getLevel).reversed());
            List<Integer> needPetBookIds = Arrays.stream(entry.getValue().get(0).getNeedpet()).map(e -> e[0]).collect(Collectors.toList());
            resonanceGroupMap.put(entry.getKey(),needPetBookIds);
        }
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetResonanceCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetResonanceCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetResonanceCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setLevel(MapHelper.getInt(e, "level"));

        config.setGroupid(MapHelper.getInt(e, "groupId"));

        config.setNeedpet(MapHelper.getIntArray(e, "needPet"));

        config.setBufflist(MapHelper.getInts(e, "buffList"));


        _ix_id.put(config.getId(), config);


    }
}
