/*CREATED BY TOOL*/

package cfg;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import org.apache.commons.lang.ArrayUtils;

@annationInit(value = "BossTowerBuyTimeConsume", methodname = "initConfig")
public class BossTowerBuyTimeConsume extends baseConfig<BossTowerBuyTimeConsumeObject> {


    private static BossTowerBuyTimeConsume instance = null;

    public static BossTowerBuyTimeConsume getInstance() {

        if (instance == null)
            instance = new BossTowerBuyTimeConsume();
        return instance;

    }


    public static Map<Integer, BossTowerBuyTimeConsumeObject> _ix_id = new HashMap<>();

    @Getter
    private static int[] maxConsume ;


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (BossTowerBuyTimeConsume) o;
        initConfig();
        Optional<BossTowerBuyTimeConsumeObject> cfg = BossTowerBuyTimeConsume._ix_id.values().stream().max(new Comparator<BossTowerBuyTimeConsumeObject>() {
            @Override
            public int compare(BossTowerBuyTimeConsumeObject o1, BossTowerBuyTimeConsumeObject o2) {
                return o1.getId() - o2.getId();
            }
        });
        if (!cfg.isPresent()|| ArrayUtils.isEmpty(cfg.get().getConsume())){
            throw new RuntimeException("BossTowerBuyTimeConsume init max consume error");
        }
        maxConsume =cfg.get().getConsume();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "BossTowerBuyTimeConsume");

        for (Map e : ret) {
            put(e);
        }

    }

    public static BossTowerBuyTimeConsumeObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, BossTowerBuyTimeConsumeObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setConsume(MapHelper.getInts(e, "consume"));


        _ix_id.put(config.getId(), config);


    }
}
