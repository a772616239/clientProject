package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.Arena.CS_ClaimArenaInfo;
import protocol.Arena.SC_ArenaDirectUp;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020.08.26
 */
@MsgId(msgId = MsgIdEnum.SC_ArenaDirectUp_VALUE)
public class ArenaDirectUpHandler extends AbstractHandler<SC_ArenaDirectUp> {

    @Override
    protected SC_ArenaDirectUp parse(byte[] bytes) throws Exception {
        return SC_ArenaDirectUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ArenaDirectUp req, int i) {
         gsChn.send(MsgIdEnum.CS_ClaimArenaInfo_VALUE, CS_ClaimArenaInfo.newBuilder());
    }
}
