package model.drawCard;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import model.reward.RewardUtil;

/**
 * @author huhan
 * @date 2020/07/27
 */
@Getter
@Setter
@ToString
public class OddsRandomImpl implements OddsRandom{
    private int id;
    private int quality;
    private int odds;
    private int[] rewards;
    private boolean isCorePet;

    public OddsRandomImpl(int[] rewards) {
        this.rewards = rewards;
        if (rewards!= null && rewards.length >= 3) {
            this.quality = RewardUtil.getQuality(rewards[0], rewards[1]);
        }
    }

    public OddsRandom setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    @Override
    public boolean getIscorepet() {
        return isCorePet;
    }
}
