package model.pet;

import cfg.HelpPetCfg;
import cfg.HelpPetCfgObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.pet.dbCache.petCache;
import protocol.Common;
import protocol.PetMessage;

public class HelpPetManager {

    @Getter
    private static HelpPetManager instance = new HelpPetManager();

    private static final Map<String, PetMessage.Pet> helpPet = new ConcurrentHashMap<>();

    public static PetMessage.Pet getHelpPet(String helpPetId) {
        return helpPet.get(helpPetId);
    }

    public boolean init() {
        for (HelpPetCfgObject cfg : HelpPetCfg._ix_id.values()) {
            PetMessage.Pet.Builder petBuilder = petCache.getInstance().getPetBuilder(cfg.getPetcfgid(), Common.RewardSourceEnum.RSE_FriendHelp_VALUE);
            if (petBuilder == null) {
                continue;
            }
            petBuilder.setPetLvl(cfg.getLevel());
            petBuilder.setPetRarity(cfg.getRarity());
            petBuilder.setId(String.valueOf(cfg.getId()));
            petBuilder.setSource(Common.RewardSourceEnum.RSE_HelpPet_VALUE);
            petBuilder = petCache.getInstance().refreshPetData(petBuilder, null);
            helpPet.put(petBuilder.getId(), petBuilder.build());
        }
        return true;
    }

}
