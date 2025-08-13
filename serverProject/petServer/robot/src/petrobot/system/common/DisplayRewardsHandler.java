package petrobot.system.common;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.Common.SC_DisplayRewards;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_DisplayRewards_VALUE)
public class DisplayRewardsHandler extends AbstractHandler<SC_DisplayRewards> {
    @Override
    protected SC_DisplayRewards parse(byte[] bytes) throws Exception {
        return SC_DisplayRewards.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_DisplayRewards result, int i) {

    }
}
