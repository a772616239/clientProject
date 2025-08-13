package server.net;

import com.bowlong.third.netty4.socket.N4BootstrapSocketServer;
import common.load.ServerConfig;
import hyzNet.message.MessagePool;
import io.netty.channel.ChannelFuture;
import util.LogUtil;

import java.util.Map;

public class GameServerBootstrap {

	@SuppressWarnings("static-access")
	public static boolean start(ServerConfig config, boolean nodelay,
                                boolean alive) {

		try {
			N4BootstrapSocketServer server = new N4BootstrapSocketServer();
			Map map = server.startSync(config.getIp(), config.getPort(), nodelay, alive,
					new GameServerInitializer(new MessagePool(config.getHandlePath())));
			if (map == null || map.size() <= 0)
				return false;

			ChannelFuture chnFu = (ChannelFuture) map.get("chnFuture");
			if (chnFu == null)
				return false;
			return chnFu.isSuccess();
		} catch (Exception e) {
			LogUtil.printStackTrace(e);
			return false;
		}

	}


}
