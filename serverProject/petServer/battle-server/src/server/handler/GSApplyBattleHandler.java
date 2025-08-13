package server.handler;

import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerConst;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_ReplyPvpBattle;
import protocol.ServerTransfer.GS_BS_ApplyPvpBattle;
import protocol.ServerTransfer.ReplyPvpBattleData;
import protocol.ServerTransfer.ServerTypeEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_BS_ApplyPvpBattle_VALUE)
public class GSApplyBattleHandler extends AbstractHandler<GS_BS_ApplyPvpBattle> {
    @Override
    protected GS_BS_ApplyPvpBattle parse(byte[] bytes) throws Exception {
        return GS_BS_ApplyPvpBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_ApplyPvpBattle req, int i) {
        try {
            LogUtil.debug("recv GS apply pvp battle msg");
            int serverIndex = StringHelper.stringToInt(gsChn.getPlayerId(), 0);
            if (serverIndex <= 0) {
                LogUtil.error("ApplyPvpBattle serverIndex={} error", serverIndex);
            }
            BS_GS_ReplyPvpBattle.Builder builder = BS_GS_ReplyPvpBattle.newBuilder();
            ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(req.getApplyPvpBattleData(), ServerTypeEnum.STE_GameServer, serverIndex);
            builder.setReplyPvpBattleData(replyBuilder);
            gsChn.send(MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
