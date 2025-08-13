package model.bosstower;

import cfg.BossTowerBossBuffConfigObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.collections4.CollectionUtils;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/07/29
 */
public class BossTowerBuffContainer {
    private List<BossTowerBossBuffConfigObject> prefixBuffList;
    private int totalWeight;

    public void add(BossTowerBossBuffConfigObject obj) {
        if (obj == null) {
            return;
        }

        if(prefixBuffList == null) {
            prefixBuffList = new ArrayList<>();
        }
        prefixBuffList.add(obj);

        totalWeight += obj.getWeight();
    }

    public BossTowerBossBuffConfigObject randomBuff() {
        if(CollectionUtils.isEmpty(prefixBuffList)) {
            LogUtil.error("BossTowerBuffContainer.randomBuff, buff list is empty");
            return null;
        }
        Random random = new Random();
        if (totalWeight >= 0) {
            int randomNum = random.nextInt(totalWeight);
            int curNum = 0;
            for (BossTowerBossBuffConfigObject object : prefixBuffList) {
                if ((curNum += object.getWeight()) >= randomNum) {
                    return object;
                }
            }
        }
        return prefixBuffList.get(random.nextInt(prefixBuffList.size()));
    }
}
