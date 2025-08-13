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

@annationInit(value = "CumuSignIn", methodname = "initConfig")
public class CumuSignIn extends baseConfig<CumuSignInObject> {


    private static CumuSignIn instance = null;

    public static CumuSignIn getInstance() {

        if (instance == null)
            instance = new CumuSignIn();
        return instance;

    }


    public static Map<Integer, CumuSignInObject> _ix_days = new HashMap<Integer, CumuSignInObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CumuSignIn) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CumuSignIn");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CumuSignInObject getByDays(int days) {

        return _ix_days.get(days);

    }


    public void putToMem(Map e, CumuSignInObject config) {

        config.setDays(MapHelper.getInt(e, "days"));

        config.setRewards(MapHelper.getIntArray(e, "rewards"));


        _ix_days.put(config.getDays(), config);

        maxDays = Math.max(config.getDays(), maxDays);
    }

    /**
     * =======================================
     */

    public int maxDays = 0;
}
