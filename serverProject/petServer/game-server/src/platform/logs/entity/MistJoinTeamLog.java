package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class MistJoinTeamLog extends AbstractPlayerLog {
    public MistJoinTeamLog(String playerIdx) {
        super(playerIdx);
    }
}
