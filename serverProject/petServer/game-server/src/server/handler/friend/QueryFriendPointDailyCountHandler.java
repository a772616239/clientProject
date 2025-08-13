package server.handler.friend;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.Friend.CS_FriendPointDailyCount;
import protocol.Friend.SC_FriendPointDailyCount;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_FriendPointDailyCount_VALUE)
public class QueryFriendPointDailyCountHandler extends AbstractBaseHandler<CS_FriendPointDailyCount> {
    @Override
    protected CS_FriendPointDailyCount parse(byte[] bytes) throws Exception {
        return CS_FriendPointDailyCount.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_FriendPointDailyCount req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_FriendPointDailyCount.Builder builder = SC_FriendPointDailyCount.newBuilder();
        builder.setDailyGainCount(player.getDb_data().getFriendInfo().getTodayGainFriendItemCount());
        gsChn.send(MsgIdEnum.SC_FriendPointDailyCount_VALUE, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_FriendPointDailyCount_VALUE, SC_FriendPointDailyCount.newBuilder());
    }
}
