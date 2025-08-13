package model.pet.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 宠物共鸣加成
 */

@Getter
@Setter
@NoArgsConstructor
public class ResonanceAddition {
    private Map<Integer,Integer> addsProperty;
    private List<Integer> addsBuff;
    private long addFixAbility;
    private int abilityRate;
}
