package model.activity.petAvoidance;

import cfg.PetEscapeBulletCfg;
import cfg.PetEscapeBulletCfgObject;
import cfg.PetEscapeBulletInvokeCfg;
import cfg.PetEscapeBulletInvokeCfgObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import util.LogUtil;

public class ScoreValidator {

    private int maxRangeInCfg;
    private float maxUnitInvokeRate;
    // KEY: 游戏时长  Value: 该游戏时长对应的最大积分
    private Map<Integer, Integer> maxScoreMap;

    public boolean init() {
        int maxUnitScore = 0;
        for (PetEscapeBulletCfgObject petEscapeBulletCfgObject : PetEscapeBulletCfg._ix_id.values()) {
            if (petEscapeBulletCfgObject != null && maxUnitScore < petEscapeBulletCfgObject.getAddscore()) {
                maxUnitScore = petEscapeBulletCfgObject.getAddscore();
            }
        }
        Map<Integer, Integer> maxScoreMap = new HashMap<>();

        TreeMap<Integer, Float> unitInvokeRateMap = new TreeMap<>();
        for (PetEscapeBulletInvokeCfgObject petEscapeBulletInvokeCfgObject : PetEscapeBulletInvokeCfg._ix_id.values()) {
            if (petEscapeBulletInvokeCfgObject != null) {
                int[] rangetime = petEscapeBulletInvokeCfgObject.getRangetime();
                int rightRange = rangetime[1];
                // 每毫秒数量
                float numPerMills = (float) petEscapeBulletInvokeCfgObject.getInvokepointnum() / petEscapeBulletInvokeCfgObject.getInvokeinterval();
                float numPerSecond = numPerMills * 1000;
                unitInvokeRateMap.put(rightRange, numPerSecond);

                if (maxRangeInCfg < rightRange) {
                    maxRangeInCfg = rightRange;
                    maxUnitInvokeRate = numPerSecond;
                }

            }
        }
        int leftRange = 1;
        for (Entry<Integer, Float> entry : unitInvokeRateMap.entrySet()) {
            for (int i = leftRange; i <= entry.getKey(); i++) {
                Integer preScore = maxScoreMap.getOrDefault(leftRange, 0);
                int totalScore = preScore + (int) Math.ceil((i - leftRange) * entry.getValue() * maxUnitScore);
                if (totalScore < 0) {
                    LogUtil.warn("ScoreValidator init err. secondTime:{}, maxUnitScore:{}, n:{}, preScore:{}, totalScore:{}",
                            i, maxUnitScore, (i - leftRange) * entry.getValue(), preScore, totalScore);
                    return false;
                }
                maxScoreMap.put(i, totalScore);
            }
            leftRange = entry.getKey();
        }

        this.maxScoreMap = maxScoreMap;
        return true;
    }

    /**
     * 确保积分正确
     * @param score
     * @param gameTime 单位 秒
     * @param maxTime 单位 秒
     * @return
     */
    public int makeRightScore(int score, long gameTime, long maxTime) {
        long time = Math.min(gameTime, maxTime);
        if (time > maxRangeInCfg) {
            Integer pre = maxScoreMap.getOrDefault(maxRangeInCfg, 0);
            return Math.min(score, pre + (int)Math.ceil((time - maxRangeInCfg) * maxUnitInvokeRate));
        }else {
            return Math.min(score, maxScoreMap.getOrDefault((int)time, 0));
        }
    }
}
