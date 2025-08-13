package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.system.thewar.WarRoomCache;
import petrobot.system.thewar.room.WarRoom;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWarDefine.SC_UpdateWarPetData;
import protocol.TheWarDefine.WarPetData;

@MsgId(msgId = MsgIdEnum.SC_UpdateWarPetData_VALUE)
public class UpdateWarPetDataHandler extends AbstractHandler<SC_UpdateWarPetData> {
    @Override
    protected SC_UpdateWarPetData parse(byte[] bytes) throws Exception {
        return SC_UpdateWarPetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateWarPetData ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null || robot.getData().getRobotWarData() == null) {
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            LogUtil.error("Robot[" + robot.getId() + "] WarRoom is Null");
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r->{
            for (WarPetData petData : ret.getPetListList()) {
                r.getData().getRobotWarData().getPlayerData().putPlayerPets(petData.getPetId(), petData);
            }
        });
    }
}
