package model.petmission.entity;

import cfg.GameConfig;
import common.GameConst;
import java.util.List;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import org.springframework.util.CollectionUtils;
import protocol.Common;
import util.LogUtil;
import util.TimeUtil;

public class PetMissionHelper {

    /**
     * 公式中变量1  首次累计数量
     */
    private static double var1;

    /**
     * 公式中变量2 最大消耗数量
     */
    private static int var2;


    /**
     * 公式中变量3 免费次数
     */
    private static int var3;

    /**
     * 公式中变量4 消耗增量
     */
    private static int var4;

    /**
     * 公式中变量5 道具类型
     */
    private static int var5;


    public static boolean init() {
        int[] petMissionRefreshVars = GameConfig.getById(GameConst.CONFIG_ID).getPetmissionrefreshvars();
        if (petMissionRefreshVars.length < 3) {
            LogUtil.error("pet mission refresh var num less than three in GameConfig");
            return false;
        }
        var2 = petMissionRefreshVars[0];
        if (var2 <= 0) {
            LogUtil.error("pet mission refresh var1 is zero");
        }

        var1 = petMissionRefreshVars[0];
        var2 = petMissionRefreshVars[1];
        var3 = petMissionRefreshVars[2];
        var4 = petMissionRefreshVars[3];
        var5 = petMissionRefreshVars[4];
        if (Common.RewardTypeEnum.forNumber(var5) == null) {
            LogUtil.error("pet mission refresh var consumeType is error");
            return false;
        }
        return true;
    }

    /**
     * @param playerIdx
     * @param nextRefreshTimes
     * @return
     */
    public static Common.Consume calculateRefreshNeed(String playerIdx, int nextRefreshTimes) {
        int count = calculateNextNum(nextRefreshTimes);
        return Common.Consume.newBuilder().setCount(count).setRewardTypeValue(var5).build();
    }

    private static int calculateNextNum(int nextRefreshTimes) {
        if (nextRefreshTimes <= var3) {
            return 0;
        }
        int num = (int) Math.min(var2, var1 + (nextRefreshTimes - var3 - 1) * var4);
        return Math.max(0, num);
    }

    /**
     * 找玩家(var2)小时挂机奖励中刷新所需要的资源类型 的数量
     *
     * @param playerIdx
     * @param curRefreshCount
     * @return
     */
    private static int calculateNumByOnHook(String playerIdx, int curRefreshCount) {
        mainlineEntity mainLine = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (mainLine == null) {
            LogUtil.warn("calculateRefreshNeed mainLine is null by playerIdx :{}", playerIdx);
            return 0;
        }
        List<Common.Reward> rewards = mainLine.calculateOnHookReward(var3 * TimeUtil.MS_IN_A_HOUR);

        if (CollectionUtils.isEmpty(rewards)) {
            return 0;
        }
        for (Common.Reward reward : rewards) {
            if (reward.getRewardTypeValue() == var5) {
                int num = calculateByRefreshCount(curRefreshCount, reward.getCount());
                if (num <= 0) {
                    LogUtil.error("calculateRefreshNeed costNum is zero by playerIdx:{},rewardType:{}", playerIdx, reward.getRewardTypeValue());
                    return 0;
                }
                return num;
            }
        }
        return 0;
    }

    private static int calculateByRefreshCount(int curRefreshCount, int baseNum) {
        double pow = Math.pow(var1, curRefreshCount) * baseNum;
        if (pow < 0 || pow > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) pow;
    }

}
