package model.warpServer;

import client.ClientMessageDeCoder;
import client.ClientMessagePool;
import com.google.protobuf.GeneratedMessageV3;
import common.load.ServerConfig;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgPackage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_BS_LoginBattleServer;
import protocol.ServerTransfer.ServerPing;
import protocol.ServerTransfer.ServerTypeEnum;
import server.net.message.coder.MessageEnCoder;
import util.LogUtil;

public class BaseNettyClient {
    public static final int LENGTH_SIZE = 4;
    public static final int MSGID_SIZE = 2;
    public static final int CODENUM_SIZE = 4;

    protected final String host;
    protected final int port;
    private Channel channel;
    private Bootstrap bootstrap;

    private volatile int state;
    private long lastStateUpdateTime;
    private long lastPingTime;

    //连接服务端的端口号地址和端口号
    public BaseNettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean init() {
        try {
            final EventLoopGroup group = new NioEventLoopGroup();
            this.bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)  // 使用NioSocketChannel来作为连接用的channel类
                    .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            LogUtil.info("正在连接中...");
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("idleStateHandler", new IdleStateHandler(30, 30, 30, TimeUnit.SECONDS));
                            pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 0));
                            pipeline.addLast("encoder", new MessageEnCoder());
                            pipeline.addLast("tcp_adapter", new ClientMessageDeCoder(new ClientMessagePool(this.getClass().getPackage().getName())));
                        }
                    });

            final ChannelFuture future = connect();
            this.channel = future.channel();
            return future.isSuccess();
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    public ChannelFuture connect() throws Exception {
        final ChannelFuture future = bootstrap.connect(host, port).sync();
        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture arg0) throws Exception {
                if (future.isSuccess()) {
                    LogUtil.info("连接服务器成功:" + host + ":" + port);
                } else {
                    LogUtil.info("连接服务器失败:" + host + ":" + port);
                    future.cause().printStackTrace();
                    bootstrap.group().shutdownGracefully(); //关闭线程组
                }
            }
        });
        return future;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getIpPort() {
        return host + ":" + port;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    public void send(int msgId, GeneratedMessageV3.Builder<?> builder) {
        send(msgId, -1, builder);
    }

    public void send(int msgId, int codeNum, GeneratedMessageV3.Builder<?> builder) {
        byte[] body = builder.build().toByteArray();
        int length = GameServerTcpChannel.MSGID_SIZE + GameServerTcpChannel.CODENUM_SIZE + body.length;
        MsgPackage msg = new MsgPackage();
        msg.setBody(body);
        msg.setMsgId(msgId);
        msg.setCodeNum(codeNum);
        msg.setLength(length);
        if (!this.channel.isWritable()) {
            LogUtil.error("Channel not isWritable() >>>>,remoteAddr=" + host + ":" + port);
        } else if (this.channel == null) {
            LogUtil.error("channel is null,remoteAddr=" + host + ":" + port);
        } else {
            this.channel.writeAndFlush(msg);
        }
    }

    private void sendPingToServer() {
        ServerPing.Builder builder = ServerPing.newBuilder();
        builder.setFromServerType(ServerTypeEnum.STE_CrossServer);
        send(MsgIdEnum.ServerPing_VALUE, builder);
    }

    public void sendLoginMsgToServer() {
        int serverIdx = ServerConfig.getInstance().getServer();
        send(MsgIdEnum.CS_BS_LoginBattleServer_VALUE, CS_BS_LoginBattleServer.newBuilder().setServerIndex(serverIdx));
    }

    public void onTick(long curTime) {
        if (state == -1) { // 关闭状态
            close();
        } else if (state == 0) { // 初始状态
            try {
                if (lastStateUpdateTime + 10000L <= curTime) {
                   if (!channel.isActive()) {
                        final ChannelFuture future = connect();
                        this.channel = future.channel();
                        if (future.isSuccess()) {
                            sendLoginMsgToServer();
                            setState(1);
                            lastStateUpdateTime = curTime;
                        }
                    } else {
                        sendLoginMsgToServer();
                        setState(1);
                        lastStateUpdateTime = curTime;
                    }
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
                setState(0);
            }
        } else if (state == 1) { // 等待连接状态
            if (lastStateUpdateTime + 5000L <= curTime) {
                setState(0);
                lastStateUpdateTime = curTime;
            }
        } else if (state == 2) { // 连接状态
            if (!channel.isActive()) {
                setState(0);
            } else if (lastPingTime + 10000 < curTime) {
                sendPingToServer();
                lastPingTime = curTime;
            }
        }
    }
}
