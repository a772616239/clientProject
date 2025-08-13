package client;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.IMessagePool;
import io.netty.channel.ChannelHandlerContext;
import model.warpServer.battleServer.BattleServerManager;
import server.net.message.coder.MessageDeCoder;
import util.LogUtil;

public class ClientMessageDeCoder extends MessageDeCoder {
    public ClientMessageDeCoder(IMessagePool pool) {
        super(pool);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.error("bs channel inactive,addr:" + ctx.channel().remoteAddress());
        close(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.error("bs channel exception remote addr:" + ctx.channel().remoteAddress()
                + ",error msg:" + cause.getMessage());
        close(ctx);
    }

    @Override
    public void close(ChannelHandlerContext ctx) {
        final GameServerTcpChannel chn = ctx.channel().attr(chnAttr).get();
        if (chn != null && chn.channel.isActive()) {
            String ipPort = chn.channel.remoteAddress().toString().substring(1);
            BattleServerManager.getInstance().onServerCloseByAddr(ipPort);
            BattleServerManager.getInstance().removeNettyChannelByAddr(ipPort);
            chn.close();

        }

    }
}
