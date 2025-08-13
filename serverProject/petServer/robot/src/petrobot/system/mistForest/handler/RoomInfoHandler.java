package petrobot.system.mistForest.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.system.mistForest.RobotMistForest;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_MistForestRoomInfo;

@MsgId(msgId = MsgIdEnum.SC_MistForestRoomInfo_VALUE)
public class RoomInfoHandler extends AbstractHandler<SC_MistForestRoomInfo> {
    @Override
    protected SC_MistForestRoomInfo parse(byte[] bytes) throws Exception {
        return SC_MistForestRoomInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_MistForestRoomInfo ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            RobotMistForest robotMistForest = r.getData().getRobotMistForest();
            if (robotMistForest == null) {
                robotMistForest = new RobotMistForest(r);

            } else {
                robotMistForest.clear();
            }
            r.getData().setRobotMistForest(robotMistForest);
            robotMistForest.setRoomId(ret.getRoomInfo().getRoomId());
            robotMistForest.initMistMap(ret.getRoomInfo().getMapId());
            robotMistForest.addPlayerInfo(ret.getRoomInfo().getPlayerInfoListList());
            robotMistForest.addItemInfo(ret.getRoomInfo().getMistForestItemList());
            robotMistForest.setTeamInfo(ret.getRoomInfo().getTeamInfo());
            robotMistForest.addObjList(ret.getRoomInfo().getInitMetaDataList());
        });
    }
}
