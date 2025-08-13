package petrobot.robot.net;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgPackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import petrobot.util.LogUtil;


public class MessageEnCoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) throws Exception {
        MsgPackage msg = (MsgPackage) obj;
        ByteBuf buffer = ctx.alloc().buffer(msg.getLength() + GameServerTcpChannel.LENGTH_SIZE);
        try {
            buffer.writeInt(msg.getLength());
            buffer.writeShort(msg.getMsgId());
            buffer.writeInt(msg.getCodeNum());
            buffer.writeBytes(msg.getBody());
            out.writeBytes(buffer);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        } finally {
            buffer.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.error("出现错误");
    }
}
