package model.ancientCall;

import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetTransfer;
import cfg.PetTransferObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import util.LogUtil;

public class PetTransferManager {
    private static PetTransferManager instance = new PetTransferManager();

    public static PetTransferManager getInstance() {
        if (instance == null) {
            synchronized (PetTransferManager.class) {
                if (instance == null) {
                    instance = new PetTransferManager();
                }
            }
        }
        return instance;
    }

    private PetTransferManager() {
    }

    /**
     * <Rarity, <petType, List<petCfg>>
     */
    private final Map<Integer, Map<Integer, List<PetTransferObject>>> petTransfer = new HashMap<>();
    private final Random random = new Random();

    private final Set<Integer> totalCanTransPet = new HashSet<>();

    public synchronized boolean init() {
        //classify by pet quality
        for (PetTransferObject value : PetTransfer._ix_id.values()) {
            if (value.getOdds() <= 0) {
                LogUtil.error("model.ancientCall.PetTransferManager.init, error odds cfg = " + value.getOdds());
                return false;
            }

            PetBasePropertiesObject petCfg = PetBaseProperties.getByPetid(value.getTargetpetid());
            if (petCfg == null) {
                LogUtil.error("model.ancientCall.PetTransferManager.init, error petCfg = " + value.getId());
                return false;
            }

            Map<Integer, List<PetTransferObject>> petTypeMap = petTransfer.computeIfAbsent(petCfg.getStartrarity(), k -> new HashMap<>());
            List<PetTransferObject> petTransferObjects = petTypeMap.computeIfAbsent(petCfg.getPettype(), s -> new ArrayList<>());
            petTransferObjects.add(value);

            totalCanTransPet.add(value.getTargetpetid());
        }
        return true;
    }

    public boolean canTransfer(int petId) {
        return this.totalCanTransPet.contains(petId);
    }

    /**
     *
     * @param startRarity
     * @return -1未随机成功
     */
    public int doTransfer(int sourceCfgId, int startRarity, int petType) {
        Map<Integer, List<PetTransferObject>> integerListMap = petTransfer.get(startRarity);
        if (integerListMap == null) {
            return -1;
        }

        List<PetTransferObject> petTransferObjects = integerListMap.get(petType);
        if (petTransferObjects == null || petTransferObjects.isEmpty()) {
            return -1;
        }

        int totalOdds = 0;
        for (PetTransferObject petTransferObject : petTransferObjects) {
            totalOdds += petTransferObject.getOdds();
        }

        for (int i = 0; i < 5; i++) {
            if (totalOdds <= 0) {
                return petTransferObjects.get(random.nextInt()).getTargetpetid();
            } else {
                int num = 0;
                int curOdds = random.nextInt(totalOdds);
                for (PetTransferObject petTransferObject : petTransferObjects) {
                    if ((num += petTransferObject.getOdds()) > curOdds) {
                        int targetPetId = petTransferObject.getTargetpetid();
                        if (targetPetId != sourceCfgId) {
                            return targetPetId;
                        }
                        break;
                    }
                }
            }
        }
        return -1;
    }
}