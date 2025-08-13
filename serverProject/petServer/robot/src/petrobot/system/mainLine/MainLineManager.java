package petrobot.system.mainLine;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import protocol.EndlessSpire.CS_ClaimEndlessSpireRanking;
import protocol.MainLine.CS_ClaimOnHookInfo;
import protocol.MainLine.CS_ClaimPassedRanking;
import protocol.MainLine.CS_QuickOnHook;
import protocol.MainLine.CS_SettleOnHookReward;
import protocol.MessageId.MsgIdEnum;
import protocol.RecentPassedOuterClass.CS_ClaimRecentPassed;

@Controller
public class MainLineManager {

    @Index(value = IndexConst.CLAIM_MAINLINE_INFO)
    public void claimEndlessRanking(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimEndlessSpireRanking_VALUE, CS_ClaimEndlessSpireRanking.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_ON_HOOK_INFO)
    public void claimOnHookInfo(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimOnHookInfo_VALUE, CS_ClaimOnHookInfo.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_PASSED_RANKING)
    public void claimPassedRanking(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimPassedRanking_VALUE, CS_ClaimPassedRanking.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_RECENT_PASSED_INFO)
    public void claimRecentPassedRanking(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimRecentPassed_VALUE, CS_ClaimRecentPassed.newBuilder());
    }

    @Index(value = IndexConst.QUICK_ON_HOOK)
    public void quickOnHook(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_QuickOnHook_VALUE, CS_QuickOnHook.newBuilder());
    }

    @Index(value = IndexConst.SETTLE_ON_HOOK_REWARD)
    public void settleOnHookReward(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_SettleOnHookReward_VALUE, CS_SettleOnHookReward.newBuilder());
    }
}
