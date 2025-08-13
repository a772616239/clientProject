package petrobot.system.drawCard;

import java.util.Random;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import protocol.DrawCard.CS_AbandonHighPool;
import protocol.DrawCard.CS_ClaimDrawCardInfo;
import protocol.DrawCard.CS_ClaimSelectedPet;
import protocol.DrawCard.CS_DrawCommonCard;
import protocol.DrawCard.CS_DrawCommonCard.Builder;
import protocol.DrawCard.CS_DrawHighCard;
import protocol.DrawCard.CS_EnsureHighDrawResult;
import protocol.DrawCard.CS_FriendShipDrawCard;
import protocol.DrawCard.CS_ResetHighCardPool;
import protocol.MessageId.MsgIdEnum;

@Controller
public class DrawCardManager {

    @Index(value = IndexConst.CLAIM_DRAW_CARD_INFO)
    public void claimDrawCardInfo(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimDrawCardInfo_VALUE, CS_ClaimDrawCardInfo.newBuilder());
    }

    @Index(value = IndexConst.DRAW_COMMON_CARD)
    public void drawCommonCard(Robot robot) {
        if (robot == null) {
            return;
        }

        Builder builder = CS_DrawCommonCard.newBuilder();
        builder.setDrawCount(new Random().nextInt(10));
        robot.getClient().send(MsgIdEnum.CS_DrawCommonCard_VALUE, builder);
    }

    @Index(value = IndexConst.DRAW_HIGH_CARD)
    public void drawHighCard(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_DrawHighCard_VALUE, CS_DrawHighCard.newBuilder());
    }

    @Index(value = IndexConst.RESET_HIGH_CARD)
    public void resetHighCard(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ResetHighCardPool_VALUE, CS_ResetHighCardPool.newBuilder());
    }

    @Index(value = IndexConst.DRAW_FRIEND_SHIP_CARD)
    public void drawFriendShipCard(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_FriendShipDrawCard_VALUE,
                CS_FriendShipDrawCard.newBuilder().setDrawCount(new Random().nextInt(10)));
    }

    @Index(value = IndexConst.ABANDON_HIGH_POOL)
    public void abandonHighPool(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_AbandonHighPool_VALUE, CS_AbandonHighPool.newBuilder());
    }

    @Index(value = IndexConst.ENSURE_HIGH_DRAW_CARD)
    public void ensureHighDrawCard(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_EnsureHighDrawResult_VALUE,
                CS_EnsureHighDrawResult.newBuilder().setEnsure(new Random().nextBoolean()));
    }

    @Index(value = IndexConst.CLAIM_SELECTED_PET)
    public void claimSelectPet(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimSelectedPet_VALUE, CS_ClaimSelectedPet.newBuilder());
    }
}
