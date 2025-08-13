package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class MistPvpTimesLog extends AbstractPlayerLog {
    public MistPvpTimesLog(String playerIdx) {
        super(playerIdx);
    }
}
