package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class OfferRewardLog extends AbstractPlayerLog {
    boolean publish;
    int grade;

    public OfferRewardLog(String playerIdx, boolean publish, int grade) {
        super(playerIdx);
        setPublish(publish);
        setGrade(grade);
    }
}
