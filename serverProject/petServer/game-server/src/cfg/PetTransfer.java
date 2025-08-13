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

@annationInit(value = "PetTransfer", methodname = "initConfig")
public class PetTransfer extends baseConfig<PetTransferObject> {


    private static PetTransfer instance = null;

    public static PetTransfer getInstance() {

        if (instance == null)
            instance = new PetTransfer();
        return instance;

    }


    public static Map<Integer, PetTransferObject> _ix_id = new HashMap<Integer, PetTransferObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetTransfer) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetTransfer");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetTransferObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetTransferObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setTargetpetid(MapHelper.getInt(e, "targetPetId"));

        config.setOdds(MapHelper.getInt(e, "odds"));


        _ix_id.put(config.getId(), config);


    }
}
