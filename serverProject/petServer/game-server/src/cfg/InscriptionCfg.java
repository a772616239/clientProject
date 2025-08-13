/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.GameConst;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Getter;
import model.base.baseConfig;
import org.springframework.util.CollectionUtils;
import util.MapUtil;

@annationInit(value = "InscriptionCfg", methodname = "initConfig")
public class InscriptionCfg extends baseConfig<InscriptionCfgObject> {


    private static InscriptionCfg instance = null;

    public static InscriptionCfg getInstance() {

        if (instance == null)
            instance = new InscriptionCfg();
        return instance;

    }


    public static Map<Integer, InscriptionCfgObject> _ix_id = new HashMap<>();

    private static List<Integer> inscriptionRarity ;

    /**
     * 不包括通用铭文
     */
    private static Map<Integer, List<InscriptionCfgObject>> rarityMap;
    @Getter
    private int inscriptionMaxRarity ;

    //<cfgId,<属性类型,属性值>> 配置id -对应当前铭文属性加成
    private Map<Integer, Map<Integer, Integer>> additionMap = new HashMap<>();

    public static List<Integer> getAdditionBuff(List<Integer> inscriptionCfgIds) {
        List<Integer> buffs = new ArrayList<>();
        for (Integer cfgId : inscriptionCfgIds) {
            InscriptionCfgObject cfg = getById(cfgId);
            if (cfg != null) {
                buffs.add(cfgId);
            }
        }
        return buffs;
    }

    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (InscriptionCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "InscriptionCfg");

        for (Map e : ret) {
            put(e);
        }
        rarityMap = _ix_id.values().stream().filter(e -> e.getType() != GameConst.generalInscriptionType).collect(Collectors.groupingBy(InscriptionCfgObject::getRarity));
        for (InscriptionCfgObject cfg : _ix_id.values()) {
            Map<Integer, Integer> addition = additionMap.computeIfAbsent(cfg.getId(), a -> new HashMap<>());
            addition.clear();
            MapUtil.add2IntMapValue(addition, cfg.getProperty());
        }
        inscriptionRarity =_ix_id.values().stream().map(InscriptionCfgObject::getRarity).sorted().collect(Collectors.toList());
        inscriptionMaxRarity = inscriptionRarity.get(inscriptionRarity.size()-1);
    }

    public int randomByRarity(int rarity) {
        List<InscriptionCfgObject> cfgList = rarityMap.get(rarity);
        if (CollectionUtils.isEmpty(cfgList)) {
            return -1;
        }
        return cfgList.get(ThreadLocalRandom.current().nextInt(cfgList.size())).getId();
    }


    public static InscriptionCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, InscriptionCfgObject config) {

        config.setId(MapHelper.getInt(e, "Id"));

        if (config.getId()<=0){
            return;
        }

        config.setRarity(MapHelper.getInt(e, "rarity"));

        config.setType(MapHelper.getInt(e, "type"));

        config.setProperty(MapHelper.getIntArray(e, "property"));

        config.setBuffdata(MapHelper.getInts(e, "buffData"));

        config.setEquipconsume(MapHelper.getIntArray(e, "equipConsume"));

        config.setProbability(MapHelper.getInt(e, "probability"));


        _ix_id.put(config.getId(), config);


    }

    public Map<Integer, Integer> calculteAddition(List<Integer> inscriptionCfgIds) {
        if (CollectionUtils.isEmpty(inscriptionCfgIds)) {
            return Collections.emptyMap();
        }
        if (inscriptionCfgIds.size() == 1) {
            return additionMap.get(inscriptionCfgIds.get(0));
        }
        Map<Integer, Integer> result = new HashMap<>();
        for (Integer cfgId : inscriptionCfgIds) {
            MapUtil.mergeIntMaps(result, additionMap.get(cfgId));
        }
        return result;
    }

    public static int getNextRarity(int curRarity) {
        return inscriptionRarity.stream().filter(e -> e > curRarity)
                .findFirst().orElse(getInstance().inscriptionMaxRarity);
    }
}
