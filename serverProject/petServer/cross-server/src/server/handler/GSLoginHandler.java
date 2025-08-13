package server.handler;

import common.GlobalData;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerConst;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_LoginCrossServer;
import protocol.ServerTransfer.GS_CS_LoginCrossServer;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_LoginCrossServer_VALUE)
public class GSLoginHandler extends AbstractHandler<GS_CS_LoginCrossServer> {
    @Override
    protected GS_CS_LoginCrossServer parse(byte[] bytes) throws Exception {
        return GS_CS_LoginCrossServer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_LoginCrossServer req, int i) {
        try {
            String ip = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));
            LogUtil.info("recv login CS msg from " + ip + ",index=" + req.getServerIndex());
            GameServerTcpChannel chn = GlobalData.getInstance().getServerChannel(req.getServerIndex());
            CS_GS_LoginCrossServer.Builder builder = CS_GS_LoginCrossServer.newBuilder();
            if (chn != null && chn != gsChn) {
                chn.close();
            }
            gsChn.setPlayerId(StringHelper.IntTostring(req.getServerIndex(), "0"));
            GlobalData.getInstance().addServerChannel(req.getServerIndex(), gsChn);
            builder.setResult(true);
            gsChn.send(MsgIdEnum.CS_GS_LoginCrossServer_VALUE, builder);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            gsChn.close();
        }
    }
}
