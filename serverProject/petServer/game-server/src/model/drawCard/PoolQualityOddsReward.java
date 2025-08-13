package model.drawCard;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

/**
 * @author huhan
 * @date 2020/05/07
 */
@Getter
public class PoolQualityOddsReward {
    private int totalOdds;
    private List<OddsRandom> oddsRandomList;

    private void addOdds(int newOdds) {
        this.totalOdds += newOdds;
    }

    public void addOddsRandom(OddsRandom oddsRandom) {
        if (oddsRandom == null) {
            return;
        }
        if (this.oddsRandomList == null) {
            this.oddsRandomList = new ArrayList<>();
        }
        this.oddsRandomList.add(oddsRandom);
        this.addOdds(oddsRandom.getOdds());
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(oddsRandomList);
    }
}
