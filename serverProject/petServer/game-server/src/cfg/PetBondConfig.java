/*CREATED BY TOOL*/

package cfg;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import common.GameConst;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import util.LogUtil;

@annationInit(value = "PetBondConfig", methodname = "initConfig")
public class PetBondConfig extends baseConfig<PetBondConfigObject> {


    private static PetBondConfig instance = null;

    public static PetBondConfig getInstance() {

        if (instance == null)
            instance = new PetBondConfig();
        return instance;

    }


    public static Map<String, PetBondConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetBondConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetBondConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetBondConfigObject getById(String id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetBondConfigObject config) {

        config.setId(MapHelper.getStr(e, "id"));

        config.setBuffid(MapHelper.getInt(e, "buffId"));

        config.setMonsterbuffid(MapHelper.getInt(e, "monsterBuffId"));


        _ix_id.put(config.getId(), config);


    }
    //map<petType,<petNum,buffId>>
    private  Map<Integer,Map<Integer,Integer>> playerBonusBuff = new HashMap();

    //map<petType,<petNum,buffId>>
    private  Map<Integer,Map<Integer,Integer>> monsterBonusBuff = new HashMap();

    private volatile AtomicBoolean cfgInit = new AtomicBoolean(false);


    public List<Integer> queryBonusBuffs(Map<Integer, Long> bonusNumMap,boolean player) {
        if (MapUtils.isEmpty(bonusNumMap)) {
            return Collections.emptyList();
        }
        if (!cfgInit.get()) {
            initMap();
        }
        List<Integer> result = new ArrayList<>();
        int petType;
        long petNum;
        Integer buffId;
        for (Map.Entry<Integer, Long> entry : bonusNumMap.entrySet()) {
            petType = entry.getKey();
            petNum = entry.getValue();
            buffId = queryBonusBuff(petType, petNum,player);
            if (buffId != null) {
                result.add(buffId);
            }
        }
        return result;
    }

    private Integer queryBonusBuff(int petType, long petNum, boolean player) {
        Map<Integer, Map<Integer, Integer>> sourceMap = player ? playerBonusBuff : monsterBonusBuff;

        Map<Integer, Integer> cfgMap = sourceMap.get(petType);
        if (MapUtils.isEmpty(cfgMap)) {
            return null;
        }
        for (Map.Entry<Integer, Integer> cfg : cfgMap.entrySet()) {
            if (cfg.getKey() <= petNum) {
                return cfg.getValue();
            }
        }
        return null;
    }

    private void initMap() {
        cfgInit.set(true);

        for (PetBondConfigObject cfg : PetBondConfig._ix_id.values()) {
            List<Integer> config = getBondTypesFromConfig(cfg);
            if (CollectionUtils.isNotEmpty(config)) {
                Map<Integer, Integer> playerMap = playerBonusBuff.computeIfAbsent(config.get(0), a -> new TreeMap<>((o1, o2) -> o2-o1));
                playerMap.put(petBondNeedPetNum(config.get(1)),cfg.getBuffid());


                Map<Integer, Integer> monsterMap = monsterBonusBuff.computeIfAbsent(config.get(0), a -> new TreeMap<>((o1, o2) -> o2-o1));
                monsterMap.put(petBondNeedPetNum(config.get(1)),cfg.getMonsterbuffid());
            }

        }
    }

    private List<Integer> getBondTypesFromConfig(PetBondConfigObject petBondConfig) {
        String[] idArray = petBondConfig.getId().split(",");
        if (idArray.length < 2) {
            LogUtil.error("petBondConfig error with configId+[" + petBondConfig.getId() + "]");
            return Collections.emptyList();
        }
        return Arrays.stream(idArray).map(Integer::parseInt).collect(Collectors.toList());
    }

    private int petBondNeedPetNum(int level) {
        int[] bondLevel = GameConfig.getById(GameConst.CONFIG_ID).getBondlevel();
        if (level > bondLevel.length || bondLevel.length == 0) {
            LogUtil.error("level in gameConfig`s bondLevel error,level+[" + level + "],bondLevel"
                    + Arrays.toString(bondLevel));
        }
        return bondLevel[level - 1];
    }
}
