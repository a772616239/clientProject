/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.Reward;
import util.LogUtil;
import util.MapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@annationInit(value = "PetGemConfig", methodname = "initConfig")
public class PetGemConfig extends baseConfig<PetGemConfigObject> {


    private static PetGemConfig instance = null;

    public static PetGemConfig getInstance() {

        if (instance == null)
            instance = new PetGemConfig();
        return instance;

    }


    private static final Map<Integer, Map<Integer, Integer>> gemIdProAdditionMap = new HashMap<>();

    public static Map<Integer, PetGemConfigObject> _ix_id = new HashMap<>();

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetGemConfig) o;
        initConfig();
    }

    public boolean initGemData() {
        Map<Integer, Integer> additionMap;
        for (PetGemConfigObject config : _ix_id.values()) {
            PetGemConfigAdvanceObject advanceObject = PetGemConfigAdvance.getByGemConfigId(config.getId());
            additionMap = new HashMap<>(getSize(config, advanceObject));
            if (advanceObject != null) {
                for (int[] value : advanceObject.getAdvanceproperties()) {
                    if (value.length > 1) {
                        MapUtil.add2IntMapValue(additionMap, value[0], value[1]);
                    }
                }
            }
            for (int[] value : config.getBaseproperties()) {
                if (value.length > 1) {
                    MapUtil.add2IntMapValue(additionMap, value[0], value[1]);
                }
            }
            gemIdProAdditionMap.put(config.getId(), additionMap);
        }
        return true;
    }

    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetGemConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetGemConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetGemConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setRarity(MapHelper.getInt(e, "rarity"));

        config.setGemtype(MapHelper.getInt(e, "gemtype"));

        config.setLv(MapHelper.getInt(e, "lv"));

        config.setStar(MapHelper.getInt(e, "star"));

        config.setMaxlv(MapHelper.getInt(e, "maxlv"));

        config.setAdvanceneedplayerlv(MapHelper.getInt(e, "advanceNeedPlayerlv"));

        config.setUplvneedrarity(MapHelper.getInt(e, "upLvNeedRarity"));

        config.setUplvneedstar(MapHelper.getInt(e, "upLvNeedStar"));

        config.setBaseproperties(MapHelper.getIntArray(e, "baseProperties"));


        _ix_id.put(config.getId(), config);


    }

    public static List<Reward> getSaleResource(int configId) {
        PetGemConfigObject gemConfig = getById(configId);
        if (gemConfig == null) {
            return Collections.emptyList();
        }
        PetGemConfigLeveObject lvConfig = PetGemConfigLeve.getByLv(gemConfig.getLv());
        if (lvConfig == null) {
            LogUtil.warn("getSaleResource  lvConfig is null by gemCfgId:{}", configId);
            return Collections.emptyList();
        }
        List<Reward> rewards = new ArrayList<>();
        List<Reward> lvReturn = RewardUtil.parseRewardIntArrayToRewardList(lvConfig.getGemsale());
        if (CollectionUtils.isNotEmpty(lvReturn)) {
            rewards.addAll(lvReturn);
        }
        PetGemConfigAdvanceObject advanceConfig = PetGemConfigAdvance.getByGemConfigId(configId);
        if (advanceConfig == null) {
            LogUtil.warn("getSaleResource  advanceConfig is null by gemCfgId:{}", configId);
            return rewards;
        }
        List<Reward> advanceReturn = RewardUtil.parseRewardIntArrayToRewardList(advanceConfig.getGemsale());
        if (CollectionUtils.isNotEmpty(advanceReturn)) {
            rewards.addAll(advanceReturn);
        }

        return rewards;
    }


    public static PetGemConfigObject getNextUpLvConfig(int curId) {
        PetGemConfigObject config = getById(curId);
        if (config == null) {
            return null;
        }
        return _ix_id.values().stream().filter(e -> e.getGemtype() == config.getGemtype() && e.getStar() == config.getStar()
                && e.getRarity() == config.getRarity() && e.getLv() == config.getLv() + 1).findFirst().orElse(null);
    }

    public static PetGemConfigObject getNextUpStarId(int curId) {
        PetGemConfigObject config = getById(curId);
        if (config == null) {
            return null;
        }
        Optional<PetGemConfigObject> UpStar = _ix_id.values().stream().filter(e -> matchNextUpStar(config, e)).findFirst();

        return UpStar.orElseGet(() -> _ix_id.values().stream().filter(e -> matchNextRarityInitLevel(config, e)).findFirst().orElse(null));

    }

    private static boolean matchNextUpStar(PetGemConfigObject config, PetGemConfigObject e) {
        return e.getGemtype() == config.getGemtype() && e.getLv() == config.getLv()
                && e.getRarity() == config.getRarity() && e.getStar() == config.getStar() + 1;
    }

    private static boolean matchNextRarityInitLevel(PetGemConfigObject config, PetGemConfigObject e) {
        return e.getGemtype() == config.getGemtype() && e.getStar() == 1 && e.getRarity() == config.getRarity() + 1 && e.getLv() == config.getLv();
    }


    private static int getSize(PetGemConfigObject config, PetGemConfigAdvanceObject advanceObject) {
        int size = 0;
        if (config != null) {
            size += config.getBaseproperties().length;
        }
        if (advanceObject != null) {
            size += advanceObject.getAdvanceproperties().length;
        }
        return size;
    }


    public static Map<Integer, Integer> getGemAdditionByGemCfgId(int gemConfigId) {
        Map<Integer, Integer> addition = gemIdProAdditionMap.get(gemConfigId);
        return addition == null ? Collections.emptyMap() : addition;
    }

    public static int queryEnhanceLv(int gemConfigId) {
        PetGemConfigObject config = getById(gemConfigId);
        if (config == null) {
            return 0;
        }
        return config.getLv();
    }


    public static int queryRarity(int cfgId) {
        PetGemConfigObject config = getById(cfgId);
        if (config == null) {
            return 0;
        }
        return config.getRarity();
    }

    public static int queryStar(int cfgId) {
        PetGemConfigObject config = getById(cfgId);
        if (config == null) {
            return 0;
        }
        return config.getStar();
    }

}
