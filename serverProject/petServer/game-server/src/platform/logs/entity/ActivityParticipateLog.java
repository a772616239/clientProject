package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.StatisticsLogUtil;
import protocol.Activity.ActivityTypeEnum;

/**
 * @author huhan
 * @date 2020.10.22
 * <p>
 * 活动参与次数
 * <p>
 * 魔灵降临只统计抽卡次数
 */
@Getter
@Setter
@NoArgsConstructor
public class ActivityParticipateLog extends AbstractPlayerLog {
    /**
     * 活动类型值
     */
    private int activityTypeNum;
    /**
     * 活动类型中文
     */
    private String activityTypeName;
    /**
     * 参与次数
     */
    private int times;

    /**
     * 关联活动Id
     */
    private long activityId;

    public ActivityParticipateLog(String playerIdx, ActivityTypeEnum activityType, long activityId) {
        this(playerIdx, activityType, activityId, 1);
    }

    public ActivityParticipateLog(String playerIdx, ActivityTypeEnum activityType, long activityId, int times) {
        super(playerIdx);
        if (activityType != null) {
            this.activityTypeNum = activityType.getNumber();
            this.activityTypeName = StatisticsLogUtil.getActivityTypeName(activityType);
        }
        this.times = times;
        this.activityId = activityId;
    }
}
