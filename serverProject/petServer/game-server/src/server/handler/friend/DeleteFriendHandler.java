package server.handler.friend;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import helper.StringUtils;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.Friend.CS_DeleteFriend;
import protocol.Friend.SC_DeleteFriend;
import protocol.Friend.SC_RemoveFriend;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_FriendInfo;
import protocol.PlayerDB.DB_PlayerData;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_DeleteFriend_VALUE)
public class DeleteFriendHandler extends AbstractBaseHandler<CS_DeleteFriend> {
    @Override
    protected CS_DeleteFriend parse(byte[] bytes) throws Exception {
        return CS_DeleteFriend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DeleteFriend req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        playerEntity player = playerCache.getByIdx(playerIdx);
        playerEntity deletePlayer = playerCache.getByIdx(req.getDeletePlayerIdx());

        SC_DeleteFriend.Builder resultBuilder = SC_DeleteFriend.newBuilder();
        if (player == null || deletePlayer == null) {
            LogUtil.error("DeleteFriendHandler, playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DeleteFriend_VALUE, resultBuilder);
            return;
        }

        removePlayer(player, deletePlayer.getIdx(), false);
        removePlayer(deletePlayer, player.getIdx(), true);

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_DeleteFriend_VALUE, resultBuilder);
    }

    private void removePlayer(playerEntity player, String deletePlayer, boolean sendRemoveMsg) {
        if (player == null || StringUtils.isEmpty(deletePlayer)) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_PlayerData.Builder builder = player.getDb_data();

            DB_FriendInfo.Builder friendInfoBuilder = builder.getFriendInfoBuilder();
            friendInfoBuilder.removeOwned(deletePlayer);
            friendInfoBuilder.putRecvPoint(deletePlayer, false);
        });

        if (sendRemoveMsg && GlobalData.getInstance().checkPlayerOnline(player.getIdx())) {
            SC_RemoveFriend.Builder removeBuilder = SC_RemoveFriend.newBuilder();
            removeBuilder.setRemovePlayerIdx(deletePlayer);
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_RemoveFriend_VALUE, removeBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_DeleteFriend_VALUE, SC_DeleteFriend.newBuilder().setRetCode(retCode));
    }
}
