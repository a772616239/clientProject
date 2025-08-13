package model.ranking.ranking;

import common.GameConst;
import lombok.Getter;

public class NaturePetAbilityRanking  extends AbstractPetRanking{
    @Getter
    private final int petType = GameConst.NaturePetClass;
}
