package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_QueryWarTeam;
import protocol.TheWar.WarTeamInfo;
import protocol.TheWarDB.WarTeamData;
import protocol.TheWarDefine.TheWarRetCode;

@MsgId(msgId = MsgIdEnum.SC_QueryWarTeam_VALUE)
public class ResQueryWarTeamHandler extends AbstractHandler<SC_QueryWarTeam> {
    @Override
    protected SC_QueryWarTeam parse(byte[] bytes) throws Exception {
        return SC_QueryWarTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_QueryWarTeam ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            if (ret.getRetCode() == TheWarRetCode.TWRC_Success && r.getData().getRobotWarData() != null) {
                WarTeamData.Builder teamBuilder = WarTeamData.newBuilder();
                for (WarTeamInfo warTeam : ret.getTotalTeamInfoList()) {
                    teamBuilder.clear();
                    for (int j = 0; j < warTeam.getPetInfo().getPetIdxCount(); j++) {
                        teamBuilder.putPetData(warTeam.getPetInfo().getPos(j), warTeam.getPetInfo().getPetIdx(j));
                    }
                    for (int j = 0; j < warTeam.getSkillInfo().getSkillCount(); j++) {
                        teamBuilder.putSkillData(warTeam.getSkillInfo().getPos(j), warTeam.getSkillInfo().getSkill(j));
                    }
                    r.getData().getRobotWarData().getPlayerData().getTeamDbDataBuilder().putTeamData(warTeam.getTeamTypeValue(), teamBuilder.build());
                }
            }
            r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
