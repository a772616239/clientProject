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

@annationInit(value = "BraveChallengeDiffAdjust", methodname = "initConfig")
public class BraveChallengeDiffAdjust extends baseConfig<BraveChallengeDiffAdjustObject> {


    private static BraveChallengeDiffAdjust instance = null;

    public static BraveChallengeDiffAdjust getInstance() {

        if (instance == null)
            instance = new BraveChallengeDiffAdjust();
        return instance;

    }


    public static Map<Integer, BraveChallengeDiffAdjustObject> _ix_id = new HashMap<Integer, BraveChallengeDiffAdjustObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (BraveChallengeDiffAdjust) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "BraveChallengeDiffAdjust");

        for (Map e : ret) {
            put(e);
        }

    }

    public static BraveChallengeDiffAdjustObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, BraveChallengeDiffAdjustObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setDiffadjust(MapHelper.getInt(e, "diffAdjust"));


        _ix_id.put(config.getId(), config);

        if (maxFailureTimes == 0 || config.getId() > maxFailureTimes) {
            maxFailureTimes = config.getId();
        }
    }

    private static int maxFailureTimes;

    public static int getByFailureTimes(int failureTimes) {
        if (failureTimes <= 0) {
            return 0;
        }
        if (failureTimes >= maxFailureTimes) {
            return getById(maxFailureTimes).getDiffadjust();
        }
        return getById(failureTimes).getDiffadjust();
    }
}
