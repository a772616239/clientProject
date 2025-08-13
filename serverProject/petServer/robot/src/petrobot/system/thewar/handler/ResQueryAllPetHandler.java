package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWarDefine.SC_QueryWarPetData;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TheWarDefine.WarPetData;

@MsgId(msgId = MsgIdEnum.SC_QueryWarPetData_VALUE)
public class ResQueryAllPetHandler extends AbstractHandler<SC_QueryWarPetData> {
    @Override
    protected SC_QueryWarPetData parse(byte[] bytes) throws Exception {
        return SC_QueryWarPetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_QueryWarPetData ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            if (ret.getRetCode() == TheWarRetCode.TWRC_Success && r.getData().getRobotWarData() != null) {
                for (WarPetData warPet : ret.getPetListList()) {
                    r.getData().getRobotWarData().getPlayerData().putPlayerPets(warPet.getPetId(), warPet);
                }

                r.getData().getRobotWarData().getPlayerData().clearBanedPets();
                for (int j = 0; j < ret.getBanedIdxCount(); j++) {
                    String petIdx = ret.getBanedIdx(j);
                    long expireTime = ret.getBanExpireTime(j);
                    r.getData().getRobotWarData().getPlayerData().putBanedPets(petIdx, expireTime);
                }
            }
            r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
