package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_GoldExchange;

@MsgId(msgId = MsgIdEnum.SC_GoldExchange_VALUE)
public class GoldExchangeHandler extends AbstractHandler<SC_GoldExchange> {
    @Override
    protected SC_GoldExchange parse(byte[] bytes) throws Exception {
        return SC_GoldExchange.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_GoldExchange result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            LogUtil.info("robot id = " + robotByChannel.getId() + "receive goldExchange result =" + result.getRetCode().getRetCode());
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        }
    }
}
