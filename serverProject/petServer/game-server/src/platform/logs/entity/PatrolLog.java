package platform.logs.entity;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Reward;
import protocol.Patrol.PatrolStatus;

@Setter
@Getter
@NoArgsConstructor
public class PatrolLog extends AbstractPlayerLog {
    private List<RewardLog> rewardList;
    private int mapId;
    private int x;
    private int y;
    private int greedChange;
    private int greed;
    private String event;

    public PatrolLog(String playerId, PatrolStatus patrolStatus, List<Reward> newReward,
                     int greedChange, String event, int x, int y) {
        super(playerId);
        this.rewardList = StatisticsLogUtil.buildRewardLogList(newReward);
        if (patrolStatus != null) {
            this.mapId = patrolStatus.getMapId();
            this.greed = patrolStatus.getGreed();
        }
        this.x = x;
        this.y = y;
        this.greedChange = greedChange;
        this.event = event;
    }

    public static class PatrolEvent {
        public static final String BATTLE_VICTORY = "战斗胜利";
        public static final String BATTLE_FAILED = "战斗失败";
        public static final String ENTER = "进入秘境探索";
        public static final String FINISHED = "秘境探索结束";
        public static final String EXPLORE = "探索";
        public static final String BOX = "宝箱";
        public static final String ENTER_BATTLE = "进入战斗";
    }
}
