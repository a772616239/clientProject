/*CREATED BY TOOL*/

package cfg;

import com.google.protobuf.ProtocolStringList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import protocol.Collection;
import protocol.PetMessage;

@annationInit(value = "PetCollectExpCfg", methodname = "initConfig")
public class PetCollectExpCfg extends baseConfig<PetCollectExpCfgObject> {


    private static PetCollectExpCfg instance = null;

    private static final String SEPARATOR = "-";

    @Getter
    private int startRarity;

    @Getter
    private List<Integer> canClaimRarity = new ArrayList<>();

    public static PetCollectExpCfg getInstance() {

        if (instance == null)
            instance = new PetCollectExpCfg();
        return instance;
    }

    private static final Map<String, Collection.CollectionPetExp> map = new ConcurrentHashMap<>();

    public static Map<Integer, PetCollectExpCfgObject> _ix_petrarity = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetCollectExpCfg) o;
        initConfig();
        initFiled();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetCollectExpCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetCollectExpCfgObject getByPetrarity(int petrarity) {

        return _ix_petrarity.get(petrarity);

    }


    public void putToMem(Map e, PetCollectExpCfgObject config) {

        config.setPetrarity(MapHelper.getInt(e, "petRarity"));

        config.setExp(MapHelper.getInt(e, "exp"));


        _ix_petrarity.put(config.getPetrarity(), config);


    }

    public int calculateCanClaimCollectionExp(ProtocolStringList expIdList) {
        if (CollectionUtils.isEmpty(expIdList)) {
            return 0;
        }
        int result = 0;
        for (String s : expIdList) {
            String[] split = s.split(SEPARATOR);
            result += getExpByRarity(Integer.parseInt(split[0]));
        }
        return result;
    }

    public static Collection.CollectionPetExp getCollectExp(String addCfgId) {
        Collection.CollectionPetExp exp = map.get(addCfgId);
        if (exp != null) {
            return exp;
        }
        if (StringUtils.isEmpty(addCfgId) || !addCfgId.contains(SEPARATOR)) {
            return null;
        }
        String[] split = addCfgId.split(SEPARATOR);
        int rarity = Integer.parseInt(split[0]);
        int bookId = Integer.parseInt(split[1]);
        if (rarity < instance.startRarity) {
            return null;
        }
        synchronized (PetCollectExpCfg.class) {
            if ((exp = map.get(addCfgId)) == null) {
                exp = Collection.CollectionPetExp.newBuilder().setExp(getInstance().getExpByRarity(rarity))
                        .setPetBookId(bookId).setPetRarity(rarity).build();
                map.put(addCfgId, exp);
            }
        }
        return exp;
    }

    public static String rarityBookId2CollectCfgId(Integer rarity, Integer bookId) {
        return rarity + SEPARATOR + bookId;
    }


    public int getExpByRarity(int rarity) {
        PetCollectExpCfgObject cfg = getByPetrarity(rarity);
        if (cfg == null) {
            return 0;
        }
        return cfg.getExp();
    }

    private void initFiled() {
        startRarity = _ix_petrarity.values().stream()
                .mapToInt(PetCollectExpCfgObject::getPetrarity).filter(e -> e > 0).min().orElse(0);
        canClaimRarity=_ix_petrarity.values().stream().mapToInt(PetCollectExpCfgObject::getPetrarity)
                .filter(e->e>0).boxed().collect(Collectors.toList());
    }

}
