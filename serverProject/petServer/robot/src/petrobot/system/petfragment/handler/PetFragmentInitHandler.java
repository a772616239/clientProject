package petrobot.system.petfragment.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.GM.CS_GM;
import protocol.GM.CS_GM.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetFragmentInit;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@MsgId(msgId = MsgIdEnum.SC_PetFragmentInit_VALUE)
public class PetFragmentInitHandler extends AbstractHandler<SC_PetFragmentInit> {
    @Override
    protected SC_PetFragmentInit parse(byte[] bytes) throws Exception {
        return SC_PetFragmentInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetFragmentInit scPetFragmentInit, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        SyncExecuteFunction.executeConsumer(robot, r -> {
            robot.getData().setPetFragmentList(scPetFragmentInit.getPetFragmentList());
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
        if (scPetFragmentInit.getPetFragmentList().stream().noneMatch(f -> f.getId().equals("90005") && f.getNumber() > 50)) {
            Builder builder = CS_GM.newBuilder();
            builder.setStr("petFragmentSpecify|90005|2000");
            robot.getClient().send(MsgIdEnum.CS_GM_VALUE, builder);
        }
    }
}
