package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.ReasonManager.Reason;

@Getter
@Setter
@NoArgsConstructor
public class MistIntegralLog extends AbstractPlayerLog {
    private int integral;

    private String rewardSource;

    public MistIntegralLog(String playerIdx, int integral, Reason reason) {
        super(playerIdx);
        setIntegral(integral);

        this.rewardSource = reason == null ? "" : reason.toString();
    }
}
