package server.handler.crossarena;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaRankManager;
import model.crossarena.entity.playercrossarenaEntity;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaRank;
import protocol.CrossArena.SC_CrossArenaRank;
import protocol.MessageId.MsgIdEnum;

import static protocol.MessageId.MsgIdEnum.SC_CrossArenaRank_VALUE;

/**
 * 擂台赛排行榜
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaRank_VALUE)
public class ClaimCrossArenaRankHandler extends AbstractBaseHandler<CS_CrossArenaRank> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaRank.Builder resultBuilder = SC_CrossArenaRank.newBuilder();
        gsChn.send(SC_CrossArenaRank_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaRank parse(byte[] bytes) throws Exception {
        return CS_CrossArenaRank.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaRank req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_CrossArenaRank.Builder msg = SC_CrossArenaRank.newBuilder();
        msg.addAllRanks(CrossArenaRankManager.getInstance().findRank(req.getRank(),req.getAreaId()));
        playercrossarenaEntity pe = CrossArenaManager.getInstance().getPlayerEntity(playerIdx);
        msg.addAllNoteAward(pe.getDataMsg().getNoteAwardList());
        GlobalData.getInstance().sendMsg(playerIdx, SC_CrossArenaRank_VALUE, msg);
    }
}
