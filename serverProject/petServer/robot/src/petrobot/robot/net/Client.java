package petrobot.robot.net;

import com.google.protobuf.GeneratedMessageV3;
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
import lombok.Getter;
import petrobot.util.LogUtil;
import protocol.MessageId.MsgIdEnum;

import java.util.concurrent.TimeUnit;

public class Client{
	
	private String host;
	
	private int port;

	@Getter
	private Channel channel;

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void close() {
		if (channel != null) {
			channel.close();
		}
	}

	public void connect() {
		try {
			final EventLoopGroup group = new NioEventLoopGroup();

			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)  // 使用NioSocketChannel来作为连接用的channel类
					.handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							System.out.println("正在连接中...");
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS));
							pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 0));
							pipeline.addLast("encoder", new MessageEnCoder());
							pipeline.addLast("tcp_adapter", new MessageDeCoder(new ClientMessagePool("petrobot.system")));
						}
					});
			//发起异步连接请求，绑定连接端口和host信息
			final ChannelFuture future = b.connect(host, port).sync();

			future.addListener((ChannelFutureListener) arg0 -> {
				if (future.isSuccess()) {
					System.out.println("连接服务器成功");

				} else {
					System.out.println("连接服务器失败");
					future.cause().printStackTrace();
					group.shutdownGracefully(); //关闭线程组
				}
			});

			this.channel = future.channel();
		} catch (InterruptedException e) {
			LogUtil.printStackTrace(e);
			close();
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
		LogUtil.debug("send msg to server, msgId = " + MsgIdEnum.forNumber(msgId));

	}
}
