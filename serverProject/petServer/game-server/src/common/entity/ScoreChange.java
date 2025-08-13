package common.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScoreChange {
    private int playerScoreChange;
    private int opponentScoreChange;
}
