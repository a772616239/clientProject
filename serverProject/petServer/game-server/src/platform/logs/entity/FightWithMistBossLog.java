package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class FightWithMistBossLog extends AbstractPlayerLog {
    private boolean battleResult;

    public FightWithMistBossLog(String playerIdx, boolean result) {
        super(playerIdx);
        this.battleResult = result;
    }
}
