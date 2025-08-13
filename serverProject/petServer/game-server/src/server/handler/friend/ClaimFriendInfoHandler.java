package server.handler.friend;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Map;
import java.util.Map.Entry;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.FriendUtil;
import protocol.Common.EnumFunction;
import protocol.Friend.CS_ClaimFriendInfo;
import protocol.Friend.FriendBaseInfo;
import protocol.Friend.FriendInfo.Builder;
import protocol.Friend.SC_ClaimFriendInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_ApplyFriendInfo;
import protocol.PlayerDB.DB_FriendInfo;
import protocol.PlayerDB.DB_OwnedFriendInfo;
import protocol.PlayerDB.DB_PlayerData;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimFriendInfo_VALUE)
public class ClaimFriendInfoHandler extends AbstractBaseHandler<CS_ClaimFriendInfo> {
    @Override
    protected CS_ClaimFriendInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimFriendInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimFriendInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimFriendInfo.Builder resultBuilder = SC_ClaimFriendInfo.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("ClaimFriendInfoHandler, playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.CS_ClaimFriendInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            long startTime = System.currentTimeMillis();
            DB_PlayerData.Builder dbPlayerData = player.getDb_data();
            if (dbPlayerData == null) {
                LogUtil.error("ClaimFriendInfoHandler, playerIdx[" + playerIdx + "] DBData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.CS_ClaimFriendInfo_VALUE, resultBuilder);
                return;
            }

            DB_FriendInfo friendInfo = dbPlayerData.getFriendInfo();

            //好友列表
            Map<String, DB_OwnedFriendInfo> ownedMap = friendInfo.getOwnedMap();
            if (ownedMap != null && ownedMap.size() > 0) {
                for (Entry<String, DB_OwnedFriendInfo> friend : ownedMap.entrySet()) {
                    Builder builder = FriendUtil.builderFriendInfo(player, playerCache.getByIdx(friend.getKey()), friend.getValue().getAddTime());
                    if (builder != null) {
                        resultBuilder.addOwned(builder);
                    }
                }
            }

            //申请列表
            Map<String, DB_ApplyFriendInfo> applyMap = friendInfo.getApplyMap();
            if (applyMap != null && applyMap.size() > 0) {
                for (Entry<String, DB_ApplyFriendInfo> apply : applyMap.entrySet()) {
                    FriendBaseInfo.Builder friendBaseInfo =
                            FriendUtil.builderFriendBaseInfo(playerCache.getByIdx(apply.getKey()), apply.getValue().getApplyTime());
                    if (friendBaseInfo != null) {
                        resultBuilder.addApply(friendBaseInfo);
                    }
                }
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimFriendInfo_VALUE, resultBuilder);
            LogUtil.debug("===========================ClaimFriend finished send time = " + (System.currentTimeMillis() - startTime));
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimFriendInfo_VALUE, SC_ClaimFriendInfo.newBuilder().setRetCode(retCode));
    }
}
