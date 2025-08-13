package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import protocol.TargetSystem;

@Getter
@Setter
@NoArgsConstructor
public class PlayerTargetLog extends AbstractPlayerLog {
    private int targetType;
    private int val;

    public PlayerTargetLog(String playerIdx,TargetSystem.TargetTypeEnum type, int val) {
        super(playerIdx);
        this.targetType = type.getNumber();
        this.val = val;
    }
}
