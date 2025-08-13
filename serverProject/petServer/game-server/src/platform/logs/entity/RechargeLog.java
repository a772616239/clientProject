package platform.logs.entity;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.RewardLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Reward;

/**
 * @author huhan
 * @date 2020.10.29
 * 充值日志
 */
@Getter
@Setter
@NoArgsConstructor
public class RechargeLog extends AbstractPlayerLog {
    /**
     * 订单号
     */
    private String orderId;

    /**
     * 发放状态
     */
    private boolean flag;

    /**
     * 具体信息
     */
    private String flagMsg;

    /**
     * 充值奖励
     */
    private List<RewardLog> goods;

    /**
     * 充值成功构造器
     *
     * @param playerIdx
     * @param orderId
     * @param rewards
     */
    public RechargeLog(String playerIdx, String orderId, List<Reward> rewards) {
        super(playerIdx);
        this.orderId = orderId;
        this.flag = true;
        this.goods = StatisticsLogUtil.buildRewardLogList(rewards);
    }

    /**
     * 充值失败构造器
     *
     * @param playerIdx
     * @param orderId
     * @param flagMsg
     */
    public RechargeLog(String playerIdx, String orderId, String flagMsg) {
        super(playerIdx);
        this.flag = false;
        this.orderId = orderId;
        this.flagMsg = flagMsg;
    }

}
