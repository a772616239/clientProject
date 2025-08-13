package petrobot.system.activity;

import java.util.Map;
import java.util.Random;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Activity.CS_ClaimActivity;
import protocol.Activity.CS_ClaimActivityReward;
import protocol.Activity.CS_ClaimActivityReward.Builder;
import protocol.Activity.CS_ClaimNoviceReward;
import protocol.Activity.CS_SignIn;
import protocol.Activity.ClientActivity;
import protocol.MessageId.MsgIdEnum;

@Controller
public class ActivityManager {

    @Index(value = IndexConst.CLAIM_ACTIVITY_REWARD)
    public void claimActivityReward(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, robot1 -> {
            Map<Long, ClientActivity> clientActivities = robot.getData().getClientActivities();
            if (clientActivities != null || clientActivities.isEmpty()) {
                robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            } else {
                Random random = new Random();
                ClientActivity clientActivity = clientActivities.get(random.nextInt(clientActivities.size()));
                Builder builder = CS_ClaimActivityReward.newBuilder();
                builder.setActivityId(clientActivity.getActicityId());
                builder.setIndex(random.nextInt(clientActivity.getBuyItemListsCount()));
                robot.getClient().send(MsgIdEnum.CS_ClaimActivityReward_VALUE, builder);
            }
        });
    }

    @Index(value = IndexConst.CLAIM_ACTIVITY)
    public void claimActivity(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimActivity_VALUE, CS_ClaimActivity.newBuilder());
    }

    @Index(value = IndexConst.SIGN_IN)
    public void signIn(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_SignIn_VALUE, CS_SignIn.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_NOVICE)
    public void claimNovice(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_SignIn_VALUE, CS_SignIn.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_NOVICE_Reward)
    public void claimNoviceReward(Robot robot) {
        if (robot == null) {
            return;
        }

        CS_ClaimNoviceReward.Builder builder = CS_ClaimNoviceReward.newBuilder();
        Random random = new Random();
        builder.setType(random.nextInt(3));
        builder.setId(random.nextInt(20));
        robot.getClient().send(MsgIdEnum.CS_ClaimNoviceReward_VALUE, builder);
    }

}
