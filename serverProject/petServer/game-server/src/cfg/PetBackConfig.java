/*CREATED BY TOOL*/

package cfg;

import java.util.*;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import model.pet.entity.PetComposeHelper;
import model.reward.RewardUtil;
import protocol.Common;
import protocol.PetMessage;

@annationInit(value = "PetBackConfig", methodname = "initConfig")
public class PetBackConfig extends baseConfig<PetBackConfigObject> {


    private static PetBackConfig instance = null;

    public static PetBackConfig getInstance() {

        if (instance == null)
            instance = new PetBackConfig();
        return instance;

    }


    public static Map<Integer, PetBackConfigObject> _ix_id = new HashMap<>();

    public static PetBackConfigObject getCfgByRarityAndClass(int petRarity, int typeById) {
        return _ix_id.values().stream().filter(e -> e.getPetclass() == typeById && e.getRarity() == petRarity).findAny().orElse(null);
    }

    public static List<Common.Reward> getRewardByPet(PetMessage.Pet pet) {
        PetBackConfigObject cfg = getCfgByRarityAndClass(pet.getPetRarity(), PetBaseProperties.getTypeById(pet.getPetBookId()));
        if (cfg == null) {
            return Collections.emptyList();
        }
        List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(cfg.getResources());
        int petCount = cfg.getPetcount();
        if (petCount > 0) {
            rewards.add(RewardUtil.parseReward(Common.RewardTypeEnum.RTE_Pet, PetComposeHelper.getComposeId(pet.getPetBookId(), 5), petCount));
        }
        return rewards;
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (PetBackConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PetBackConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static PetBackConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, PetBackConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setRarity(MapHelper.getInt(e, "rarity"));

        config.setPetclass(MapHelper.getInt(e, "petClass"));

        config.setResources(MapHelper.getIntArray(e, "resources"));

        config.setPetcount(MapHelper.getInt(e, "petCount"));


        _ix_id.put(config.getId(), config);


    }
}
