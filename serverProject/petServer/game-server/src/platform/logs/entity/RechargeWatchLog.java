package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class RechargeWatchLog extends AbstractPlayerLog {
    private int watchType;
    private String name;
    private int num;

    public RechargeWatchLog(String playerId, int type, String name, int num) {
        super(playerId);
        this.watchType = type;
        this.name = name;
        this.num = num;
    }
}
