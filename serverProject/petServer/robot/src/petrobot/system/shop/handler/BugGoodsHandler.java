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
import protocol.Shop.SC_BuyGoods;

@MsgId(msgId = MsgIdEnum.SC_BuyGoods_VALUE)
public class BugGoodsHandler extends AbstractHandler<SC_BuyGoods> {
    @Override
    protected SC_BuyGoods parse(byte[] bytes) throws Exception {
        return SC_BuyGoods.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_BuyGoods result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        RetCodeEnum retCode = result.getRetCode().getRetCode();
        if (retCode != RetCodeEnum.RCE_Success) {
            LogUtil.error("refresh shop error, reason = " + retCode);
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
