package model.activity.petAvoidance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 魔灵大躲避 游戏数据
 */
@Getter
@Setter
@AllArgsConstructor
public class PetAvoidanceGameData {
    String playerIdx;
    int curScore;
    long startTime;
    long endTime;
}
