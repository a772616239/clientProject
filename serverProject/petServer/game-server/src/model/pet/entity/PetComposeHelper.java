package model.pet.entity;


import cfg.PetBaseProperties;

/**
 * 宠物合成相关
 */
public class PetComposeHelper {

    /**
     * 碎片合成宠物bookId起始id
     */
    private static final int fragmentPetIdStart = 100000;

    private static final int rarityRate = 100000;

    /**
     * @param bookId
     * @param rarity
     * @return 碎片品质*rarityRate+bookId
     */
    public static int getComposeId(int bookId, int rarity) {
        return bookId + rarity * rarityRate;
    }

    public static int getPetRarityByComposeId(int composeId) {
        if (composeId > fragmentPetIdStart) {
            return composeId / rarityRate;
        }
        return PetBaseProperties.getQualityByPetId(composeId);
    }

    public static int getBookIdByComposeId(int composeId) {
        if (composeId > fragmentPetIdStart) {
            return composeId % rarityRate;
        }
        return composeId;
    }

}
