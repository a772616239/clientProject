package petrobot.system.feats;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.CS_GetFeatsInfo;

@Controller
public class FeatsManager {

    private static final String advancedFeatsGm = "purchase|gxgas19.99";

    @Index(value = IndexConst.Feats_Info)
    public void claimFeatsInfo(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_GetFeatsInfo_VALUE, CS_GetFeatsInfo.newBuilder());
    }

    private static final int advanceFeats = 1;

    @Index(value = IndexConst.ClaimFeatsReward)
    public void buyCard(Robot robot) {
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
//        SC_GetFeatsInfo featsInfos = robot.getData().getFeatsInfos();
//        if (featsInfos == null || featsInfos.getInfoCount() <= 0) {
//            robot.setDealResult(DealResultConst.CUR_STEP_FAILED);
//            return;
//        }
//        int claimIndex = 0;
//        for (FeatsInfo featInfo : featsInfos.getInfoList()) {
//            claimIndex = featInfo.getClaimedAdvancedRewardList().stream().max(Integer::compareTo).orElse(0);
//            if (claimIndex > 0) {
//                break;
//            }
//        }
//        robot.getClient().send(MsgIdEnum.CS_ClaimFeatsReward_VALUE, CS_ClaimFeatsReward.newBuilder().setCfgId(++claimIndex));
    }
}

