package platform.logs.entity;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import platform.logs.LogClass.RewardLog;
import platform.logs.StatisticsLogUtil;
import platform.logs.AbstractPlayerLog;
import protocol.BraveChallenge.ChallengeProgress;
import protocol.Common.Reward;

@Getter
@Setter
@NoArgsConstructor
public class BraveChallengeLog extends AbstractPlayerLog {
    private List<RewardLog> reward;
//    private int difficulty;
    private int fightMakeId;
    private String event;
    private long remainCount;

    public BraveChallengeLog(String playerId, ChallengeProgress progress, List<Reward> rewardList, String event, int fightMakeId) {
        super(playerId);
        this.reward = StatisticsLogUtil.buildRewardLogList(rewardList);
//        if (progress != null) {
//            this.difficulty = progress.getDifficulty();
//        }
        this.fightMakeId = fightMakeId;
        this.event = event;
        itembagEntity bag = itembagCache.getInstance().getItemBagByPlayerIdx(playerId);
        this.remainCount = bag == null ? 0 : bag.getItemCount(1041);
    }
}
