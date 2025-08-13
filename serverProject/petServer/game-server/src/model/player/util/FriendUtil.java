package model.player.util;

import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.GlobalData;
import java.util.Date;
import model.arena.ArenaManager;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import protocol.Friend.FriendBaseInfo;
import protocol.Friend.FriendInfo;
import protocol.Friend.SC_AddFriend;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_FriendInfo;
import protocol.PlayerDB.DB_PlayerData.Builder;
import util.LogUtil;

public class FriendUtil {

    /**
     * 返回好友baseInfo，不包括时间字段
     *
     * @param player
     * @return
     */
    public static FriendBaseInfo.Builder builderFriendBaseInfo(playerEntity player, long time) {
        if (player == null) {
            return null;
        }

        FriendBaseInfo.Builder builder = FriendBaseInfo.newBuilder();
        builder.setPlayerIdx(player.getIdx());
        builder.setPlayerLv(player.getLevel());
        builder.setAvatarId(player.getAvatar());
        builder.setPlayerName(player.getName());
        builder.setOnlineStatus(player.isOnline());
        builder.setVIPLv(player.getVip());
        builder.setFightingCapacity(petCache.getInstance().totalAbility(player.getIdx()));
        builder.setTime(Math.max(time, 0));
        builder.setUserId(player.getUserid());
        builder.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
        if (builder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            builder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(player.getIdx()));
        }
        builder.setTitleId(player.getTitleId());
        builder.setNewTitleId(player.getCurEquipNewTitleId());
        return builder;
    }


    /**
     * @param player  玩家
     * @param friend  玩家好友
     * @param curTime
     * @return
     */
    public static FriendInfo.Builder builderFriendInfo(playerEntity player, playerEntity friend, long curTime) {
        if (player == null || friend == null) {
            return null;
        }

        Builder db_data = player.getDb_data();
        if (db_data == null) {
            return null;
        }

        DB_FriendInfo friendInfo = db_data.getFriendInfo();
        FriendInfo.Builder respondFriend = FriendInfo.newBuilder();
        FriendBaseInfo.Builder builder = FriendUtil.builderFriendBaseInfo(friend, curTime);
        if (builder != null) {
            respondFriend.setBaseInfo(builder);
        }
        //能否发送友情点
        respondFriend.setSendPoint(!friendInfo.getSendFriendshipPointList().contains(friend.getIdx()));
        Boolean aBoolean = friendInfo.getRecvPointMap().get(friend.getIdx());
        respondFriend.setRecvPoint(aBoolean != null && aBoolean);
        respondFriend.setLastOnlineTime(getPlayerLastOnlineTime(friend.getIdx()));
        return respondFriend;
    }

    /**
     * 获取玩家上次在线时间
     *
     * @param playerIdx
     * @return
     */
    public static long getPlayerLastOnlineTime(String playerIdx) {
        if (playerIdx == null) {
            return 0;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("FriendUtil.getPlayerLastOnlineTime， playerIdx[" + playerIdx + "] entity is null");
            return 0;
        }

        Date logOutTime = player.getLogouttime();
        if (logOutTime == null) {
            return 0;
        }

        return logOutTime.getTime();
    }

    public static void sendAddFriendMsg(playerEntity player, playerEntity friend, long curTime) {
        SC_AddFriend.Builder addFriend = SC_AddFriend.newBuilder();
        FriendInfo.Builder builder = FriendUtil.builderFriendInfo(player, friend, curTime);
        if (builder != null) {
            addFriend.setFriend(builder);
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_AddFriend_VALUE, addFriend);
        }
    }

    public static int getFriendLimit(String playerIdx) {
        VIPConfigObject vipCfg = VIPConfig.getById(PlayerUtil.queryPlayerVipLv(playerIdx));
        return vipCfg == null ? 0 : vipCfg.getFriendlimit();
    }

    public static int getFriendItemGainLimit(String playerIdx) {
        VIPConfigObject vipCfg = VIPConfig.getById(PlayerUtil.queryPlayerVipLv(playerIdx));
        return vipCfg == null ? 0 : vipCfg.getFrienditemgainlimit();
    }
}
