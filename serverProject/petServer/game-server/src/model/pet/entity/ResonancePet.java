package model.pet.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ResonancePet {

    private int petBookId;
    private int petRarity;

    public ResonancePet(int petBookId, int rarity) {
        this.petBookId = petBookId;
        this.petRarity = rarity;
    }
}
