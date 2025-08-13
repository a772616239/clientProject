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
import protocol.Common.LanguageEnum;

@annationInit(value = "HeadBorder", methodname = "initConfig")
public class HeadBorder extends baseConfig<HeadBorderObject> {


    private static HeadBorder instance = null;

    public static HeadBorder getInstance() {

        if (instance == null)
            instance = new HeadBorder();
        return instance;

    }


    public static Map<Integer, HeadBorderObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (HeadBorder) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "HeadBorder");

        for (Map e : ret) {
            put(e);
        }

    }

    public static HeadBorderObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, HeadBorderObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        config.setIsdefault(MapHelper.getBoolean(e, "IsDefault"));

        config.setExpiretime(MapHelper.getInt(e, "expireTime"));

        config.setName(MapHelper.getInt(e, "name"));


        _ix_id.put(config.getId(), config);

        if (config.getIsdefault()) {
            defaultHeadBorder = config.getId();
        }

        if (config.getExpiretime() != -1) {
            TIME_LIMIT_AVATAR_CFG_Map.put(config.getId(), config);
        }
    }

    private static int defaultHeadBorder;

    private static final Map<Integer, HeadBorderObject> TIME_LIMIT_AVATAR_CFG_Map = new HashMap<>();

    public static int getDefaultHeadBorder() {
        return defaultHeadBorder;
    }

    public static String getName(int cfgId, LanguageEnum language) {
        HeadBorderObject cfg = getById(cfgId);
        return language == null ? "" : ServerStringRes.getContentByLanguage(cfg.getName(), language);
    }

    public static Map<Integer, HeadBorderObject> getTimeLimitAvatarCfgMap() {
        return TIME_LIMIT_AVATAR_CFG_Map;
    }
}
