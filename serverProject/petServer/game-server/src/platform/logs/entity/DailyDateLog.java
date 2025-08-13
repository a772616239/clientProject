package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.ReasonManager.Reason;
import platform.logs.StatisticsLogUtil;
import protocol.Common.RewardTypeEnum;

@Getter
@Setter
@NoArgsConstructor
public class DailyDateLog extends AbstractPlayerLog {
    private int changeType;
    private boolean consume;
    private String name;
    private long before;
    private long changed;
    private long remain;
    private int id;
    private String reason;
    private int reasonValue;
    private long ns;

    public DailyDateLog(String playerIdx, boolean consume, RewardTypeEnum type, int id, long before, long changed, long remain, Reason reason) {
        super(playerIdx);
        if (type != null) {
            this.changeType = type.getNumber();
        }
        this.consume = consume;
        this.id = id;
        this.name = StatisticsLogUtil.getNameByTypeAndId(type, id);
        this.before = before;
        this.changed = changed;
        this.remain = remain;
        if (reason != null) {
            this.reason = reason.toString();
            this.reasonValue = reason.getSourceNum();
        }
        this.ns = System.nanoTime();
    }
}
