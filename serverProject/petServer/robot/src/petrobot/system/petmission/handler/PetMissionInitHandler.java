package petrobot.system.petmission.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetMissionInit;
import protocol.RetCodeId.RetCodeEnum;

/**
 * @author xiao_FL
 * @date 2019/12/18
 */
@MsgId(msgId = MsgIdEnum.SC_PetMissionInit_VALUE)
public class PetMissionInitHandler extends AbstractHandler<SC_PetMissionInit> {
    @Override
    protected SC_PetMissionInit parse(byte[] bytes) throws Exception {
        return SC_PetMissionInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetMissionInit scPetMissionInit, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        if (scPetMissionInit.getResult().getRetCode() == RetCodeEnum.RCE_Success) {
            robot.getData().setPetMissionList(scPetMissionInit.getMissionList());
            robot.getData().setAcceptedPetMissionList(scPetMissionInit.getAcceptedMissionList());
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }
}
