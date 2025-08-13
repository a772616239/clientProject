package petrobot.system.resCopy;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import protocol.MessageId.MsgIdEnum;
import protocol.ResourceCopy.CS_BuyTimes;
import protocol.ResourceCopy.CS_ClaimResCopy;
import protocol.ResourceCopy.CS_SweepCopy;

@Controller
public class ResCopyManager {
    @Index(value = IndexConst.CLAIM_RES_COPY_INFO)
    public void settleOnHookReward(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimResCopy_VALUE, CS_ClaimResCopy.newBuilder());
    }

    @Index(value = IndexConst.BUY_TIMES)
    public void buyTimes(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_BuyTimes_VALUE, CS_BuyTimes.newBuilder());
    }

    @Index(value = IndexConst.SWEEP_RES_COPY)
    public void sweepResCopy(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_SweepCopy_VALUE, CS_SweepCopy.newBuilder());
    }
}
