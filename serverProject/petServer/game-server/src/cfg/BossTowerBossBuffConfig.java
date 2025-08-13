/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import model.base.baseConfig;

@annationInit(value = "BossTowerBossBuffConfig", methodname = "initConfig")
public class BossTowerBossBuffConfig extends baseConfig<BossTowerBossBuffConfigObject> {


    private static BossTowerBossBuffConfig instance = null;

    public static BossTowerBossBuffConfig getInstance() {

        if (instance == null)
            instance = new BossTowerBossBuffConfig();
        return instance;

    }


    public static Map<Integer, BossTowerBossBuffConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (BossTowerBossBuffConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "BossTowerBossBuffConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static BossTowerBossBuffConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, BossTowerBossBuffConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setPrefixtype(MapHelper.getInt(e, "prefixType"));

        config.setLinkbuffdisid(MapHelper.getInt(e, "linkBuffDisId"));

        config.setWeight(MapHelper.getInt(e, "weight"));

        config.setEffectcamp(MapHelper.getInt(e, "effectCamp"));


        _ix_id.put(config.getId(), config);


    }

    /**
     * 更具linkDisBuffId 获取buff生效阵营
     * @param linkBuffId
     * @return
     */
    public static int getBuffCampByLinkBuffId(int linkBuffId) {
        for (BossTowerBossBuffConfigObject value : _ix_id.values()) {
            if (Objects.equals(value.getLinkbuffdisid(), linkBuffId)) {
                return value.getEffectcamp();
            }
        }
        return -1;
    }
}
