package server.handler;

import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerConst;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.ServerPing;
import protocol.ServerTransfer.ServerTypeEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.ServerPing_VALUE)
public class ServerPingHandler extends AbstractHandler<ServerPing> {
    @Override
    protected ServerPing parse(byte[] bytes) throws Exception {
        return ServerPing.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, ServerPing req, int i) {
        try {
            String ip = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));
            int serverIndex = StringHelper.stringToInt(gsChn.getPlayerId(), 0);
            GameServerTcpChannel chn = null;
            ServerTypeEnum serverType = req.getFromServerType();
            if (serverType == ServerTypeEnum.STE_GameServer) {
                LogUtil.info("recv GS server ping from remoteAddr={},serverIndex={}", ip, serverIndex);
                chn = WarpServerManager.getInstance().getGameServerChannel(serverIndex);
            } else if (serverType == ServerTypeEnum.STE_CrossServer) {
                LogUtil.info("recv CS server ping from remoteAddr={},serverIndex={}", ip, serverIndex);
                chn = WarpServerManager.getInstance().getCrossServerChannel(serverIndex);
            }
            if (chn != null && chn.equals(gsChn)) {
                chn.send(MsgIdEnum.ServerPing_VALUE, req.toBuilder().setFromServerType(ServerTypeEnum.STE_BattleServer));
            } else {
                gsChn.close();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            gsChn.close();
        }
    }
}
