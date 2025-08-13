package petrobot.system.friend.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.Friend.SC_UpdateFriendState;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_UpdateFriendState_VALUE)
public class UpdateFriendStateHandler extends AbstractHandler<SC_UpdateFriendState> {
    @Override
    protected SC_UpdateFriendState parse(byte[] bytes) throws Exception {
        return SC_UpdateFriendState.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UpdateFriendState result, int i) {

    }
}
