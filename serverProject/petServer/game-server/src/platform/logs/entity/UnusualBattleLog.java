package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.StatisticsLogUtil;
import protocol.Battle.BattleSubTypeEnum;

/**
 * @author huhan
 * @date 2020/07/26
 */
@Getter
@Setter
@NoArgsConstructor
public class UnusualBattleLog extends AbstractPlayerLog {
    /**
     * 战斗类型
     */
    private String battleType;
    private int battleTypeValue;
    /**
     * 预估计时间   ms
     */
    private long estimatedTime;

    /**
     * 实际时间  ms
     */
    private long realTime;

    /**
     * 玩家作弊的结果
     * * -1为平局，PVE中1为玩家胜利，2为怪物胜利，3为玩家投降
     */
    private int cheatedResult;

    /**
     * 战斗异常情况
     */
    private String unusualCondition;

    public UnusualBattleLog(String playerIdx, BattleSubTypeEnum battleType, long estimatedTime, long realTime
            , int cheatedResult) {
        super(playerIdx);
        if (battleType != null) {
            this.battleType = StatisticsLogUtil.getBattleSubTypeName(battleType);
            this.battleTypeValue = battleType.getNumber();
        }
        this.estimatedTime = estimatedTime;
        this.realTime = realTime;
        this.cheatedResult = cheatedResult;
    }

    public UnusualBattleLog setUnusualCondition(String unusualCondition) {
        this.unusualCondition = unusualCondition;
        return this;
    }

    public static class UnusualCondition {
        public static final String CHEATED = "作弊";
        public static final String SPEECH = "加速";
        public static final String GM = "GM";
    }
}
