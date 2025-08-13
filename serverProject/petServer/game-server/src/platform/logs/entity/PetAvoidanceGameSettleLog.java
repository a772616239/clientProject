package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.activity.petAvoidance.PetAvoidanceGameData;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class PetAvoidanceGameSettleLog extends AbstractPlayerLog {
    long gameTime;
    long maxTime;
    int cliScore;
    int realScore;
    int challengedTimes;

    public PetAvoidanceGameSettleLog(String playerIdx, long gameTime, long maxTime, int cliScore, int realScore, int challengedTimes) {
        this(playerCache.getByIdx(playerIdx), gameTime, maxTime, cliScore, realScore, challengedTimes);
    }

    public PetAvoidanceGameSettleLog(playerEntity playerEntity, long gameTime, long maxTime, int cliScore, int realScore, int challengedTimes) {
        super(playerEntity);
        setGameTime(gameTime);
        setMaxTime(maxTime);
        setCliScore(cliScore);
        setRealScore(realScore);
        setChallengedTimes(challengedTimes);
    }
}
