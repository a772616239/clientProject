package model.petrune;

import cfg.PetRuneExp;
import cfg.PetRuneExpObject;
import lombok.Getter;
import platform.logs.LogClass;
import protocol.PetMessage;
import util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetRuneManager {

    private static final String SEPARATOR = "-";

    @Getter
    private static final PetRuneManager instance = new PetRuneManager();

    //<符文品质-等级-类型,基础属性对象>
    private static final Map<String, PetMessage.RuneProperties> runeBasePropertiesMap = new HashMap<>();

    //<符文品质-等级-类型,基础属性日志>
    private static final Map<String, List<LogClass.PetPropertyLog>> basePropertiesLogMap = new HashMap<>();

    public boolean init() {
        String runeBaseProMapKey;
        PetMessage.RuneProperties baseProperties;

        PetMessage.RuneProperties.Builder propertyBuilder = PetMessage.RuneProperties.newBuilder();
        for (PetRuneExpObject runExpCfg : PetRuneExp._ix_key.values()) {
            if (runExpCfg.getRunelvl()<=0){
                continue;
            }

            baseProperties = buildPetRuneBaseProperties(runExpCfg);
            runeBaseProMapKey = getRuneBaseProMapKey(runExpCfg.getRarity(), runExpCfg.getRunelvl(), runExpCfg.getRunetype());

            runeBasePropertiesMap.put(runeBaseProMapKey, baseProperties);

            basePropertiesLogMap.put(runeBaseProMapKey, buildBasePropertiesLog(baseProperties));
            propertyBuilder.clear();
        }
        return true;
    }

    private PetMessage.RuneProperties buildPetRuneBaseProperties(PetRuneExpObject runExpCfg) {
        PetMessage.RuneProperties.Builder result = PetMessage.RuneProperties.newBuilder();
        int[][] basePropertiesCfg = runExpCfg.getBaseproperties();
        for (int[] property : basePropertiesCfg) {
            if (property.length < 2) {
                LogUtil.error("PetRuneExpObject config error by data in baseProperties length less than 2,cfgKey:{}", runExpCfg.getKey());
                continue;
            }
            result.addProperty(PetMessage.RunePropertieyEntity.newBuilder().setPropertyType(property[0]).setPropertyValue(property[1]));
        }
        return result.build();
    }

    private List<LogClass.PetPropertyLog> buildBasePropertiesLog(PetMessage.RuneProperties baseProperties) {
        List<LogClass.PetPropertyLog> basePropertiesLog;
        basePropertiesLog = new ArrayList<>();

        for (PetMessage.RunePropertieyEntity entity : baseProperties.getPropertyList()) {
            basePropertiesLog.add(new LogClass.PetPropertyLog(entity));
        }
        return basePropertiesLog;
    }

    public PetMessage.RuneProperties queryRuneBaseProperties(int rarity, int runeLv, int runeType) {
        return runeBasePropertiesMap.get(getRuneBaseProMapKey(rarity, runeLv, runeType));

    }

    private String getRuneBaseProMapKey(int rarity, int runeLvl, int runeType) {
        return rarity + SEPARATOR + runeLvl + SEPARATOR + runeType;
    }

    public List<LogClass.PetPropertyLog> queryBasePropertiesLog(int rarity, int runeLv, int runeType) {
        return basePropertiesLogMap.get(getRuneBaseProMapKey(rarity, runeLv, runeType));
    }

}
