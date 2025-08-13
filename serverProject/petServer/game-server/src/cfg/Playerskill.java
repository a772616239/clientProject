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


@annationInit(value = "Playerskill", methodname = "initConfig")
public class Playerskill extends baseConfig<PlayerskillObject> {


    private static Playerskill instance = null;

    public static Playerskill getInstance() {

        if (instance == null)
            instance = new Playerskill();
        return instance;

    }


    public static Map<Integer, PlayerskillObject> _ix_id = new HashMap<Integer, PlayerskillObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (Playerskill) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "Playerskill");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PlayerskillObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PlayerskillObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setName(MapHelper.getInt(e, "Name"));

        config.setSkillres(MapHelper.getStr(e, "SkillRes"));

        config.setUnlock(MapHelper.getInt(e, "UnLock"));

        config.setDesc(MapHelper.getInt(e, "Desc"));


        _ix_id.put(config.getId(), config);


    }
}
