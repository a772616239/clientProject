package petrobot.system.shop.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.Shop.SC_ClaimShopInfo;

@MsgId(msgId = MsgIdEnum.SC_ClaimShopInfo_VALUE)
public class ClaimShopInfoHandler extends AbstractHandler<SC_ClaimShopInfo> {
    @Override
    protected SC_ClaimShopInfo parse(byte[] bytes) throws Exception {
        return SC_ClaimShopInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimShopInfo result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.getData().addShopInfo(result.getShopInfo());
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
