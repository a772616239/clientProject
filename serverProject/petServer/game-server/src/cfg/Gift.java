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
import model.base.baseConfig;
import protocol.ExchangeHistory.ExchangeMessageEnum;


@annationInit(value = "Gift", methodname = "initConfig")
public class Gift extends baseConfig<GiftObject> {


    private static Gift instance = null;

    public static Gift getInstance() {

        if (instance == null)
            instance = new Gift();
        return instance;

    }


    public static Map<Integer, GiftObject> _ix_id = new HashMap<Integer, GiftObject>();
    public static GiftObject First_Gift;
    public static List<GiftObject> Limit_Gift = new ArrayList<>();
    public static List<GiftObject> Worth_Gift = new ArrayList<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (Gift) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "Gift");

        for (Map e : ret) {
            put(e);
        }

    }

    public static GiftObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, GiftObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setType(MapHelper.getInt(e, "type"));

        config.setConsume(MapHelper.getInts(e, "consume"));

        config.setReward(MapHelper.getIntArray(e, "reward"));

        config.setLimit(MapHelper.getInt(e, "limit"));

        config.setVipexp(MapHelper.getInt(e, "vipExp"));


        _ix_id.put(config.getId(), config);

        if (config.getType() == ExchangeMessageEnum.FIRST_GIFT_VALUE) {
            First_Gift = config;
        }
        if (config.getType() == ExchangeMessageEnum.LIMIT_GIFT_VALUE) {
            Limit_Gift.add(config);
        }
        if (config.getType() == ExchangeMessageEnum.WORTY_GIFT_VALUE) {
            Worth_Gift.add(config);
        }
    }
}
