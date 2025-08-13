package model.itembag;

import java.util.Arrays;
import java.util.List;

public class ItemConst {
    /**
     * 1:迷雾森林宝箱（每日凌晨自动清除）
     * 2:只用作展示的道具
     * 3:迷雾森林boss宝箱
     * 4.迷雾森林新手宝箱
     * 5.符文经验道具
     * 6.迷雾森林队友宝箱
     * 7.等级宝箱
     * 8.盲盒
     */
    public static class ItemType {
        public static final int Normal = 0;
        public static final int MIST_BOX = 1;
        public static final int ONLY_USE_FOR_DISPLAY = 2;
        public static final int Mist_Boss_Box = 3;
        public static final int MIST_NEW_BEE_BOX = 4;
        public static final int RUNE_EXP_ITEM = 5;
        public static final int Mist_Team_Box = 6;
        public static final int Blind_Box = 8;
        public static final int Purchase = 9;

        public static final int TrainItem = 27;

    }

    public static List<Integer> mistBoxType = Arrays.asList(ItemType.MIST_BOX, ItemType.Mist_Boss_Box, ItemType.Mist_Team_Box);
}
