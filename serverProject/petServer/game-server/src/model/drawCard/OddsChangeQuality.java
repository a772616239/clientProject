package model.drawCard;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author huhan
 * @date 2020/05/07
 */

@Getter
@Setter
@ToString
public class OddsChangeQuality {
    /**
     * 品质
     */
    private int quality;
    /**
     * 获得指定品质后重置的概率
     */
    private int resetOdds;
    /**
     * 最大概率
     */
    private int maxOdds;
    /**
     * 概率增长
     */
    private int increaseOdds;

    public OddsChangeQuality(int quality, int resetOdds, int maxOdds, int increaseOdds) {
        this.quality = quality;
        this.resetOdds = resetOdds;
        this.maxOdds = maxOdds;
        this.increaseOdds = increaseOdds;
    }
}
