/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.GameConst;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.base.baseConfig;
import org.springframework.util.CollectionUtils;
import protocol.PetMessage;
import util.LogUtil;

@annationInit(value = "PetRuneBlessRatingCfg", methodname = "initConfig")
public class PetRuneBlessRatingCfg extends baseConfig<PetRuneBlessRatingCfgObject> {


    private static PetRuneBlessRatingCfg instance = null;

    public static PetRuneBlessRatingCfg getInstance() {

        if (instance == null)
            instance = new PetRuneBlessRatingCfg();
        return instance;

    }


    public static Map<Integer, PetRuneBlessRatingCfgObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetRuneBlessRatingCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetRuneBlessRatingCfg");

        for (Map e : ret) {
            put(e);
        }
        initRatingCfgMap();
    }

    public static PetRuneBlessRatingCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetRuneBlessRatingCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setRunerarity(MapHelper.getInt(e, "runeRarity"));

        config.setBlesslevel(MapHelper.getInt(e, "blessLevel"));

        config.setRatingthreshold(MapHelper.getIntArray(e, "ratingThreshold"));

        config.setOfferproperty(MapHelper.getInt(e, "offerProperty"));


        _ix_id.put(config.getId(), config);


    }

    public PetRuneBlessRatingCfgObject findByRarityAndLv(int runRarity, int ratingLv) {
        return _ix_id.values().stream().filter(e -> e.getRunerarity() == runRarity && e.getBlesslevel() == ratingLv).findAny().orElse(null);

    }

    //<符文品质-符文评级-符文部位,属性加成>>
    private static final Map<String, Map<Integer, Integer>> totalRatingAdditionMap = new HashMap<>();

    public boolean initTotalRatingAdditionMap() {
        for (Integer rarity : _ix_id.values().stream().map(PetRuneBlessRatingCfgObject::getRunerarity).collect(Collectors.toSet())) {
            //对应1-4 4个部位符文
            for (int runeType = 1; runeType <= 4; runeType++) {
                PetRuneExpObject runeExpCfg = PetRuneExp.getByRarityAndLvlAndType(rarity, PetRuneExp.queryRuneMaxLv(rarity), runeType);
                if (runeExpCfg == null) {
                    LogUtil.error("initTotalRatingAdditionMap error case by PetRuneExp.getByRarityAndLvlAndType " +
                            "is empty,rarity:{},lv:{},type:{}", rarity, PetRuneExp.queryRuneMaxLv(rarity), runeType);
                    return false;
                }
                int finalRuneType = runeType;
                _ix_id.values().stream().filter(e -> e.getRunerarity() == rarity).forEach(cfg -> {
                    String key = getTotalRatingAdditionMapKey(rarity, cfg.getBlesslevel(), finalRuneType);
                    totalRatingAdditionMap.put(key, muiltProperty(cfg.getOfferproperty(), runeExpCfg.getBaseproperties()));
                });

            }


        }
        return true;

    }

    private Map<Integer, Integer> muiltProperty(int offerProperty, int[][] baseProperties) {
        if (offerProperty == 0 || baseProperties.length == 0) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (int[] baseProperty : baseProperties) {
            if (baseProperties.length >= 2) {
                map.put(baseProperty[0], (int) (baseProperty[1] * (offerProperty * 1.0 / GameConst.commonMagnification - 1)));
            }
        }
        return map;
    }

    private String getTotalRatingAdditionMapKey(Integer rarity, int blesslevel, int finalRuneType) {
        return rarity + "-" + blesslevel + "-" + finalRuneType;
    }

    public Map<Integer, Integer> queryRuneAddition(PetMessage.Rune rune) {

        if (rune == null) {
            return Collections.emptyMap();
        }
        return getTotalRatingAddition(PetRuneProperties.getRuneRarity(rune.getRuneBookId()),
                rune.getBlessRating().getTotalLv(), PetRuneProperties.getRuneType(rune.getRuneBookId()));
    }

    private Map<Integer, Integer> getTotalRatingAddition(int runeRarity, int totalLv, int runeType) {
        Map<Integer, Integer> result = totalRatingAdditionMap.get(getTotalRatingAdditionMapKey(runeRarity, totalLv, runeType));
        return result == null ? Collections.emptyMap() : result;
    }

    //<符文品质,<符文属性类型,<属性值,评级>>>
    private static final Map<Integer, Map<Integer, Map<Integer, Integer>>> ratingCfg = new HashMap<>();

    public void initRatingCfgMap() {
        int runeRarity;
        for (PetRuneBlessRatingCfgObject cfg : _ix_id.values()) {
            runeRarity = cfg.getRunerarity();
            Map<Integer, Map<Integer, Integer>> propertyMap = ratingCfg.computeIfAbsent(runeRarity, a -> new HashMap<>());
            for (int[] ints : cfg.getRatingthreshold()) {
                if (ints.length >= 2) {
                    Map<Integer, Integer> ratingMap = propertyMap.computeIfAbsent(ints[0], a -> new HashMap<>());
                    ratingMap.put(ints[1], cfg.getBlesslevel());
                }
            }

        }
    }


    public int ratingProperty(int runeRarity, int propertyType, int propertyValue) {
        Map<Integer, Map<Integer, Integer>> rarityMap = ratingCfg.get(runeRarity);
        if (CollectionUtils.isEmpty(rarityMap)) {
            return 0;
        }
        Map<Integer, Integer> propertyTypeValueRatingMap = rarityMap.get(propertyType);
        if (CollectionUtils.isEmpty(propertyTypeValueRatingMap)) {
            return 0;
        }
        Integer valueThreshold = propertyTypeValueRatingMap.keySet().stream()
                .filter(value -> value < propertyValue).max(Comparator.comparingInt(o -> o)).orElse(0);

        Integer result = propertyTypeValueRatingMap.get(valueThreshold);
        return result == null ? 0 : result;
    }
}
