package petrobot.system.battle.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.CS_BattleResult.Builder;
import protocol.Battle.SC_EnterFight;
import protocol.BattleMono.FightParamDict;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;

import java.util.Random;

/**
 * @author xiao_FL
 * @date 2019/12/23
 */
@MsgId(msgId = MsgIdEnum.SC_EnterFight_VALUE)
public class EnterFightHandler extends AbstractHandler<SC_EnterFight> {

    @Override
    protected SC_EnterFight parse(byte[] bytes) throws Exception {
        return SC_EnterFight.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_EnterFight result, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            if (result.getRetCode().getRetCode() == RetCodeEnum.RCE_Success || result.getSubType() == BattleSubTypeEnum.BSTE_MistForest) {
                robot.getData().setBattleId(result.getBattleId());
                try {
                    Thread.sleep(30 * 1000);
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
                if (result.getSubType() == BattleSubTypeEnum.BSTE_TheWar) {
                    robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                }
                Random random = new Random();
                Builder builder = CS_BattleResult.newBuilder();
                builder.setBattleId(result.getBattleId());
                builder.setWinnerCamp(random.nextInt(2) + 1);
                builder.setIsGMEnd(true);
                builder.addFightParams(FightParamDict.newBuilder().setKey(FightParamTypeEnum.FPTE_FightStar).setValue(3));
                robot.getClient().send(MsgIdEnum.CS_BattleResult_VALUE, builder);
            } else {
                LogUtil.error("robot enter fight error," + result.getSubType() + " : " + result.getRetCode());
                robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            }
        });
    }
}
