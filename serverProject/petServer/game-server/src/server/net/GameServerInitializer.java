package server.net;/**
 * Created by zql on 16/2/25.
 */

import hyzNet.message.IMessagePool;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import server.net.message.coder.MessageDeCoder;
import server.net.message.coder.MessageEnCoder;

import java.util.concurrent.TimeUnit;


public class GameServerInitializer extends ChannelInitializer<SocketChannel> {

    int nThreads = 8;
    int maxFrameLength = 1024 * 1024;//解码的帧的最大长度
    int lengthFieldOffset = 0;
    int lengthFieldLength = 4;
    int lengthAdjustment = 0;
    int initialBytesToStrip = 0;
    EventExecutorGroup eventExecutorGroup;
    private IMessagePool msgPool;

    public GameServerInitializer(IMessagePool msgPool) {
        this.eventExecutorGroup = new DefaultEventExecutorGroup(this.nThreads);
        this.msgPool = msgPool;
    }


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS));
        pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip));
        pipeline.addLast("encoder", new MessageEnCoder());
        pipeline.addLast("tcp_adapter", new MessageDeCoder(msgPool));
    }
}
