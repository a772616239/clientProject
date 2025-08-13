package server.handler;

import common.GlobalData;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerConst;
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
            LogUtil.info("recv server ping from remoteAddr={},serverIndex={}" , ip, serverIndex);
            ServerTypeEnum serverType = req.getFromServerType();
            if (serverType != ServerTypeEnum.STE_GameServer) {
                return;
            }
            GameServerTcpChannel chn = GlobalData.getInstance().getServerChannel(serverIndex);
            if (chn != null && chn.equals(gsChn)) {
                chn.send(MsgIdEnum.ServerPing_VALUE, req.toBuilder());
            } else {
                gsChn.close();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            gsChn.close();
        }
    }
}
