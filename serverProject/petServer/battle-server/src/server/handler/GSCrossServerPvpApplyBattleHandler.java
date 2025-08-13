package server.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerConst;
import model.warpServer.WarpServerManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_ReplyPvpBattle;
import protocol.ServerTransfer.GS_BS_ApplyPvpBattle;
import protocol.ServerTransfer.ReplyPvpBattleData;
import protocol.ServerTransfer.ServerTypeEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_BS_ApplyPvpBattle_VALUE)
public class GSCrossServerPvpApplyBattleHandler extends AbstractHandler<GS_BS_ApplyPvpBattle> {
    @Override
    protected GS_BS_ApplyPvpBattle parse(byte[] bytes) throws Exception {
        return GS_BS_ApplyPvpBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_ApplyPvpBattle req, int i) {
        try {
            String ip = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));
            int serverIndex = WarpServerManager.getInstance().getSeverIndexByIp(ip);
            BS_GS_ReplyPvpBattle.Builder builder = BS_GS_ReplyPvpBattle.newBuilder();
            ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(req.getApplyPvpBattleData(), ServerTypeEnum.STE_GameServer, serverIndex);
            builder.setReplyPvpBattleData(replyBuilder);
            gsChn.send(MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
