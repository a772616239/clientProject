/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.*;
import model.base.baseConfig;

@annationInit(value = "PetMissionDescription", methodname = "initConfig")
public class PetMissionDescription extends baseConfig<PetMissionDescriptionObject> {


    private static PetMissionDescription instance = null;

    public static PetMissionDescription getInstance() {

        if (instance == null)
            instance = new PetMissionDescription();
        return instance;

    }


    public static Map<Integer, PetMissionDescriptionObject> _ix_id = new HashMap<Integer, PetMissionDescriptionObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetMissionDescription) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetMissionDescription");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetMissionDescriptionObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetMissionDescriptionObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setStringsourceid(MapHelper.getInt(e, "stringSourceId"));


        _ix_id.put(config.getId(), config);

    }


    public static List<PetMissionDescriptionObject> randomList;

    public static int randomDescriptionId() {
        if (randomList == null) {
            randomList = new ArrayList<>(_ix_id.values());
        }

        PetMissionDescriptionObject descriptionObject = randomList.get(new Random().nextInt(randomList.size()));
        if (descriptionObject == null) {
            descriptionObject =  randomList.get(0);
        }
        return descriptionObject.getStringsourceid();
    }
}
