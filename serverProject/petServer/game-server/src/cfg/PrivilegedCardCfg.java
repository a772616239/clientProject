/*CREATED BY TOOL*/

package cfg;

import common.GameConst;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MonthCard;
import protocol.PlayerDB;

@annationInit(value = "PrivilegedCardCfg", methodname = "initConfig")
public class PrivilegedCardCfg extends baseConfig<PrivilegedCardCfgObject> {


    private static PrivilegedCardCfg instance = null;

    public static PrivilegedCardCfg getInstance() {

        if (instance == null)
            instance = new PrivilegedCardCfg();
        return instance;

    }


    public static Map<Integer, PrivilegedCardCfgObject> _ix_id = new HashMap<>();

    public static Map<Integer, Map<MonthCard.PrivilegedCardFunction, Integer>> cardFunction = new HashMap<>();

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PrivilegedCardCfg) o;
        initConfig();
    }

    public int queryPrivilegedNum(List<PlayerDB.DB_PrivilegedCard> cardList, MonthCard.PrivilegedCardFunction function) {
        int result = 0;
        if (CollectionUtils.isEmpty(cardList)) {
            return result;
        }
        for (PlayerDB.DB_PrivilegedCard card : cardList) {
            Map<MonthCard.PrivilegedCardFunction, Integer> map = cardFunction.get(card.getCarId());
            if (map == null) {
                continue;
            }
            Integer count = map.get(function);
            if (count == null) {
                continue;
            }
            result += count;

        }
        return result;
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PrivilegedCardCfg");

        for (Map e : ret) {
            put(e);
        }
        for (PrivilegedCardCfgObject cfg : _ix_id.values()) {
            if (cfg.getId() <= 0) {
                continue;
            }
            Map<MonthCard.PrivilegedCardFunction, Integer> function = cardFunction.computeIfAbsent(cfg.getId(), a -> new HashMap<>());
            for (int[] ints : cfg.getAddcount()) {
                function.put(MonthCard.PrivilegedCardFunction.forNumber(ints[0]), ints[1]);
            }
        }
    }

    public static PrivilegedCardCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PrivilegedCardCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setExpiredays(MapHelper.getInt(e, "expireDays"));

        config.setInstantreward(MapHelper.getIntArray(e, "instantReward"));

        config.setAddcount(MapHelper.getIntArray(e, "addCount"));


        _ix_id.put(config.getId(), config);


    }
}
