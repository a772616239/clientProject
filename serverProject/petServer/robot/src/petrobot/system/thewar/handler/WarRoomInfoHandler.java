package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.system.thewar.RobotTheWarData;
import petrobot.system.thewar.WarRoomCache;
import petrobot.system.thewar.room.WarRoom;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_TheWarTotalInfo;
import protocol.TheWarDefine.TheWarGridData;

@MsgId(msgId = MsgIdEnum.SC_TheWarTotalInfo_VALUE)
public class WarRoomInfoHandler extends AbstractHandler<SC_TheWarTotalInfo> {
    @Override
    protected SC_TheWarTotalInfo parse(byte[] bytes) throws Exception {
        return SC_TheWarTotalInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_TheWarTotalInfo ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            if (r.getData().getRobotWarData() == null) {
                r.getData().setRobotWarData(new RobotTheWarData(robot));
            }
            r.getData().getRobotWarData().setWarRoomIdx(ret.getRoomIdx());
            r.getData().getRobotWarData().setCamp(ret.getPlayerData().getCamp());
            r.getData().getRobotWarData().setJobTile(ret.getPlayerData().getJobTileLevel());
            r.getData().getRobotWarData().getPlayerData().setLastSettleAfkTime(ret.getPlayerData().getLastSettleTime());
            r.getData().getRobotWarData().getPlayerData().setStamina(ret.getPlayerData().getStamia());
            r.getData().getRobotWarData().getPlayerData().addAllCollectionPos(ret.getCollectionPosList());
            for (TheWarGridData gridData : ret.getOwnedGridsList()) {
                r.getData().getRobotWarData().getPlayerData().addOwnedGridPos(gridData.getPos());
            }

        });

        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(ret.getRoomIdx());
        if (warRoom == null) {
            warRoom = WarRoomCache.getInstance().createWarRoom(ret.getRoomIdx(), ret.getMapName());
        }
        SyncExecuteFunction.executeConsumer(warRoom, room -> room.updateMembers(ret.getRoomMembersList()));
    }
}
