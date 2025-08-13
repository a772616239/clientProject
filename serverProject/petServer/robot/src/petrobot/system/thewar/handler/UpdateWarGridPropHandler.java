package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.system.thewar.WarRoomCache;
import petrobot.system.thewar.map.grid.WarMapGrid;
import petrobot.system.thewar.room.WarRoom;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.SC_UpdateWarGridProp;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarGridData;

@MsgId(msgId = MsgIdEnum.SC_UpdateWarGridProp_VALUE)
public class UpdateWarGridPropHandler extends AbstractHandler<SC_UpdateWarGridProp> {
    @Override
    protected SC_UpdateWarGridProp parse(byte[] bytes) throws Exception {
        return SC_UpdateWarGridProp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateWarGridProp ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        if (robot.getData().getRobotWarData() == null) {
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            LogUtil.error("Robot[" + robot.getId() + "] WarRoom is Null");
            return;
        }
        List<Position> addPosList = new ArrayList<>();
        List<Position> removePosList = new ArrayList<>();
        SyncExecuteFunction.executeConsumer(warRoom, room->{
            WarMapGrid grid;
            for (TheWarGridData gridData : ret.getWarGridsInfoList()) {
                grid = room.getWarMap().getGridMap().get(gridData.getPos());
                if (grid == null) {
                    return;
                }
                for (int j = 0; j < gridData.getProps().getKeysCount(); j++) {
                    int propType = gridData.getProps().getKeysValue(j);
                    Long propVal = gridData.getProps().getValues(j);
                    grid.setPropValue(propType, propVal);

                    if (propType == TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) {
                        if (propVal.toString().equals(robot.getData().getBaseInfo().getPlayerId())) {
                            if (!robot.getData().getRobotWarData().getPlayerData().getOwnedGridPosList().contains(grid.getPos())) {
                                addPosList.add(grid.getPos());
                            }
                        } else {
                            if (robot.getData().getRobotWarData().getPlayerData().getOwnedGridPosList().contains(grid.getPos())) {
                                removePosList.add(grid.getPos());
                            }
                        }
                    }
                }
            }
        });

        if (!addPosList.isEmpty() || !removePosList.isEmpty()) {
            SyncExecuteFunction.executeConsumer(robot, r->{
                r.getData().getRobotWarData().getPlayerData().getOwnedGridPosBuilderList().removeAll(removePosList);
                r.getData().getRobotWarData().getPlayerData().addAllOwnedGridPos(addPosList);
            });
        }

    }
}
