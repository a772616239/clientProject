package client;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.IMessagePool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.warpServer.battleServer.BattleServerManager;
import model.warpServer.crossServer.CrossServerManager;
import protocol.MessageId.MsgIdEnum;
import server.net.message.coder.MessageDeCoder;
import util.LogUtil;

public class ClientMessageDeCoder extends MessageDeCoder {
    private final boolean battleServerFlag;
    public ClientMessageDeCoder(IMessagePool pool, boolean battleServerFlag) {
        super(pool);
        this.battleServerFlag = battleServerFlag;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.error("trans channel inactive,addr:" + ctx.channel().remoteAddress());
        close(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.error("trans channel exception remote addr:" + ctx.channel().remoteAddress()
                + ",error msg:" + cause.getMessage());
        close(ctx);
    }

    @Override
    public void close(ChannelHandlerContext ctx) {
        final GameServerTcpChannel chn = ctx.channel().attr(chnAttr).get();
        if (chn != null && chn.channel.isActive()) {
            String ipPort = chn.channel.remoteAddress().toString().substring(1);
            if (battleServerFlag) {
                BattleServerManager.getInstance().onServerCloseByAddr(ipPort);
                BattleServerManager.getInstance().removeNettyChannelByAddr(ipPort);
            } else {
                CrossServerManager.getInstance().onServerCloseByAddr(ipPort);
                CrossServerManager.getInstance().removeNettyChannelByAddr(ipPort);
            }
            chn.close();
        }

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
                LogUtil.error("ClientMessageDeCoder.channelRead, msg [ " + msgIdEnum + "] handler is null");
                return;
            }
            if (!checkMsgValid(chn, msgIdEnum, handler)) {
                LogUtil.error("ClientMessageDeCoder.channelRead, msg [ " + msgIdEnum + "] msgId not valid");
                return;
            }
            handler.setMsgByte(body);
            chn.getAutoDrivenActionQueue().add(() -> {
                long startExecuteTime = System.currentTimeMillis();
                handler.doAction(chn, codeNum);
                long costTime = System.currentTimeMillis() - startExecuteTime;
                if (costTime > 0) {
                    LogUtil.debug("ClientMessageDeCoder execute msg =" + msgIdEnum + ",costTime=" + costTime);
                }
                if (costTime > 100) {
                    LogUtil.error("ClientMessageDeCoder execute msg =" + msgIdEnum + " too slow,costTime=" + costTime);
                }
            });
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        } finally {
            data.release();
        }
    }

    @Override
    protected boolean checkMsgValid(final GameServerTcpChannel chn, MsgIdEnum msgIdEnum, final AbstractHandler<?> handler) {
        return msgIdEnum.getNumber() > 20000;
    }
}
