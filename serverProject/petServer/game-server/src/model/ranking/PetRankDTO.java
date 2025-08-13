package model.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PetRankDTO {
    private String playerId;
    private String petId;
    private int petBookId;
    private long score;
}
