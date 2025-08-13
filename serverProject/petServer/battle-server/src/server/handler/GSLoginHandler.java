package server.handler;

import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerConst;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_LoginBattleServer;
import protocol.ServerTransfer.GS_BS_LoginBattleServer;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_BS_LoginBattleServer_VALUE)
public class GSLoginHandler extends AbstractHandler<GS_BS_LoginBattleServer> {
    @Override
    protected GS_BS_LoginBattleServer parse(byte[] bytes) throws Exception {
        return GS_BS_LoginBattleServer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_LoginBattleServer req, int i) {
        try {
            String ip = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));
            LogUtil.info("recv GS login BS msg from " + ip + ",index=" + req.getServerIndex());
            GameServerTcpChannel chn = WarpServerManager.getInstance().getGameServerChannel(req.getServerIndex());
            BS_GS_LoginBattleServer.Builder builder = BS_GS_LoginBattleServer.newBuilder();
            if (chn != null) {
                chn.close();
            }
            gsChn.setPlayerId(StringHelper.IntTostring(req.getServerIndex(), "0"));
            WarpServerManager.getInstance().addGameServerChannel(req.getServerIndex(), gsChn);
            builder.setResult(true);
            gsChn.send(MsgIdEnum.BS_GS_LoginBattleServer_VALUE, builder);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            gsChn.close();
        }
    }
}