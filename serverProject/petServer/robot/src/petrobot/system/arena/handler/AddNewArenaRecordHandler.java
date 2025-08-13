package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.Arena.SC_AddNewArenaRecord;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/05/28
 */
@MsgId(msgId = MsgIdEnum.SC_AddNewArenaRecord_VALUE)
public class AddNewArenaRecordHandler extends AbstractHandler<SC_AddNewArenaRecord> {
    @Override
    protected SC_AddNewArenaRecord parse(byte[] bytes) throws Exception {
        return SC_AddNewArenaRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AddNewArenaRecord req, int i) {

    }
}
