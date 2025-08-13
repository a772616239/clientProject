package petrobot.system.player;


import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robot.net.Client;
import petrobot.robotConst.IndexConst;
import petrobot.util.LogUtil;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.*;
import protocol.PlayerInfo.CS_AlterName.Builder;

import java.util.List;
import java.util.Random;

@Controller
public class PlayerManager {

    @Index(value = IndexConst.ALTER_NAME)
    public void alterName(Robot robot) {
        if (robot == null) {
            return;
        }
        Client client = robot.getClient();
        if (client == null) {
            LogUtil.error("Robot client is null, id = " + robot.getId());
        }
        Builder builder = CS_AlterName.newBuilder();
        builder.setNewName(robot.getLoginName());
        robot.getClient().send(MsgIdEnum.CS_AlterName_VALUE, builder);
    }

    @Index(value = IndexConst.CHANGE_AVATAR)
    public void ChangeAvatar(Robot robot) {
        if (robot == null) {
            return;
        }

        SC_PlayerBaseInfo.Builder baseInfo = robot.getData().getBaseInfo();
        if (baseInfo == null) {
            LogUtil.error("robot base info is null, id = " + robot.getId());
            return;
        }

        int avatar = baseInfo.getAvatar();
        List<Integer> ownedAvatarList = baseInfo.getOwnedAvatarList();
        CS_ChangeAvatar.Builder builder = CS_ChangeAvatar.newBuilder();
        for (Integer integer : ownedAvatarList) {
            if (avatar != integer) {
                builder.setNewAvatarId(integer);
                break;
            }
        }
        robot.getClient().send(MsgIdEnum.CS_ChangeAvatar_VALUE, builder);
    }

    @Index(value = IndexConst.GOLD_EXCHANGE)
    public void goldExchange(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_GoldExchange_VALUE, CS_GoldExchange.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_VIP_GIFT)
    public void claimVipGift(Robot robot) {
        CS_ClaimVipLvGift.Builder builder = CS_ClaimVipLvGift.newBuilder();
        builder.setVipLv(1 + new Random().nextInt(10));
        robot.getClient().send(MsgIdEnum.CS_ClaimVipLvGift_VALUE, builder);
    }

}