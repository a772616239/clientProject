package model.drawCard;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author huhan
 * @date 2020/04/01
 */
@Getter
@Setter
@ToString
public class QualityWeight {
    private int quality;
    private int weight;

    public QualityWeight(int quality, int weight) {
        this.quality = quality;
        this.weight = weight;
    }
}
