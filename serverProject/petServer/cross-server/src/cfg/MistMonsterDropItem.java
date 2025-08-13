/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import org.apache.commons.lang.math.RandomUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "MistMonsterDropItem", methodname = "initConfig")
public class MistMonsterDropItem extends baseConfig<MistMonsterDropItemObject> {


    private static MistMonsterDropItem instance = null;

    public static MistMonsterDropItem getInstance() {

        if (instance == null)
            instance = new MistMonsterDropItem();
        return instance;

    }


    public static Map<Integer, MistMonsterDropItemObject> _ix_groupid = new HashMap<Integer, MistMonsterDropItemObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistMonsterDropItem) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistMonsterDropItem");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistMonsterDropItemObject getByGroupid(int groupid) {

        return _ix_groupid.get(groupid);

    }


    public void putToMem(Map e, MistMonsterDropItemObject config) {

        config.setGroupid(MapHelper.getInt(e, "GroupId"));

        config.setDropitemgroup(MapHelper.getIntArray(e, "DropItemGroup"));


        _ix_groupid.put(config.getGroupid(), config);


    }

    public static int getDropItemType(int groupId) {
        MistMonsterDropItemObject cfg = getByGroupid(groupId);
        if (cfg == null || cfg.getTotalOdds() <= 0) {
            return 0;
        }
        List<List<Integer>> dropItemGroup = cfg.getDropitemgroup();
        if (dropItemGroup == null) {
            return 0;
        }
        int sum = 0;
        int rand = RandomUtils.nextInt(cfg.getTotalOdds());
        for (List<Integer> dropItemInfo : dropItemGroup) {
            sum += dropItemInfo.get(0);
            if (rand >= sum) {
                continue;
            }
            return dropItemInfo.get(1);
        }
        return 0;
    }
}
