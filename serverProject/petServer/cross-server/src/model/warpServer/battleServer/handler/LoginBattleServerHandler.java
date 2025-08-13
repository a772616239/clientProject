package model.warpServer.battleServer.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_CS_LoginBattleServer;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.BS_CS_LoginBattleServer_VALUE)
public class LoginBattleServerHandler extends AbstractHandler<BS_CS_LoginBattleServer> {
    @Override
    protected BS_CS_LoginBattleServer parse(byte[] bytes) throws Exception {
        return BS_CS_LoginBattleServer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_CS_LoginBattleServer req, int i) {
        String ipport = gsChn.channel.remoteAddress().toString().substring(1);
        LogUtil.info("recv login BattleServer:" + ipport + ", result=" + req.getResult());
        BaseNettyClient nettyClient = BattleServerManager.getInstance().getActiveNettyClientByAddr(ipport);
        if (nettyClient != null) {
            if (req.getResult()) {
                LogUtil.info("login bs success,addr=" + ipport);
                nettyClient.setState(2);
            } else {
                LogUtil.error("login bs failed,addr=" + ipport);
                nettyClient.setState(0);
            }
        } else {
            LogUtil.error("bs nettyClient is null,addr=" + ipport);
        }
    }
}
