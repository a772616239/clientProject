package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer;
import protocol.ServerTransfer.BS_GS_CrossArenaNeedAI;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaNeedAI_VALUE)
public class CrossArenaBGNeedAIHandler extends AbstractHandler<BS_GS_CrossArenaNeedAI> {
	@Override
	protected ServerTransfer.BS_GS_CrossArenaNeedAI parse(byte[] bytes) throws Exception {
		return BS_GS_CrossArenaNeedAI.parseFrom(bytes);
	}

	@Override
	protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaNeedAI req, int i) {
//        CrossArenaManager.getInstance().needAIAddTable(req.getTableId());
		CrossArenaManager.getInstance().needAI(req.getTableId(), req.getPlayerId(), req.getWinNum(), req.getUseType(), req.getDifficult());
	}
}
