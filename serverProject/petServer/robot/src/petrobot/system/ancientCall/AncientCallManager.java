package petrobot.system.ancientCall;

import java.util.Random;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import protocol.AncientCall.CS_CallAncient;
import protocol.AncientCall.CS_CallAncient.Builder;
import protocol.AncientCall.CS_PetTransfer;
import protocol.MessageId.MsgIdEnum;

//@Controller
public class AncientCallManager {

    @Index(value = IndexConst.CALL_ANCIENT)
    public void callAncient(Robot robot) {
        if (robot == null) {
            return;
        }
        Builder builder = CS_CallAncient.newBuilder();
        Random random = new Random();
        // 1- 5
        builder.setType(random.nextInt(6));
        builder.setDoCount(random.nextInt(10));
        robot.getClient().send(MsgIdEnum.CS_CallAncient_VALUE, builder);
    }

    @Index(value = IndexConst.PET_TRANSFER)
    public void petTransfer(Robot robot) {
        if (robot == null) {
            return;
        }

        CS_PetTransfer.Builder builder = CS_PetTransfer.newBuilder();
        //TODO
        builder.setPetIdx("");
        robot.getClient().send(MsgIdEnum.CS_PetTransfer_VALUE, builder);
    }

}
