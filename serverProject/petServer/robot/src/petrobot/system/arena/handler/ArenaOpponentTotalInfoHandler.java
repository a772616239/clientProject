package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Arena.SC_ClaimArenaOpponentTotalInfo;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/05/28
 */
@MsgId(msgId = MsgIdEnum.SC_ClaimArenaOpponentTotalInfo_VALUE)
public class ArenaOpponentTotalInfoHandler extends AbstractHandler<SC_ClaimArenaOpponentTotalInfo> {
    @Override
    protected SC_ClaimArenaOpponentTotalInfo parse(byte[] bytes) throws Exception {
        return SC_ClaimArenaOpponentTotalInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimArenaOpponentTotalInfo req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
//        if (req.getRetCode().getRetCode() == RetCodeEnum.RCE_Success) {
//            Builder builder = CS_EnterArenaBattle.newBuilder();
//            builder.setChallengeIdx(req.getTotalInfo().getOpponnentInfo().getSimpleInfo().getPlayerIdx());
//            robotByChannel.getClient().send(MsgIdEnum.CS_EnterArenaBattle_VALUE, builder);
//        } else {
//            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
//                robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
//            });
//        }
    }
}
