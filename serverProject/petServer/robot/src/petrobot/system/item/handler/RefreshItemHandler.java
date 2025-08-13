package petrobot.system.item.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.Bag.ItemInfo;
import protocol.Bag.SC_RefreashItem;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_RefreashItem_VALUE)
public class RefreshItemHandler extends AbstractHandler<SC_RefreashItem> {
    @Override
    protected SC_RefreashItem parse(byte[] bytes) throws Exception {
        return SC_RefreashItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreashItem result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            List<ItemInfo> itemInfoList = result.getItemInfoList();
            for (ItemInfo itemInfo : itemInfoList) {
                robotByChannel.getData().putItem(itemInfo.getItemCfgId(), itemInfo.getNewItemCount());
            }
        });
    }
}
