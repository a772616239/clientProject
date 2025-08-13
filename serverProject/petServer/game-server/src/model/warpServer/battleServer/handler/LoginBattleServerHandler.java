package model.warpServer.battleServer.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_LoginBattleServer;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.BS_GS_LoginBattleServer_VALUE)
public class LoginBattleServerHandler extends AbstractHandler<BS_GS_LoginBattleServer> {
    @Override
    protected BS_GS_LoginBattleServer parse(byte[] bytes) throws Exception {
        return BS_GS_LoginBattleServer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_LoginBattleServer req, int i) {
        String ipPort = gsChn.channel.remoteAddress().toString().substring(1);
        LogUtil.info("recv login battleServer:" + ipPort + ", result=" + req.getResult());
        BaseNettyClient nettyClient = BattleServerManager.getInstance().getActiveNettyClientByIpPort(ipPort);
        if (nettyClient != null) {
            if (req.getResult()) {
                LogUtil.info("login BS success,addr=" + ipPort);
                nettyClient.setState(2);
            } else {
                LogUtil.error("login BS failed,addr=" + ipPort);
                nettyClient.setState(0);
            }
        } else {
            LogUtil.error("BS nettyClient is null,addr=" + ipPort);
        }
    }
}
