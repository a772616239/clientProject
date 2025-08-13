package server.handler;

import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerConst;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_CS_ReplyPvpBattle;
import protocol.ServerTransfer.CS_BS_ApplyPvpBattle;
import protocol.ServerTransfer.ReplyPvpBattleData;
import protocol.ServerTransfer.ServerTypeEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_BS_ApplyPvpBattle_VALUE)
public class CSApplyBattleHandler extends AbstractHandler<CS_BS_ApplyPvpBattle> {
    @Override
    protected CS_BS_ApplyPvpBattle parse(byte[] bytes) throws Exception {
        return CS_BS_ApplyPvpBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BS_ApplyPvpBattle req, int i) {
        try {
            LogUtil.debug("recv CS apply pvp battle msg");
            int serverIndex = StringHelper.stringToInt(gsChn.getPlayerId(), 0);
            BS_CS_ReplyPvpBattle.Builder builder = BS_CS_ReplyPvpBattle.newBuilder();
            ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(req.getApplyPvpBattleData(), ServerTypeEnum.STE_CrossServer, serverIndex);
            builder.setReplyPvpBattleData(replyBuilder);
            gsChn.send(MsgIdEnum.BS_CS_ReplyPvpBattle_VALUE, builder);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
