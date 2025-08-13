/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import model.base.baseConfig;

@annationInit(value = "TheWarItemConfig", methodname = "initConfig")
public class TheWarItemConfig extends baseConfig<TheWarItemConfigObject> {


    private static TheWarItemConfig instance = null;

    public static TheWarItemConfig getInstance() {

        if (instance == null)
            instance = new TheWarItemConfig();
        return instance;

    }


    public static Map<Integer, TheWarItemConfigObject> _ix_itemid = new HashMap<Integer, TheWarItemConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (TheWarItemConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "TheWarItemConfig");

        for (Map e : ret) {
            put(e);
        }
        calcAllItemComposeCost();
    }

    public static TheWarItemConfigObject getByItemid(int itemid) {

        return _ix_itemid.get(itemid);

    }


    public void putToMem(Map e, TheWarItemConfigObject config) {

        config.setItemid(MapHelper.getInt(e, "ItemId"));

        config.setPosdefine(MapHelper.getInt(e, "PosDefine"));

        config.setProdefine(MapHelper.getInt(e, "ProDefine"));

        config.setQuality(MapHelper.getInt(e, "Quality"));

        config.setComposite(MapHelper.getIntArray(e, "Composite"));

        config.setPrice(MapHelper.getInt(e, "Price"));

        config.setBuffid(MapHelper.getInt(e, "BuffID"));


        _ix_itemid.put(config.getItemid(), config);

    }

    public void calcAllItemComposeCost() {
        int sumCost;
        TheWarItemConfigObject itemCfg;
        for (TheWarItemConfigObject config: _ix_itemid.values()) {
            sumCost = 0;
            for (Entry<Integer, Integer> entry : config.getComposite().entrySet()) {
                itemCfg = _ix_itemid.get(entry.getKey());
                if (itemCfg == null) {
                    continue;
                }
                sumCost += itemCfg.getPrice() * entry.getValue();
            }
            config.setTotalCost(sumCost);
        }
    }
}
