/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import model.consume.ConsumeUtil;
import protocol.Common;

@annationInit(value = "PetRuneBlessPropertyCfg", methodname = "initConfig")
public class PetRuneBlessPropertyCfg extends baseConfig<PetRuneBlessPropertyCfgObject> {


    private static PetRuneBlessPropertyCfg instance = null;

    public static PetRuneBlessPropertyCfg getInstance() {

        if (instance == null)
            instance = new PetRuneBlessPropertyCfg();
        return instance;

    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneBlessPropertyCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneBlessPropertyCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetRuneBlessPropertyCfgObject getByRunerarity(int runerarity) {

        return _ix_runerarity.get(runerarity);

    }


    public void putToMem(Map e, PetRuneBlessPropertyCfgObject config) {

        config.setRunerarity(MapHelper.getInt(e, "runeRarity"));

        config.setBlessconsume(MapHelper.getIntArray(e, "blessConsume"));

        config.setRatingbase(MapHelper.getIntArray(e, "ratingBase"));

        config.setFinalproperty(MapHelper.getIntArray(e, "finalProperty"));


        _ix_runerarity.put(config.getRunerarity(), config);


    }


    public static Map<Integer, PetRuneBlessPropertyCfgObject> _ix_runerarity = new HashMap<>();

    public static List<Common.Consume> getBlessConsume(int rarity) {
        PetRuneBlessPropertyCfgObject cfg = getByRunerarity(rarity);
        if (cfg == null) {
            return Collections.emptyList();
        }
        return ConsumeUtil.parseToConsumeList(cfg.getBlessconsume());
    }


}
