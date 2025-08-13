/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PetGemConfigAdvance", methodname = "initConfig")
public class PetGemConfigAdvance extends baseConfig<PetGemConfigAdvanceObject> {


    private static PetGemConfigAdvance instance = null;

    public static PetGemConfigAdvance getInstance() {

        if (instance == null)
            instance = new PetGemConfigAdvance();
        return instance;

    }


    public static Map<String, PetGemConfigAdvanceObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetGemConfigAdvance) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetGemConfigAdvance");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetGemConfigAdvanceObject getById(String id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetGemConfigAdvanceObject config) {

        config.setId(MapHelper.getStr(e, "id"));

        config.setRarity(MapHelper.getInt(e, "rarity"));

        config.setGemtype(MapHelper.getInt(e, "gemtype"));

        config.setStar(MapHelper.getInt(e, "star"));

        config.setAdvanceproperties(MapHelper.getIntArray(e, "advanceProperties"));

        config.setAdvancesourceconsume(MapHelper.getIntArray(e, "advanceSourceConsume"));

        config.setAdvancegemconsume(MapHelper.getIntArray(e, "advanceGemConsume"));

        config.setGemsale(MapHelper.getIntArray(e, "gemSale"));

        config.setGemsaleconsume(MapHelper.getInts(e, "gemSaleConsume"));


        _ix_id.put(config.getId(), config);


    }

    public static PetGemConfigAdvanceObject getByGemConfigId(int gemCfgId) {
        PetGemConfigObject gemConfig = PetGemConfig.getById(gemCfgId);
        if (gemConfig == null) {
            return null;
        }
        for (PetGemConfigAdvanceObject advanceConfig : _ix_id.values()) {
            if (advanceConfig.getRarity() == gemConfig.getRarity()
                    && advanceConfig.getGemtype() == gemConfig.getGemtype()
                    && advanceConfig.getStar() == gemConfig.getStar()) {
                return advanceConfig;
            }
        }
        return null;
    }
}
