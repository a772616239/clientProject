//package model.foreigninvasion.old;
//
//import cfg.ForeignInvasionRankingReward;
//import cfg.ForeignInvasionRankingRewardObject;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import model.reward.RewardUtil;
//import protocol.Common.Reward;
//import util.ArrayUtil;
//import util.LogUtil;
//
//public class ForeignInvasionUtil {
//
//    /**
//     * 获取下一个开放日（1-7）
//     *
//     * @param openDay
//     * @param toDayInWeek
//     * @return
//     */
//    public static int getNextOpenDay(int[] openDay, int toDayInWeek) {
//        if (openDay == null || toDayInWeek < 1 || toDayInWeek > 7) {
//            return 0;
//        }
//
//        List<Integer> list = excludeElement(openDay, new int[]{1, 2, 3, 4, 5, 6, 7});
//        list.sort(Integer::compareTo);
//
//        int findIndex = list.indexOf(toDayInWeek);
//        if (findIndex == -1 || findIndex >= (list.size() - 1)) {
//            return list.get(0);
//        } else {
//            return list.get(findIndex + 1);
//        }
//    }
//
//    /**
//     * 剔除除指定元素之外的元素
//     *
//     * @param checkIntArr  需要筛选的数组
//     * @param containArr  给定元素数组
//     * @return
//     */
//    public static List<Integer> excludeElement(int[] checkIntArr, int[] containArr) {
//        List<Integer> array = new ArrayList<>();
//
//        if (checkIntArr == null || containArr == null) {
//            return array;
//        }
//
//        for (int i = 0; i < checkIntArr.length; i++) {
//            if (ArrayUtil.intArrayContain(containArr, checkIntArr[i])) {
//                array.add(checkIntArr[i]);
//            }
//        }
//
//        return array;
//    }
//
//    /**
//     * 获取外敌入侵排行奖励
//     * @param ranking 排名
//     * @param weekNum 当前周距离第一次开启外敌入侵的周数
//     * @param dayOfWeek 当前是周几
//     * @return
//     */
//    public static List<Reward> getRankingReward(int ranking, int weekNum, int dayOfWeek) {
//        Map<Integer, ForeignInvasionRankingRewardObject> ix_id = ForeignInvasionRankingReward._ix_id;
//        if (ix_id == null || ix_id.isEmpty()) {
//            return null;
//        }
//
//        int findRewardId = 0;
//        for (ForeignInvasionRankingRewardObject value : ix_id.values()) {
//            if (value.getStartranking() <= ranking && ranking <= value.getEndranking()) {
//                //{{周几,第一周奖励，第二周奖励…}}
//                int[][] rewards = value.getRewards();
//                for (int[] reward : rewards) {
//                    if (reward.length <= 0) {
//                        LogUtil.warn("model.foreignInvasion.ForeignInvasionUtil.getRankingReward, forInv ranking rewards length is less than 0");
//                        continue;
//                    }
//                    if (reward[0] == dayOfWeek) {
//                        if (weekNum >= reward.length - 1) {
//                            findRewardId = reward[reward.length - 1];
//                        } else {
//                            findRewardId = reward[weekNum];
//                        }
//                    }
//                }
//            }
//        }
//
//        if (findRewardId != 0) {
//            return RewardUtil.getRewardsByRewardId(findRewardId);
//        }
//
//        LogUtil.error("model.foreignInvasion.ForeignInvasionUtil.getRankingReward, can not find ranking reward,  weekNum:" + weekNum + ", dayOfWeek:" + dayOfWeek);
//        return null;
//    }
//
//    /**
//     * 获得最大排行榜奖励的最大排行
//     * @return
//     */
//    public static int getMaxRanking() {
//        int maxRanking = 0;
//        for (ForeignInvasionRankingRewardObject value : ForeignInvasionRankingReward._ix_id.values()) {
//            if(value.getEndranking() > maxRanking) {
//                maxRanking = value.getEndranking();
//            }
//        }
//        return maxRanking;
//    }
//}
