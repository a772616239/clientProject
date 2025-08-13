package model.matcharena;

import java.io.Serializable;
import lombok.Data;
import protocol.Common;

@Data
public class MatchArenaBattleReward implements Serializable {
    private static final long serialVersionUID = 5373034982794765961L;
    private Common.Reward giftReward;
    private Common.Reward baseReward;
    private int incrBaseRewardTimes;
}
