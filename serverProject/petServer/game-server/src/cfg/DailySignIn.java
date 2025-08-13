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

@annationInit(value = "DailySignIn", methodname = "initConfig")
public class DailySignIn extends baseConfig<DailySignInObject> {


    private static DailySignIn instance = null;

    public static DailySignIn getInstance() {

        if (instance == null)
            instance = new DailySignIn();
        return instance;

    }


    public static Map<Integer, DailySignInObject> _ix_cumusignindays = new HashMap<Integer, DailySignInObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (DailySignIn) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "DailySignIn");

        for (Map e : ret) {
            put(e);
        }

    }

    public static DailySignInObject getByCumusignindays(int cumusignindays) {

        return _ix_cumusignindays.get(cumusignindays);

    }


    public void putToMem(Map e, DailySignInObject config) {

        config.setCumusignindays(MapHelper.getInt(e, "cumuSignInDays"));

        config.setSigninreward(MapHelper.getIntArray(e, "signInReward"));

        config.setTemplateid(MapHelper.getInt(e, "templateId"));


        _ix_cumusignindays.put(config.getCumusignindays(), config);


    }
}
