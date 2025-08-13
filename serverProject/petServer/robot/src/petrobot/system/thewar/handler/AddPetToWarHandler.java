package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_AddNewPetToWar;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TheWarDefine.WarPetData;

@MsgId(msgId = MsgIdEnum.SC_AddNewPetToWar_VALUE)
public class AddPetToWarHandler extends AbstractHandler<SC_AddNewPetToWar> {
    @Override
    protected SC_AddNewPetToWar parse(byte[] bytes) throws Exception {
        return SC_AddNewPetToWar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AddNewPetToWar ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            if (ret.getRetCode() == TheWarRetCode.TWRC_Success && r.getData().getRobotWarData() != null) {
                for (String petIdx : ret.getRemovedPetsList()) {
                    r.getData().getRobotWarData().getPlayerData().removePlayerPets(petIdx);
                }
                for (WarPetData petData : ret.getNewPetsList()) {
                    r.getData().getRobotWarData().getPlayerData().putPlayerPets(petData.getPetId(), petData);
                }
            }
            r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
