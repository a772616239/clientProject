package server.net.message.coder;

import com.bowlong.third.netty4.TcpInboundHandler;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.IMessagePool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import model.warpServer.WarpServerConst;
import model.warpServer.WarpServerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MessageDeCoder extends TcpInboundHandler {

    static final AttributeKey<GameServerTcpChannel> chnAttr = AttributeKey.valueOf("pet");
    static Log log = LogFactory.getLog(MessageDeCoder.class);
    static ExecutorService pool = Executors.newCachedThreadPool();
    private IMessagePool msgPool;

    public MessageDeCoder(IMessagePool pool) {
        msgPool = pool;
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        Channel nioSocketChannel = (Channel) ctx.channel();
        GameServerTcpChannel chn = new GameServerTcpChannel(nioSocketChannel, ctx);
        nioSocketChannel.attr(chnAttr).set(chn);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.error("channel inactive,addr:" + ctx.channel().remoteAddress());
        close(ctx);
    }

    public int getbodyLength(int headLengthValue) {
        return headLengthValue - GameServerTcpChannel.MSGID_SIZE - GameServerTcpChannel.CODENUM_SIZE;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf data = (ByteBuf) msg;
        final GameServerTcpChannel chn = ctx.channel().attr(chnAttr).get();
        try {
            final int l = getbodyLength(data.readInt());
            final short msgId = data.readShort();
            LogUtil.debug("recv msg id=" + msgId);
            final int codeNum = data.readInt();
            byte[] body = new byte[l];
            data.readBytes(body);
            final AbstractHandler<?> handler = msgPool.getHandler(msgId);
            handler.setMsgByte(body);
            chn.getAutoDrivenActionQueue().add(new Runnable() {
                @Override
                public void run() {
                    long startExecuteTime = System.currentTimeMillis();
                    handler.doAction(chn, codeNum);
                    long costTime = System.currentTimeMillis() - startExecuteTime;
                    if (costTime > 0) {
                        LogUtil.debug("execute msg id=" + msgId + ",costTime=" + costTime);
                    }
                    if (costTime > 100) {
                        LogUtil.error("execute msg id=" + msgId + " too slow,costTime=" + costTime);
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.printStackTrace(e);

        } finally {
            data.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.error("channel exception remote addr:" + ctx.channel().remoteAddress()
                + ",error msg:" + cause.getMessage());
        close(ctx);
    }

    void execute(final GameServerTcpChannel chn, short msgId, int codeNum, byte[] body) {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(ChannelHandlerContext ctx) {
        final GameServerTcpChannel chn = ctx.channel().attr(chnAttr).get();
        if (chn != null) {
            String ip = WarpServerConst.parseIp(chn.channel.remoteAddress().toString().substring(1));
            if (!StringHelper.isNull(ip)) {
                int serverIndex = StringHelper.stringToInt(chn.getPlayerId(), 0);
                if (WarpServerManager.getInstance().getGameServerChannel(serverIndex) != null) {
                    WarpServerManager.getInstance().removeGameServerChannel(serverIndex);
                    LogUtil.error("GameServer closed addr:{},serverIndex:{}" ,ip, serverIndex);
                } else if (WarpServerManager.getInstance().getCrossServerChannel(serverIndex) != null) {
                    WarpServerManager.getInstance().removeCrossServerChannel(serverIndex);
                    LogUtil.error("CrossServer closed addr:" + serverIndex);
                }
            }
            chn.close();
        }

    }

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                LogUtil.error("idle state,remote addr:" + ctx.channel().remoteAddress());
                close(ctx);
            }

        }
    }

}
