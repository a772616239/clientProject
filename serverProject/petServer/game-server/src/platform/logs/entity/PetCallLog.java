package platform.logs.entity;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.ConsumeLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.ReasonManager.Reason;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Consume;
import protocol.Common.Reward;

/**
 * 宠物召唤获得日志
 */
@Getter
@Setter
@NoArgsConstructor
public class PetCallLog extends AbstractPlayerLog {
    private List<RewardLog> gain;
    private List<ConsumeLog> consume;
    private String reason;

    public PetCallLog(String playerIdx, List<Reward> rewards, List<Consume> consumes, Reason reason) {
        super(playerIdx);
        this.gain = StatisticsLogUtil.buildRewardLogList(rewards);
        this.consume = StatisticsLogUtil.buildConsumeByList(consumes);
        this.reason = reason == null ? "" : reason.toString();
    }

    public PetCallLog(String playerIdx, List<Reward> rewards, Consume consume, Reason source) {
        this(playerIdx, rewards, Arrays.asList(consume), source);
    }

    public PetCallLog(String playerIdx, Reward rewards, Consume consume, Reason source) {
        this(playerIdx, Arrays.asList(rewards), Arrays.asList(consume), source);
    }
}
