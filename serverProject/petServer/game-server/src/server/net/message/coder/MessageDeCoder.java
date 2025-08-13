package server.net.message.coder;

import com.bowlong.third.netty4.TcpInboundHandler;
import common.GameConst.EventType;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.IMessagePool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import model.FunctionManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import server.event.Event;
import server.event.EventManager;
import common.AbstractBaseHandler;
import util.GameUtil;
import util.LogUtil;


public class MessageDeCoder extends TcpInboundHandler {

    static protected final AttributeKey<GameServerTcpChannel> chnAttr = AttributeKey.valueOf("pet");
    //    static Log log = LogFactory.getLog(MessageDeCoder.class);
//    static ExecutorService pool = Executors.newCachedThreadPool();
    protected IMessagePool msgPool;

    public MessageDeCoder(IMessagePool pool) {
        msgPool = pool;
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        Channel nioSocketChannel = ctx.channel();
        GameServerTcpChannel chn = new GameServerTcpChannel(nioSocketChannel, ctx);
        nioSocketChannel.attr(chnAttr).set(chn);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.error("channel inactive,addr:" + ctx.channel().remoteAddress());
        close(ctx);
    }

    public int getbodyLength(int headLengthValue) {
        // 不包含包头的长度字段
        return headLengthValue - GameServerTcpChannel.MSGID_SIZE - GameServerTcpChannel.CODENUM_SIZE;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf data = (ByteBuf) msg;
        final GameServerTcpChannel chn = ctx.channel().attr(chnAttr).get();
        try {
            final int l = getbodyLength(data.readInt());
            final short msgId = data.readShort();
            final int codeNum = data.readInt();
            byte[] body = new byte[l];
            data.readBytes(body);
            final AbstractHandler<?> handler = msgPool.getHandler(msgId);
            MsgIdEnum msgIdEnum = MsgIdEnum.forNumber(msgId);
            if (handler == null) {
                LogUtil.error("MessageDeCoder.channelRead, msg [ " + msgIdEnum + "] handler is null");
                return;
            }
            if (!checkMsgValid(chn, msgIdEnum, handler)) {
                LogUtil.error("MessageDeCoder.channelRead, msg [ " + msgIdEnum + "] player is not valid");
                return;
            }
            handler.setMsgByte(body);
            chn.getAutoDrivenActionQueue().add(() -> {
                long startExecuteTime = System.currentTimeMillis();
                //是baseHandler且功能未开放
                if (handler instanceof AbstractBaseHandler
                        && FunctionManager.getInstance().functionClosed(((AbstractBaseHandler<?>) handler).belongFunction())) {
                    ((AbstractBaseHandler<?>) handler).doClosedActive(chn, codeNum);
                } else {
                    handler.doAction(chn, codeNum);
                }
                long costTime = System.currentTimeMillis() - startExecuteTime;
                if (costTime > 0) {
                    LogUtil.debug("execute msg =" + msgIdEnum + ",costTime=" + costTime);
                }
                if (costTime > 100) {
                    LogUtil.error("execute msg =" + msgIdEnum + " too slow,costTime=" + costTime);
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

    @Override
    public void close(ChannelHandlerContext ctx) {
        final GameServerTcpChannel chn = ctx.channel().attr(chnAttr).get();
        if (chn != null && chn.getPlayerId1() != 0) {
            playerEntity player = playerCache.getByIdx(String.valueOf(chn.getPlayerId1()));
            if (player != null) {
                Event idleEvent = Event.valueOf(EventType.ET_Logout, GameUtil.getDefaultEventSource(), player);
                EventManager.getInstance().dispatchEvent(idleEvent);
            }
            chn.setPlayerId("");
            chn.setPlayerId1(0);
            chn.close();
        }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                LogUtil.error("idle state,remote addr:" + ctx.channel().remoteAddress());
                close(ctx);
            }
        }
    }

    protected boolean isBindPlayerOnline(final GameServerTcpChannel chn) {
        playerEntity player = playerCache.getByIdx(GameUtil.longToString(chn.getPlayerId1(), ""));
        if (player == null) {
            return false;
        }
        GameServerTcpChannel playerChn = GlobalData.getInstance().getOnlinePlayerChannel(player.getIdx());
        return playerChn != null && playerChn.channel.isActive();
    }

    protected boolean checkMsgValid(final GameServerTcpChannel chn, MsgIdEnum msgIdEnum, final AbstractHandler<?> handler) {
        if (msgIdEnum == MsgIdEnum.CS_Login) {
            return true;
        }
//        if (msgIdEnum.getNumber() >= 20000) {
//            return true;
//        }
        if (handler instanceof AbstractBaseHandler) {
            AbstractBaseHandler baseHandler = (AbstractBaseHandler) handler;
            return baseHandler.checkValid(chn);
        }
        return false;
    }

}
