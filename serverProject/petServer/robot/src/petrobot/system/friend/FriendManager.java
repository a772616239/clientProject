package petrobot.system.friend;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Friend.CS_ApplyAddFriend;
import protocol.Friend.CS_ClaimAllPointAndSend;
import protocol.Friend.CS_ClaimFriendInfo;
import protocol.Friend.CS_DeleteFriend;
import protocol.Friend.CS_DeleteFriend.Builder;
import protocol.Friend.CS_FriendRecommend;
import protocol.Friend.CS_RespondAddFriend;
import protocol.Friend.FriendBaseInfo;
import protocol.Friend.FriendInfo;
import protocol.MessageId.MsgIdEnum;

import java.util.Map;
import java.util.Random;

@Controller
public class FriendManager {

    @Index(value = IndexConst.CLAIM_FRIEND_INFO)
    public void claimFriendInfo(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimFriendInfo_VALUE, CS_ClaimFriendInfo.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_RECOMMEND_FRIEND)
    public void claimRecommendFriend(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_FriendRecommend_VALUE, CS_FriendRecommend.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_ALL_POINT_AND_SEND)
    public void claimAllPointAndSend(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimAllPointAndSend_VALUE, CS_ClaimAllPointAndSend.newBuilder());
    }

    @Index(value = IndexConst.DELETE_FRIEND)
    public void deleteFriend(Robot robot) {
        if (robot == null) {
            return;
        }
        Builder builder = CS_DeleteFriend.newBuilder();
        Map<String, FriendInfo> applyMap = robot.getData().getOwnedFriend();
        if (!applyMap.isEmpty()) {
            for (String s : applyMap.keySet()) {
                builder.setDeletePlayerIdx(s);
                break;
            }
        }

        Map<String, FriendBaseInfo> recommendMap = robot.getData().getRecommendMap();
        for (String s : recommendMap.keySet()) {
            builder.setDeletePlayerIdx(s);
            break;
        }

        robot.getClient().send(MsgIdEnum.CS_DeleteFriend_VALUE, builder);
    }

    @Index(value = IndexConst.APPLY_ADD_FRIEND)
    public void applyAddFriend(Robot robot) {
        if (robot == null) {
            return;
        }
        CS_ApplyAddFriend.Builder builder = CS_ApplyAddFriend.newBuilder();

        Map<String, FriendBaseInfo> recommendMap = robot.getData().getRecommendMap();
        for (String s : recommendMap.keySet()) {
            builder.addTargetPlayerIdx(s);
            break;
        }

        robot.getClient().send(MsgIdEnum.CS_ApplyAddFriend_VALUE, builder);
    }

    @Index(value = IndexConst.RESPOND_ADD_FRIEND)
    public void respondAddFriend(Robot robot) {
        if (robot == null) {
            return;
        }
        CS_RespondAddFriend.Builder builder = CS_RespondAddFriend.newBuilder();

        SyncExecuteFunction.executeConsumer(robot, r -> {
            Map<String, FriendBaseInfo> recommendMap = robot.getData().getApplyMap();
            if (recommendMap == null || recommendMap.isEmpty()) {
                robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            } else {
                for (String s : recommendMap.keySet()) {
                    builder.addRespondPlayerIdx(s);
                    builder.setAgree(new Random().nextInt(10) >= 5);
                    break;
                }
                robot.getClient().send(MsgIdEnum.CS_RespondAddFriend_VALUE, builder);
            }
        });
    }
}
