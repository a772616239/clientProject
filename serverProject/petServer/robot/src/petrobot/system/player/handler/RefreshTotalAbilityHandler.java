package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_RefreshTotalAbility;

@MsgId(msgId = MsgIdEnum.SC_RefreshTotalAbility_VALUE)
public class RefreshTotalAbilityHandler extends AbstractHandler<SC_RefreshTotalAbility> {
    @Override
    protected SC_RefreshTotalAbility parse(byte[] bytes) throws Exception {
        return SC_RefreshTotalAbility.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_RefreshTotalAbility result, int i) {
    }
}
