package common;

public class RewardRecordConst {

    //最大值不超过63
    public static class DailyRewardRecord {

    }

    //最大值不超过63
    public static class OnceRewardRecord {
        //许愿池
        public static final int WishWellDay1 = 0;
        public static final int WishWellDay2 = 1;
        public static final int WishWellDay3 = 2;

        //零元购(钻石)
        public static final int ZeroCost1PurchaseDay1 = 3;
        public static final int ZeroCost1PurchaseDay2 = 4;
        public static final int ZeroCost1PurchaseDay3 = 6;
        public static final int ZeroCost1PurchaseDay4 = 7;
        public static final int ZeroCost1PurchaseDay5 = 8;
        public static final int ZeroCost1PurchaseDay6 = 9;
        public static final int ZeroCost1PurchaseDay7 = 10;

        //零元购(魔晶)
        public static final int ZeroCost2PurchaseDay1 = 11;
        public static final int ZeroCost2PurchaseDay2 = 12;
        public static final int ZeroCost2PurchaseDay3 = 13;
        public static final int ZeroCost2PurchaseDay4 = 14;
        public static final int ZeroCost2PurchaseDay5 = 15;
        public static final int ZeroCost2PurchaseDay6 = 16;
        public static final int ZeroCost2PurchaseDay7 = 17;
    }


    public enum RewardRecordEnum {
        DailyRewardRecord, OnceRewardRecord
    }

}
