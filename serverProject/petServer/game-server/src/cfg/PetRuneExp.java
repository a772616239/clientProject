/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.stream.Collectors;
import model.base.baseConfig;
import model.petrune.PetRuneManager;
import protocol.PetMessage.Rune.Builder;
import protocol.PetMessage.RuneProperties;
import util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "PetRuneExp", methodname = "initConfig")
public class PetRuneExp extends baseConfig<PetRuneExpObject> {


    private static PetRuneExp instance = null;

    public static PetRuneExp getInstance() {

        if (instance == null)
            instance = new PetRuneExp();
        return instance;

    }


    public static Map<Integer, PetRuneExpObject> _ix_key = new HashMap<>();

    public static Map<Integer, Integer> rarityLevelMap = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneExp) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneExp");

        for (Map e : ret) {
            put(e);
        }
        initRuneMaxLv();
    }

    public static int queryRuneMaxLv(int rarity) {
        Integer lv = rarityLevelMap.get(rarity);
        return lv == null ? -1 : lv;
    }

    private void initRuneMaxLv() {
        Map<Integer, List<PetRuneExpObject>> collect = _ix_key.values().stream().collect(Collectors.groupingBy(PetRuneExpObject::getRarity));
        rarityLevelMap.clear();
        int rarity;
        int maxLv;
        for (Map.Entry<Integer, List<PetRuneExpObject>> entry : collect.entrySet()) {
            rarity = entry.getKey();
            maxLv = entry.getValue().stream().mapToInt(PetRuneExpObject::getRunelvl).max().orElse(0);
            rarityLevelMap.put(rarity, maxLv);
        }
    }

    public static PetRuneExpObject getByKey(int key) {

        return _ix_key.get(key);

    }


    public void putToMem(Map e, PetRuneExpObject config) {

        config.setRunelvl(MapHelper.getInt(e, "runeLvl"));

        config.setRunetype(MapHelper.getInt(e, "runeType"));

        config.setNextlvlexp(MapHelper.getInt(e, "nextLvlExp"));

        config.setAccumexp(MapHelper.getInt(e, "accumExp"));

        config.setNewproperties(MapHelper.getInt(e, "newProperties"));

        config.setRarity(MapHelper.getInt(e, "rarity"));

        config.setKey(MapHelper.getInt(e, "key"));

        config.setBaseproperties(MapHelper.getIntArray(e, "baseProperties"));

        config.setExbaseproperties(MapHelper.getIntArray(e, "exbaseproperties"));


        _ix_key.put(config.getKey(), config);


    }

    public static void refreshBaseProperty(Builder rune) {
        PetRunePropertiesObject runeConfig = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (runeConfig == null) {
            LogUtil.error("refreshBaseProperty can`t find PetRuneProperties by runeBookId:{}", rune.getRuneBookId());
            return;
        }
        RuneProperties runeBaseProperties = PetRuneManager.getInstance().queryRuneBaseProperties(runeConfig.getRunerarity(), rune.getRuneLvl(), runeConfig.getRunetype());
        if (runeBaseProperties == null) {
            LogUtil.error("refreshBaseProperty can`t find PetRuneProperties by rarity:{},lv:{},type:{}", runeConfig.getRunerarity(), rune.getRuneLvl(), runeConfig.getRunetype());
            return;
        }
        rune.setRuneBaseProperty(runeBaseProperties);
    }

    public static PetRuneExpObject getByRarityAndLvlAndType(int rarity, int runeLvl, int runeType) {
        for (PetRuneExpObject value : _ix_key.values()) {
            if (value.getRarity() == rarity && value.getRunelvl() == runeLvl && value.getRunetype() == runeType) {
                return value;
            }
        }
        return null;
    }


}
