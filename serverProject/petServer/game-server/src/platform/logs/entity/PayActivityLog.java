package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Consume;

/**
 * @Description 付费活动日志(日礼包 / 月礼包 / 周礼包 / 成长基金 / 限时礼包 )
 * @Author hanx
 * @Date2020/8/18 0018 9:31
 **/
@Getter
@Setter
@NoArgsConstructor
public class PayActivityLog extends AbstractPlayerLog {

    private String goods_name;
    private String currency_name;
    private int consume;

    public PayActivityLog(String playerIdx, Consume consume, PayActivityEnum activityEnum) {
        super(playerIdx);
        this.goods_name = activityEnum.logName;
        this.currency_name = StatisticsLogUtil.getNameByTypeAndId(consume.getRewardType(), consume.getId());
        this.consume = consume.getCount();
    }

    public PayActivityLog(String playerIdx, String consumeType, PayActivityEnum activityEnum) {
        super(playerIdx);
        this.goods_name = activityEnum.logName;
        this.currency_name = consumeType;
    }

    public enum PayActivityEnum {
        DailyGift("日礼包"),
        WeeklyGift("周礼包"),
        MonthlyGift("月礼包"),
        GrowthFund("成长基金"),
        TimeLimitGift("限时礼包");

        private String logName;

        PayActivityEnum(String logName) {
            this.logName = logName;
        }

        public String value() {
            return logName;
        }
    }
}
