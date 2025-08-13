/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.Random;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import util.LogUtil;

@annationInit(value = "PetRuneProperties", methodname = "initConfig")
public class PetRuneProperties extends baseConfig<PetRunePropertiesObject> {


    private static PetRuneProperties instance = null;

    public static PetRuneProperties getInstance() {

        if (instance == null)
            instance = new PetRuneProperties();
        return instance;

    }


    public static Map<Integer, PetRunePropertiesObject> _ix_runeid = new HashMap<Integer, PetRunePropertiesObject>();

    public static int getRuneRarity(int runeBookId) {
        PetRunePropertiesObject cfg = getByRuneid(runeBookId);
        if (cfg==null){
            return 0;
        }
        return cfg.getRunerarity();

    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneProperties) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneProperties");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetRunePropertiesObject getByRuneid(int runeid) {

        PetRunePropertiesObject cfg = _ix_runeid.get(runeid);
        if (cfg == null) {
            LogUtil.error("PetRuneProperties get rune is null by runeId:{}", runeid);
            return null;
        }
        return cfg;

    }


    public void putToMem(Map e, PetRunePropertiesObject config) {

        config.setRuneid(MapHelper.getInt(e, "runeId"));

        config.setRunename(MapHelper.getInt(e, "runeName"));

        config.setSevername(MapHelper.getStr(e, "severName"));

        config.setRunerarity(MapHelper.getInt(e, "runeRarity"));

        config.setRunetype(MapHelper.getInt(e, "runeType"));

        config.setExpropertiesrange(MapHelper.getInts(e, "exPropertiesRange"));

        config.setExproperties(MapHelper.getIntArray(e, "exProperties"));

        config.setRunesuit(MapHelper.getInt(e, "runeSuit"));


        _ix_runeid.put(config.getRuneid(), config);

        RARITY_MAP.computeIfAbsent(config.getRunerarity(), key -> new ArrayList<>()).add(config);

        PetRunePropertiesObject pre = RARITY_SUIT_TYPE_MAP.put(buildRaritySuitTypeKey(config.getRunerarity(), config.getRunesuit(), config.getRunetype()), config);
        if (pre != null) {
            LogUtil.warn("PetRuneProperties init err, the PetRunePropertiesObject is duplicate id={}", config.getRuneid());
        }
    }

    /**
     * 符文稀有度分类
     */
    private static final Map<Integer, List<PetRunePropertiesObject>> RARITY_MAP = new HashMap<>();

    /**
     * 根据稀有度随机获得符文
     * @param rarity
     * @return
     */
    public static PetRunePropertiesObject randomGetRuneByRarity(int rarity) {
        List<PetRunePropertiesObject> rarityList = RARITY_MAP.get(rarity);
        if(CollectionUtils.isEmpty(rarityList)) {
            return null;
        }
        return rarityList.get(new Random().nextInt(rarityList.size()));
    }

    public static int getQualityByCfgId(int cfgId) {
        PetRunePropertiesObject byRuneid = getByRuneid(cfgId);
        if (byRuneid == null) {
            return 0;
        }
        return byRuneid.getRunerarity();
    }

    public static String getNameById(int cfgId) {
        PetRunePropertiesObject byRuneid = getByRuneid(cfgId);
        if (byRuneid == null) {
            return String.valueOf(cfgId);
        }
        return byRuneid.getSevername();
    }

    public static int getRuneType(int runeCfgId) {
        PetRunePropertiesObject byRuneid = getByRuneid(runeCfgId);
        return byRuneid == null ? 0 : byRuneid.getRunetype();
    }

    //<符文品质-套装id-类型,基础属性对象>
    private static final Map<String, PetRunePropertiesObject> RARITY_SUIT_TYPE_MAP = new HashMap<>();
    public static PetRunePropertiesObject getRarityRune(int rarity, int suit, int type) {
        String key = buildRaritySuitTypeKey(rarity, suit, type);
        return RARITY_SUIT_TYPE_MAP.get(key);
    }

    private static String buildRaritySuitTypeKey(int rarity, int suit, int type) {
        return rarity + "_" + suit + "_" + type;
    }
}
