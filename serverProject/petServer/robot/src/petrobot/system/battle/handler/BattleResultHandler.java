package petrobot.system.battle.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Battle.SC_BattleResult;
import protocol.MessageId.MsgIdEnum;

/**
 * @author xiao_FL
 * @date 2019/12/23
 */
@MsgId(msgId = MsgIdEnum.SC_BattleResult_VALUE)
public class BattleResultHandler extends AbstractHandler<SC_BattleResult> {
    @Override
    protected SC_BattleResult parse(byte[] bytes) throws Exception {
        return SC_BattleResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_BattleResult result, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot != null) {
            SyncExecuteFunction.executeConsumer(robot, r -> {
                if (r.getIndex() != IndexConst.MIST_MistForestMove) {
                    r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                }
                r.getData().setBattleId(0);
            });
        }
    }
}
