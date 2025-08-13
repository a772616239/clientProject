package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class NewBeeLog extends AbstractPlayerLog {
    private int newBeeStep;

    public NewBeeLog(String playerId, int newBeeStep) {
        super(playerId);
        this.newBeeStep = newBeeStep;
    }
}
