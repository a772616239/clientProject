/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.Optional;
import model.base.baseConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.LogUtil;

@annationInit(value = "PetFragmentConfig", methodname = "initConfig")
public class PetFragmentConfig extends baseConfig<PetFragmentConfigObject> {


    private static PetFragmentConfig instance = null;

    public static PetFragmentConfig getInstance() {

        if (instance == null)
            instance = new PetFragmentConfig();
        return instance;

    }


    public static Map<Integer, PetFragmentConfigObject> _ix_id = new HashMap<Integer, PetFragmentConfigObject>();

    public static Map<Integer, PetFragmentConfigObject> _ix_petid = new HashMap<Integer, PetFragmentConfigObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetFragmentConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetFragmentConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetFragmentConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public static PetFragmentConfigObject getByPetid(int petid) {

        return _ix_petid.get(petid);

    }


    public void putToMem(Map e, PetFragmentConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setDebrisrarity(MapHelper.getInt(e, "debrisRarity"));

        config.setAmount(MapHelper.getInt(e, "amount"));

        config.setDebristype(MapHelper.getInt(e, "debrisType"));

        config.setPetcore(MapHelper.getBoolean(e, "petCore"));

        config.setPetid(MapHelper.getInt(e, "petId"));

        config.setProbability(MapHelper.getInt(e, "probability"));

        config.setProbabilitybyclass(MapHelper.getInt(e, "probabilityByClass"));

        config.setName(MapHelper.getStr(e, "name"));

        config.setNamelanguage(MapHelper.getInt(e, "nameLanguage"));


        _ix_id.put(config.getId(), config);

        _ix_petid.put(config.getPetid(), config);


        if (config.getPetid() == 0 && config.getProbability() == 0) {
            return;
        }
        List<PetFragmentConfigObject> list = entityMap.computeIfAbsent(config.getDebrisrarity(), (key) -> new ArrayList());
        list.add(config);


        safeAddToMap(randomRarityClassWeight, getRarityClassMapKey(config.getDebrisrarity(), config.getDebristype(), config.getPetcore()), config.getProbabilitybyclass());
        //种族0代表所有
        safeAddToMap(randomRarityClassWeight, getRarityClassMapKey(config.getDebrisrarity(), 0, config.getPetcore()), config.getProbabilitybyclass());
    }

    /**
     * key-value:rarity-entityList, 用于宠物碎片随机合成，过滤掉几率为0
     */
    public static Map<Integer, List<PetFragmentConfigObject>> entityMap = new HashMap<>();

    private static final String keySeparator = "-";

    private static String getRarityClassMapKey(int rarity, int classType, boolean corePet) {
        return rarity + keySeparator + classType + keySeparator + corePet;
    }

    private static void safeAddToMap(Map<Object, Integer> map, Object key, int value) {
        Integer beforeValue = map.get(key);
        map.put(key, beforeValue == null ? value : beforeValue + value);
    }


    /**
     * <品质-种族,总权重></>
     * ps:0代表所有种族
     */
    private static Map<Object, Integer> randomRarityClassWeight = new HashMap<>();

    /**
     *
     * @param rarity
     * @param type 0代表所有种族
     * @param corePet
     * @return
     */
    public static int getTotalWeightByRarityAndType(int rarity, int type, boolean corePet) {
        return randomRarityClassWeight.get(getRarityClassMapKey(rarity, type, corePet));
    }


    public static List<PetFragmentConfigObject> getProbByRarity(int rarity) {
        return entityMap.get(rarity);
    }

    public static int getQualityByCfgId(int cfgId) {
        PetFragmentConfigObject byId = getById(cfgId);
        if (byId == null) {
            return 0;
        }
        return byId.getDebrisrarity();
    }

    public static String getNameById(int cfgId) {
        PetFragmentConfigObject byId = getById(cfgId);
        if (byId == null) {
            return String.valueOf(cfgId);
        }
        return byId.getName();
    }

    public static int getLinkPetId(int fragmentId) {
        PetFragmentConfigObject fragmentCfg = getById(fragmentId);
        if (fragmentCfg == null) {
            return 0;
        }
        return fragmentCfg.getPetid();
    }

    //<petBookId,petFragmentId> 碎片品质==宠物初始瓶子
    private static final Map<Integer,PetFragmentConfigObject> petBornRarityFragment = new HashMap<>();

    public  PetFragmentConfigObject getPetDebrisRarityFragment(int petBookId){
        return petBornRarityFragment.get(petBookId);
    }

    public boolean afterAllCfgInit(){
        for (PetBasePropertiesObject config : PetBaseProperties._ix_petdebrisid.values()) {
            if (config.getPetid()<=0||config.getPetfinished()==0){
                continue;
            }
            int petBookId;
            int petRarity;
            petBookId=config.getPetid();
            petRarity=config.getStartrarity();
            Optional<PetFragmentConfigObject> fragment = _ix_id.values().stream()
                        .filter(e -> e.getPetid() == petBookId && e.getDebrisrarity() == petRarity).findFirst();
            if (!fragment.isPresent()) {
                LogUtil.warn("can`t find fragment match petBookId:{},pet born rarity:{}", petBookId, petRarity);
                continue;
            }
            petBornRarityFragment.put(petBookId,fragment.get());
        }
        return true;
    }
}
