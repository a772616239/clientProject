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
import org.apache.commons.lang.math.RandomUtils;

@annationInit(value = "MistMonsterDropBox", methodname = "initConfig")
public class MistMonsterDropBox extends baseConfig<MistMonsterDropBoxObject> {


    private static MistMonsterDropBox instance = null;

    public static MistMonsterDropBox getInstance() {

        if (instance == null)
            instance = new MistMonsterDropBox();
        return instance;

    }


    public static Map<Integer, MistMonsterDropBoxObject> _ix_groupid = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistMonsterDropBox) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistMonsterDropBox");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistMonsterDropBoxObject getByGroupid(int groupid) {

        return _ix_groupid.get(groupid);

    }


    public void putToMem(Map e, MistMonsterDropBoxObject config) {

        config.setGroupid(MapHelper.getInt(e, "GroupId"));

        config.setDropboxgroup(MapHelper.getIntArray(e, "DropBoxGroup"));

        config.setExisttime(MapHelper.getInt(e, "ExistTime"));


        _ix_groupid.put(config.getGroupid(), config);
    }

    public static int getDropBoxUnitCfgId(MistMonsterDropBoxObject cfg) {
        if (cfg == null || cfg.getTotalOdds() <= 0) {
            return 0;
        }
        List<List<Integer>> dropBoxGroup = cfg.getDropboxgroup();
        if (dropBoxGroup == null) {
            return 0;
        }
        int sum = 0;
        int rand = RandomUtils.nextInt(cfg.getTotalOdds());
        for (List<Integer> dropBoxInfo : dropBoxGroup) {
            sum += dropBoxInfo.get(0);
            if (rand >= sum) {
                continue;
            }
            return dropBoxInfo.get(1);
        }
        return 0;
    }
}
