package platform.logs.entity;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.LogClass.PetLog;
import platform.logs.StatisticsLogUtil;
import platform.logs.AbstractPlayerLog;
import protocol.Battle.SC_EnterFight;

@Getter
@Setter
@NoArgsConstructor
public class SpireLog extends AbstractPlayerLog {
    private int spireLv;
    private long challengeTime;
    private boolean result;
    private List<PetLog> petData;

    public SpireLog(String playerIdx, int spireLv, long challengeTime, boolean result, SC_EnterFight.Builder battleData) {
        super(playerIdx);
        this.spireLv = spireLv;
        this.challengeTime = challengeTime;
        this.result = result;
        this.petData = StatisticsLogUtil.buildPetLogListByBattleData(battleData, 1);
    }
}
