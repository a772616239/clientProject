package petrobot.system.other;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.Common.SC_Marquee;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/05/29
 */
@MsgId(msgId = MsgIdEnum.SC_Marquee_VALUE)
public class MarqueeHandler extends AbstractHandler<SC_Marquee> {
    @Override
    protected SC_Marquee parse(byte[] bytes) throws Exception {
        return SC_Marquee.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_Marquee sc_marquee, int i) {

    }
}
