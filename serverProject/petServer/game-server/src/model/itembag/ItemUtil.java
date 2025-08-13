package model.itembag;

import cfg.GameConfig;
import cfg.Item;
import cfg.ItemObject;
import common.GameConst;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.util.CollectionUtils;
import protocol.Common;

public class ItemUtil {

    public static boolean mistBox(int itemId) {
        return mistBox(Item.getById(itemId));
    }

    public static boolean mistBox(ItemObject item) {

        if (item == null) {
            return false;
        }

        return ItemConst.mistBoxType.contains(item.getSpecialtype());
    }

    /**
     * 解析迷雾森林宝箱中的稀有道具
     * @param rewards
     * @return
     */
    public static List<Common.Reward> parseMistMarqueeRareReward(List<Common.Reward> rewards) {
        if (CollectionUtils.isEmpty(rewards)) {
            return Collections.emptyList();
        }

        int[][] mistmarqueerewardtype = GameConfig.getById(GameConst.CONFIG_ID).getMistmarqueerewardtype();

        List<Common.Reward> rareReward = null;
        for (Common.Reward reward : rewards) {
            for (int[] ints : mistmarqueerewardtype) {
                if (ints.length != 2) {
                    continue;
                }
                if (ints[0] == reward.getRewardTypeValue() && ints[1] == reward.getId()) {
                    if (rareReward == null) {
                        rareReward = new ArrayList<>();
                    }
                    rareReward.add(reward);
                }
            }
        }
        return rewards;
    }
}
