/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.GameConst;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@annationInit(value = "PetBaseProperties", methodname = "initConfig")
public class PetBaseProperties extends baseConfig<PetBasePropertiesObject> {


    private static PetBaseProperties instance = null;

    public static PetBaseProperties getInstance() {

        if (instance == null)
            instance = new PetBaseProperties();
        return instance;

    }


    public static Map<Integer, PetBasePropertiesObject> _ix_petid = new HashMap<>();

    public static Map<Integer, PetBasePropertiesObject> _ix_petdebrisid = new HashMap<>();

    public static boolean isCorePet(int petBookId) {
        return queryPetCore(petBookId) == GameConst.CORE_PET;
    }

    public static int queryPetCore(int petBookId) {
        PetBasePropertiesObject cfg = getByPetid(petBookId);
        if (cfg == null) {
            return -1;
        }
        return cfg.getPetcore();
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetBaseProperties) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetBaseProperties");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetBasePropertiesObject getByPetid(int petid) {

        return _ix_petid.get(petid);

    }


    public static PetBasePropertiesObject getByPetdebrisid(int petdebrisid) {

        return _ix_petdebrisid.get(petdebrisid);

    }


    public void putToMem(Map e, PetBasePropertiesObject config) {

        config.setPetid(MapHelper.getInt(e, "petId"));

        config.setMingzi(MapHelper.getStr(e, "mingzi"));

        config.setPetcore(MapHelper.getInt(e, "petCore"));

        config.setPetname(MapHelper.getInt(e, "petName"));

        config.setShowmap(MapHelper.getInt(e, "showMap"));

        config.setStartrarity(MapHelper.getInt(e, "startRarity"));

        config.setMaxrarity(MapHelper.getInt(e, "maxRarity"));

        config.setPetclass(MapHelper.getInt(e, "petClass"));

        config.setPettype(MapHelper.getInt(e, "petType"));

        config.setPettag(MapHelper.getInts(e, "petTag"));

        config.setMaxuplvl(MapHelper.getInt(e, "maxUpLvl"));

        config.setPetproperties(MapHelper.getIntArray(e, "petProperties"));

        config.setPetextraproperties(MapHelper.getInt(e, "petExtraProperties"));

        config.setPetdebrisid(MapHelper.getInt(e, "petDebrisId"));

        config.setUnlockhead(MapHelper.getInt(e, "UnlockHead"));

        config.setPetfinished(MapHelper.getInt(e, "petfinished"));

        config.setBraverandom(MapHelper.getBoolean(e, "braveRandom"));

        config.setIsoptional(MapHelper.getBoolean(e, "IsOptional"));

        config.setPropertymodel(MapHelper.getInt(e, "PropertyModel"));

        config.setDrawgk(MapHelper.getInt(e, "drawgk"));


        _ix_petid.put(config.getPetid(), config);

        _ix_petdebrisid.put(config.getPetdebrisid(), config);


        basePropertiesList.add(config);
        PET_START_RARITY_LIST.computeIfAbsent(config.getStartrarity(), Key -> new ArrayList<>()).add(config);
        if (config.getBraverandom()) {
            BRAVE_PET_START_RARITY_MAP.computeIfAbsent(config.getStartrarity(), key -> new ArrayList<>()).add(config);
        }
    }

    /**
     * ====================================================
     */
    public static List<PetBasePropertiesObject> basePropertiesList = new ArrayList<>();

    public static final Map<Integer, List<PetBasePropertiesObject>> PET_START_RARITY_LIST = new HashMap<>();

    private static final Map<Integer, List<PetBasePropertiesObject>> BRAVE_PET_START_RARITY_MAP = new HashMap<>();

    private static final Map<Integer, List<PetBasePropertiesObject>> HelpPetMap = new HashMap<>();


    public static List<PetBasePropertiesObject> getHelpPetByRarity(int rarity) {
        if (MapUtils.isEmpty(HelpPetMap)) {
            int minRarity = basePropertiesList.stream().mapToInt(PetBasePropertiesObject::getStartrarity).min().orElse(1);
            int maxRarity = basePropertiesList.stream().mapToInt(PetBasePropertiesObject::getMaxrarity).max().orElse(10);
            for (int tempRarity = minRarity; tempRarity <= maxRarity; tempRarity++) {
                int finalTempRarity = tempRarity;
                List<PetBasePropertiesObject> collect = basePropertiesList.stream().filter(item -> matchHelpPet(finalTempRarity, item)).collect(Collectors.toList());
                HelpPetMap.put(finalTempRarity, collect);
            }
        }
        return HelpPetMap.get(rarity);
    }

    private static boolean matchHelpPet(int finalTempRarity, PetBasePropertiesObject item) {
        return item.getPetfinished() == 1 && item.getBraverandom() && item.getStartrarity() <= finalTempRarity && item.getMaxrarity() >= finalTempRarity;
    }

    public static PetBasePropertiesObject randomBravePetByStartRarity(int rarity) {
        List<PetBasePropertiesObject> rarityList = BRAVE_PET_START_RARITY_MAP.get(rarity);
        if (CollectionUtils.isEmpty(rarityList)) {
            LogUtil.error("cfg.PetBaseProperties.randomPetByStartRarity, pet start rarity is not exist, rarity:" + rarity);
            return null;
        }
        return rarityList.get(new Random().nextInt(rarityList.size()));
    }


    public static int getQualityByPetId(int petBookId) {
        PetBasePropertiesObject petBasePropertiesObject = _ix_petid.get(petBookId);
        if (petBasePropertiesObject == null) {
            return 0;
        }
        return petBasePropertiesObject.getStartrarity();
    }

    public static int getRaceByPetId(int petBookId) {
        PetBasePropertiesObject petBasePropertiesObject = _ix_petid.get(petBookId);
        if (petBasePropertiesObject == null) {
            return 0;
        }
        return petBasePropertiesObject.getPettype();
    }


    public static String getNameById(int cfgId) {
        PetBasePropertiesObject petBase = getByPetid(cfgId);
        if (petBase == null) {
            return String.valueOf(cfgId);
        }
        return petBase.getMingzi();
    }

    public static int getStartRarityById(int cfgId) {
        PetBasePropertiesObject petBase = getByPetid(cfgId);
        if (petBase == null) {
            return 0;
        }
        return petBase.getStartrarity();
    }

    public static int getTypeById(int cfgId) {
        PetBasePropertiesObject petBase = getByPetid(cfgId);
        if (petBase == null) {
            return 0;
        }
        return petBase.getPettype();
    }

    public static String getTypeNameById(int cfgId) {
        int typeById = getTypeById(cfgId);
        if (typeById == 1) {
            return "自然";
        } else if (typeById == 2) {
            return "蛮荒";
        } else if (typeById == 3) {
            return "深渊";
        } else if (typeById == 4) {
            return "地狱";
        }
        return "";
    }

    public static int getAvatarIdByPetId(int petBookId) {
        PetBasePropertiesObject byPetId = getByPetid(petBookId);
        if (null == byPetId) {
            return 0;
        }
        return byPetId.getUnlockhead();
    }

    public static int getPetIdByUnlockHeadId(int headId) {
        for (PetBasePropertiesObject value : _ix_petid.values()) {
            if (value.getUnlockhead() == headId) {
                return value.getUnlockhead();
            }
        }
        return -1;
    }

    public static List<Integer> getRarityTotalPetId(int rarity) {
        List<PetBasePropertiesObject> totalPet = getRarityTotalPet(rarity);
        if (CollectionUtils.isEmpty(totalPet)) {
            return null;
        }
        return totalPet.stream().map(PetBasePropertiesObject::getPetid).collect(Collectors.toList());
    }

    public static List<PetBasePropertiesObject> getRarityTotalPet(int rarity) {
        return PET_START_RARITY_LIST.get(rarity);
    }

    public static int getClass(int petBookId) {
        PetBasePropertiesObject config = getByPetid(petBookId);
        if (config == null) {
            LogUtil.error("can`t find orientation by petBookId:{}", petBookId);
            return 0;
        }
        return config.getPropertymodel();
    }

}
