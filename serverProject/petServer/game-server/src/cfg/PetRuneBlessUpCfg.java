/*CREATED BY TOOL*/

package cfg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javafx.util.Pair;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import util.ArrayUtil;

@annationInit(value = "PetRuneBlessUpCfg", methodname = "initConfig")
public class PetRuneBlessUpCfg extends baseConfig<PetRuneBlessUpCfgObject> {


    private static PetRuneBlessUpCfg instance = null;

    public static PetRuneBlessUpCfg getInstance() {

        if (instance == null)
            instance = new PetRuneBlessUpCfg();
        return instance;

    }


    public static Map<Integer, PetRuneBlessUpCfgObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneBlessUpCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneBlessUpCfg");

        for (Map e : ret) {
            put(e);
        }

        initProbability();
    }

    private static Map<Integer, Integer> values =new HashMap<>();

    private void initProbability() {
        values = _ix_id.values().stream().collect(Collectors
                .groupingBy(PetRuneBlessUpCfgObject::getRunerarity, Collectors.summingInt(PetRuneBlessUpCfgObject::getProbability)));

    }

    public static PetRuneBlessUpCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetRuneBlessUpCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setRunerarity(MapHelper.getInt(e, "runeRarity"));

        config.setItemrating(MapHelper.getInt(e, "itemRating"));

        config.setOfferproperty(MapHelper.getIntArray(e, "offerProperty"));

        config.setProbability(MapHelper.getInt(e, "probability"));

        _ix_id.put(config.getId(), config);


    }

    private static final Pair<Integer, Integer>  defalutPair = new Pair(0,0);

    public Pair<Integer, Integer> randomBless(int runRarity, int propertyType) {
        Integer totalWeight = values.get(runRarity);
        if (totalWeight == null) {
            return defalutPair;
        }
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        for (PetRuneBlessUpCfgObject cfg : _ix_id.values()) {
            if (cfg.getRunerarity() != runRarity) {
                continue;
            }
            if (random < cfg.getProbability()) {
                return new Pair(cfg.getItemrating(), ArrayUtil.getValueFromKeyValueIntArray(cfg.getOfferproperty(), propertyType));
            }
            random -= cfg.getProbability();
        }
        return defalutPair;
    }



}
