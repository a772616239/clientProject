package petrobot.system.endlessSpire;

import java.util.Random;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import protocol.EndlessSpire.CS_ClaimEndlessSpireInfo;
import protocol.EndlessSpire.CS_ClaimEndlessSpireRanking;
import protocol.EndlessSpire.CS_ClaimSpireAchievementReward;
import protocol.MessageId.MsgIdEnum;

@Controller
public class EndlessSpireManager {

    @Index(value = IndexConst.CLAIM_ENDLESS_INFO)
    public void claimEndlessInfo(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimEndlessSpireInfo_VALUE, CS_ClaimEndlessSpireInfo.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_ENDLESS_RANKING)
    public void claimEndlessRanking(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimEndlessSpireRanking_VALUE, CS_ClaimEndlessSpireRanking.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_SPIRE_ACHIEVEMENT_REWARDS)
    public void spireAchievementRewards(Robot robot) {
        if (robot == null) {
            return;
        }

        Random random = new Random();
        robot.getClient().send(MsgIdEnum.CS_ClaimSpireAchievementReward_VALUE,
                CS_ClaimSpireAchievementReward.newBuilder()
                        .setStepId(random.nextInt(10))
                        .setNodeIndex(random.nextInt(10)));
    }
}
