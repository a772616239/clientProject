package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.system.thewar.WarRoomCache;
import petrobot.system.thewar.room.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_KickOutFromTheWar;

@MsgId(msgId = MsgIdEnum.SC_KickOutFromTheWar_VALUE)
public class KickOutFromWarHandler extends AbstractHandler<SC_KickOutFromTheWar> {
    @Override
    protected SC_KickOutFromTheWar parse(byte[] bytes) throws Exception {
        return SC_KickOutFromTheWar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_KickOutFromTheWar ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null || robot.getData().getRobotWarData() == null) {
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom != null) {
            warRoom.clear();
            WarRoomCache.getInstance().removeWarRoom(warRoom.getRoomIdx());
        }
    }
}
