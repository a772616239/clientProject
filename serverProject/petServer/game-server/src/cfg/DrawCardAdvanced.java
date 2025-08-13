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
import java.util.Random;
import model.base.baseConfig;
import org.apache.commons.collections4.CollectionUtils;
import util.LogUtil;

@annationInit(value = "DrawCardAdvanced", methodname = "initConfig")
public class DrawCardAdvanced extends baseConfig<DrawCardAdvancedObject> {


    private static DrawCardAdvanced instance = null;

    public static DrawCardAdvanced getInstance() {

        if (instance == null)
            instance = new DrawCardAdvanced();
        return instance;

    }


    public static Map<Integer, DrawCardAdvancedObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (DrawCardAdvanced) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "DrawCardAdvanced");

        for (Map e : ret) {
            put(e);
        }

    }

    public static DrawCardAdvancedObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, DrawCardAdvancedObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setGroup(MapHelper.getIntArray(e, "group"));

        config.setWeight(MapHelper.getInt(e, "weight"));

        config.setInnermustpool(MapHelper.getIntArray(e, "innerMustPool"));


        _ix_id.put(config.getId(), config);

        init(config);
    }

    private static int totalOdds;
    private static List<DrawCardAdvancedObject> advanceList;

    private void init(DrawCardAdvancedObject object) {
        if (object == null || object.getId() <= 0) {
            return;
        }

        if (advanceList == null) {
            advanceList = new ArrayList<>();
        }
        advanceList.add(object);
        totalOdds += object.getWeight();
    }

    public static DrawCardAdvancedObject randomAdvance() {
        if (CollectionUtils.isEmpty(advanceList)) {
            LogUtil.error("cfg.DrawCardAdvanced.randomAdvance, advance list is empty");
            return null;
        }

        Random random = new Random();
        if (totalOdds <= 0) {
            return advanceList.get(random.nextInt(advanceList.size()));
        } else {
            int randomNum = random.nextInt(totalOdds);
            int curNum = 0;
            for (DrawCardAdvancedObject object : advanceList) {
                if ((curNum += object.getWeight()) > randomNum) {
                    return object;
                }
            }
        }

        LogUtil.error("cfg.DrawCardAdvanced.randomAdvance, random failed");
        return advanceList.get(random.nextInt(advanceList.size()));
    }
}
