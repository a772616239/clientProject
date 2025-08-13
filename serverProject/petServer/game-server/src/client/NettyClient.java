package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_PatrolInit;
import server.net.message.coder.MessageDeCoder;

import java.util.concurrent.TimeUnit;


public class NettyClient {

    private final String host;
    private final int port;
    private Channel channel;

    //连接服务端的端口号地址和端口号
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        final EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)  // 使用NioSocketChannel来作为连接用的channel类
                .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("正在连接中...");
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("idleStateHandler", new IdleStateHandler(120, 120, 360, TimeUnit.SECONDS));
                        pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 0));
                        pipeline.addLast("tcp_adapter", new MessageDeCoder(new ClientMessagePool("src/client")));
                    }
                });
        //发起异步连接请求，绑定连接端口和host信息
        final ChannelFuture future = b.connect(host, port).sync();

        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture arg0) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("连接服务器成功");

                } else {
                    System.out.println("连接服务器失败");
                    future.cause().printStackTrace();
                    group.shutdownGracefully(); //关闭线程组
                }
            }
        });

        this.channel = future.channel();
    }

    public Channel getChannel() {
        return channel;
    }

    public static void main(String[] args) throws Exception {
        NettyClient client = new NettyClient("192.168.0.120", 10001);
        //启动client服务
        client.start();

        Channel channel = client.getChannel();

//        CS_Login.Builder request= CS_Login.newBuilder();
//        request.setUserId("987654321");
//        request.setToken("pet_proj");
//        CS_PetBagInit.Builder request = CS_PetBagInit.newBuilder();
//        request.setSort(0);

        CS_PatrolInit.Builder request =CS_PatrolInit.newBuilder();
//        request.setStr("Pet");
        byte[] body = request.build().toByteArray();
        int length = 2 + 4 + body.length;
        ByteBuf buffer = channel.alloc().buffer(length);
        buffer.writeInt(length);
        buffer.writeShort(MsgIdEnum.CS_PatrolInit_VALUE);//msgid
        buffer.writeInt(-1);
        buffer.writeBytes(body);

        if (channel != null) {
            channel.writeAndFlush(buffer);
        }
    }
}
