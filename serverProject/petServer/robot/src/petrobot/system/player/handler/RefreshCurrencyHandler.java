package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.Common.SC_RefreashCurrency;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_RefreashCurrency_VALUE)
public class RefreshCurrencyHandler extends AbstractHandler<SC_RefreashCurrency> {
    @Override
    protected SC_RefreashCurrency parse(byte[] bytes) throws Exception {
        return SC_RefreashCurrency.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreashCurrency result, int i) {
    }
}
