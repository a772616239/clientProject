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
import protocol.TheWar.SC_UpdateWarFightTeam;
import protocol.TheWarDB.WarTeamData;
import protocol.TheWarDefine.WarTeamType;

@MsgId(msgId = MsgIdEnum.SC_UpdateWarFightTeam_VALUE)
public class UpdateWarPetFightTeamHandler extends AbstractHandler<SC_UpdateWarFightTeam> {
    @Override
    protected SC_UpdateWarFightTeam parse(byte[] bytes) throws Exception {
        return SC_UpdateWarFightTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateWarFightTeam ret, int i) {
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
            WarTeamData.Builder builder = WarTeamData.newBuilder();
            for (int j = 0; j < ret.getPetInfo().getPosCount(); j++) {
               builder.putPetData(ret.getPetInfo().getPos(j), ret.getPetInfo().getPetIdx(j));
            }
            r.getData().getRobotWarData().getPlayerData().getTeamDbDataBuilder().putTeamData(WarTeamType.WTT_AttackTeam_VALUE, builder.build());
        });
    }
}
