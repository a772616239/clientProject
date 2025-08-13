package server.handler;

import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerConst;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_CS_LoginBattleServer;
import protocol.ServerTransfer.CS_BS_LoginBattleServer;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_BS_LoginBattleServer_VALUE)
public class CSLoginHandler extends AbstractHandler<CS_BS_LoginBattleServer> {
    @Override
    protected CS_BS_LoginBattleServer parse(byte[] bytes) throws Exception {
        return CS_BS_LoginBattleServer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BS_LoginBattleServer req, int i) {
        try {
            String ip = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));
            LogUtil.info("recv CS login BS msg from " + ip + ",index=" + req.getServerIndex());
            GameServerTcpChannel chn = WarpServerManager.getInstance().getCrossServerChannel(req.getServerIndex());
            BS_CS_LoginBattleServer.Builder builder = BS_CS_LoginBattleServer.newBuilder();
            if (chn != null) {
                chn.close();
            }
            gsChn.setPlayerId(StringHelper.IntTostring(req.getServerIndex(), "0"));
            WarpServerManager.getInstance().addCrossServerChannel(req.getServerIndex(), gsChn);
            builder.setResult(true);
            gsChn.send(MsgIdEnum.BS_CS_LoginBattleServer_VALUE, builder);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            gsChn.close();
        }
    }
}
