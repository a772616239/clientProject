package server.net;

import common.load.ServerConfig;
import io.netty.channel.ChannelFuture;

import java.util.Map;
import com.bowlong.third.netty4.socket.N4BootstrapSocketServer;

import hyzNet.message.MessagePool;

public class GameServerBootstrap {

	@SuppressWarnings("static-access")
	public static boolean start(ServerConfig config, boolean nodelay,
								boolean alive) {

        Map map = N4BootstrapSocketServer.startSync(config.getIp(), config.getPort(), nodelay, alive,
				new GameServerInitializer(new MessagePool(config.getHandlerPath())));
        if (map == null || map.isEmpty()) {
			return false;
		}

		ChannelFuture chnFu = (ChannelFuture) map.get("chnFuture");
		if (chnFu == null) {
			return false;
		}
		return chnFu.isSuccess();
	}
}
