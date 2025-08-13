package petrobot.robot.net;

import com.bowlong.third.netty4.TcpInboundHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.IMessagePool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.LogUtil;
import protocol.MessageId.MsgIdEnum;

public class MessageDeCoder extends TcpInboundHandler {

    static protected final AttributeKey<GameServerTcpChannel> chnAttr = AttributeKey.valueOf("robot");
    private IMessagePool msgPool;

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


    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.error("channel inactive,addr:" + ctx.channel().remoteAddress());
        close(ctx);
    }

    public int getbodyLength(int headLengthValue) {
        return headLengthValue - GameServerTcpChannel.MSGID_SIZE - GameServerTcpChannel.CODENUM_SIZE;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf data = (ByteBuf) msg;
        final GameServerTcpChannel chn = ctx.channel().attr(chnAttr).get();
        try {
            final int l = getbodyLength(data.readInt());
            final short msgId = data.readShort();
            MsgIdEnum msgIdEnum = MsgIdEnum.forNumber(msgId);
            if (msgIdEnum != MsgIdEnum.CS_Ping) {
                LogUtil.debug("recv msg ," + msgIdEnum);
            }
//            if (msgId != MsgIdEnum.CS_Login_VALUE && (chn != null || !isBindPlayerOnline(chn))) {
//                chn.send(MsgIdEnum.SC_KickOut_VALUE, GameUtil.buildRetCode(RetCodeEnum.RCE_PlayerOffline));
//                chn.close();
//                return;
//            }
            final int codeNum = data.readInt();
            byte[] body = new byte[l];
            data.readBytes(body);
            final AbstractHandler<?> handler = msgPool.getHandler(msgId);
            if (handler == null) {
                LogUtil.error("MessageDeCoder.channelRead, msgId [ " + msgIdEnum + "] handler is null");
                return;
            }
            handler.setMsgByte(body);
            chn.getAutoDrivenActionQueue().add(new Runnable() {
                @Override
                public void run() {
                    handler.doAction(chn, codeNum);
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
        if (chn != null && chn.getPlayerId1() != 0) {
            Robot robot = RobotManager.getInstance().getRobotByChannel(chn.channel);
            if (robot != null) {
                LogUtil.info("robot[" + robot.getLoginName() + "] offline");
                robot.setOnline(false);
            }
            chn.setPlayerId("");
            chn.setPlayerId1(0);
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

    protected boolean isBindPlayerOnline(final GameServerTcpChannel chn) {
//        playerEntity player = playerCache.getByIdx(GameUtil.longToString(chn.getPlayerId1(), ""));
//        if (player == null) {
//            return false;
//        }
//        GameServerTcpChannel playerChn = GlobalData.getInstance().getOnlinePlayerChannel(player.getIdx());
//        return playerChn != null && playerChn.channel.isActive();
        return false;
    }


}
