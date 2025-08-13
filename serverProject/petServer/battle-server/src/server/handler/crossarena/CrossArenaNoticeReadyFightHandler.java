package server.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaReadyFight;
import protocol.ServerTransfer.GS_BS_CrossArenaReadyFight;
import protocol.ServerTransfer.ServerTypeEnum;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaReadyFight_VALUE)
public class CrossArenaNoticeReadyFightHandler extends AbstractHandler<GS_BS_CrossArenaReadyFight> {
    @Override
    protected GS_BS_CrossArenaReadyFight parse(byte[] bytes) throws Exception {
        return GS_BS_CrossArenaReadyFight.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaReadyFight req, int i) {
		String playerId = req.getPlayerId();
		long endtime = req.getEndtime();
		BS_GS_CrossArenaReadyFight.Builder builder = BS_GS_CrossArenaReadyFight.newBuilder();
		builder.setEndtime(endtime);
		builder.setPlayerId(playerId);
		builder.addAllState(req.getStateList());
		builder.setAtt(req.getAtt());
		int svrIndex = req.getSvrIndex();
		if (svrIndex <= 0) { // 兼容代码
			svrIndex = WarpServerManager.getInstance().getSeverIndexByIp(req.getFromIp());
		}
		WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, svrIndex, MsgIdEnum.BS_GS_CrossArenaReadyFight_VALUE, builder);
    }

}
