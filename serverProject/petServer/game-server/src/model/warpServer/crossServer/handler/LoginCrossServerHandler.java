package model.warpServer.crossServer.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.BaseNettyClient;
import model.warpServer.crossServer.CrossServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_LoginCrossServer;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_LoginCrossServer_VALUE)
public class LoginCrossServerHandler extends AbstractHandler<CS_GS_LoginCrossServer> {
    @Override
    protected CS_GS_LoginCrossServer parse(byte[] bytes) throws Exception {
        return CS_GS_LoginCrossServer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_LoginCrossServer req, int i) {
        String ipport = gsChn.channel.remoteAddress().toString().substring(1);
        LogUtil.info("recv login CrossServer:" + ipport + ", result=" + req.getResult());
        int serverIndex = CrossServerManager.getInstance().getServerIndexByCsAddr(ipport);
        if (serverIndex <= 0) {
            LogUtil.error("cs nettyClient not found serverIndex,addr=" + ipport);
            return;
        }
        BaseNettyClient nettyClient = CrossServerManager.getInstance().getActiveNettyClient(serverIndex);
        if (nettyClient != null) {
            if (req.getResult()) {
                LogUtil.info("login cs success,addr=" + ipport);
                nettyClient.setState(2);
            } else {
                LogUtil.error("login cs failed,addr=" + ipport);
                nettyClient.setState(0);
            }
        } else {
            LogUtil.error("cs nettyClient is null,addr=" + ipport);
        }
    }
}
