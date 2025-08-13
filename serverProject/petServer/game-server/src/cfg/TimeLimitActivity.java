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
import util.ArrayUtil;

@annationInit(value = "TimeLimitActivity", methodname = "initConfig")
public class TimeLimitActivity extends baseConfig<TimeLimitActivityObject> {


    private static TimeLimitActivity instance = null;

    public static TimeLimitActivity getInstance() {

        if (instance == null)
            instance = new TimeLimitActivity();
        return instance;

    }


    public static Map<Integer, TimeLimitActivityObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TimeLimitActivity) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TimeLimitActivity");

        for (Map e : ret) {
            put(e);
        }

    }

    public static TimeLimitActivityObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, TimeLimitActivityObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setPicture(MapHelper.getStr(e, "picture"));

        config.setTitle(MapHelper.getInt(e, "title"));

        config.setDesc(MapHelper.getInt(e, "desc"));

        config.setDetail(MapHelper.getInt(e, "detail"));

        config.setEnddistime(MapHelper.getInt(e, "endDisTime"));

        config.setOpenlv(MapHelper.getInt(e, "openLv"));

        config.setShowlv(MapHelper.getInt(e, "showLv"));

        config.setTasklist(MapHelper.getInts(e, "taskList"));

        config.setTabtype(MapHelper.getInt(e, "tabType"));

        config.setReddottype(MapHelper.getInt(e, "redDotType"));


        _ix_id.put(config.getId(), config);


    }

    public static int getTaskLinkActivityId(int timeLimitTaskId) {
        TimeLimitActivityObject activity = getTaskLinkActivity(timeLimitTaskId);
        return activity == null ? -1 : activity.getId();
    }

    public static TimeLimitActivityObject getTaskLinkActivity(int timeLimitTaskId) {
        for (TimeLimitActivityObject value : _ix_id.values()) {
            if (ArrayUtil.intArrayContain(value.getTasklist(), timeLimitTaskId)) {
                return value;
            }
        }
        return null;
    }
}
