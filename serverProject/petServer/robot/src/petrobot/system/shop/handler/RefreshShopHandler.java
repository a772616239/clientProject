package petrobot.system.shop.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Shop.SC_RefreshShop;

@MsgId(msgId = MsgIdEnum.SC_RefreshShop_VALUE)
public class RefreshShopHandler extends AbstractHandler<SC_RefreshShop> {
    @Override
    protected SC_RefreshShop parse(byte[] bytes) throws Exception {
        return SC_RefreshShop.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreshShop result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        RetCodeEnum retCode = result.getRetcode().getRetCode();
        if (retCode != RetCodeEnum.RCE_Success) {
            LogUtil.error("refresh shop error, reason = " + retCode);
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.getData().addShopInfo(result.getShopInfo());
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
