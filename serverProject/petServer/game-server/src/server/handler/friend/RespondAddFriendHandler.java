package server.handler.friend;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import helper.StringUtils;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.FriendUtil;
import protocol.Common.EnumFunction;
import protocol.Friend.CS_RespondAddFriend;
import protocol.Friend.FriendInfo;
import protocol.Friend.SC_RespondAddFriend;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_ApplyFriendInfo;
import protocol.PlayerDB.DB_FriendInfo;
import protocol.PlayerDB.DB_OwnedFriendInfo;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_RespondAddFriend_VALUE)
public class RespondAddFriendHandler extends AbstractBaseHandler<CS_RespondAddFriend> {
    @Override
    protected CS_RespondAddFriend parse(byte[] bytes) throws Exception {
        return CS_RespondAddFriend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RespondAddFriend req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_RespondAddFriend.Builder resultBuilder = SC_RespondAddFriend.newBuilder();
        if (req.getRespondPlayerIdxCount() <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_RespondAddFriend_VALUE, resultBuilder);
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_RespondAddFriend_VALUE, resultBuilder);
            return;
        }

        //按照申请时间排序
        List<String> targetList = new ArrayList<>(req.getRespondPlayerIdxList());
        if (targetList.size() > 1) {
            targetList.sort(Comparator.comparingLong(e -> {
                DB_ApplyFriendInfo applyFriendInfo = player.getDb_data().getFriendInfo().getApplyMap().get(e);
                return applyFriendInfo == null ? 0 : applyFriendInfo.getApplyTime();
            }));
        }

        RetCodeEnum retCode = RetCodeEnum.RCE_Success;
        long curTime = GlobalTick.getInstance().getCurrentTime();
        for (String targetPlayerIdx : targetList) {
            if (!req.getAgree()) {
                removeApply(player, targetPlayerIdx);
                continue;
            }

            if (!canAddPlayer(player)) {
                retCode = RetCodeEnum.RCE_Friend_FriendUpperLimit;
                break;
            }

            playerEntity targetPlayer = playerCache.getByIdx(targetPlayerIdx);
            if (!canAddPlayer(targetPlayer)) {
                retCode = RetCodeEnum.RCE_Friend_TargetFriendUpperLimit;
                continue;
            }

            if (removeApply(player, targetPlayerIdx)) {
                toBeFriend(player, targetPlayer);

                FriendInfo.Builder builder = FriendUtil.builderFriendInfo(player, targetPlayer, curTime);
                if (builder != null) {
                    resultBuilder.addAddFriend(builder);
                }
            }
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_RespondAddFriend_VALUE, resultBuilder);
    }

    private void toBeFriend(playerEntity player, playerEntity target) {
        if (player == null || target == null) {
            return;
        }
        addFriend(player, target, false);
        addFriend(target, player, true);
    }

    private void addFriend(playerEntity player, playerEntity targetPlayer, boolean sendAddFriendMsg) {
        if (player == null || targetPlayer == null) {
            return;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_FriendInfo.Builder friendInfo = player.getDb_data().getFriendInfoBuilder();
            if (!friendInfo.containsOwned(targetPlayer.getIdx())) {
                DB_OwnedFriendInfo.Builder newFriend = DB_OwnedFriendInfo.newBuilder();
                newFriend.setAddTime(currentTime);
                friendInfo.putOwned(targetPlayer.getIdx(), newFriend.build());
            }

            //目标：拥有好友个数
            EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_PlayerFriendReach, friendInfo.getOwnedCount(), 0);
        });

        if (sendAddFriendMsg && GlobalData.getInstance().checkPlayerOnline(player.getIdx())) {
            FriendUtil.sendAddFriendMsg(player, targetPlayer, currentTime);
        }
    }

    private boolean removeApply(playerEntity player, String targetIdx) {
        if (player == null || StringUtils.isEmpty(targetIdx)) {
            return false;
        }
        return SyncExecuteFunction.executePredicate(player, entity -> {
            DB_FriendInfo.Builder friendInfoBuilder = player.getDb_data().getFriendInfoBuilder();
            if (friendInfoBuilder.getApplyMap().containsKey(targetIdx)) {
                friendInfoBuilder.removeApply(targetIdx);
                return true;
            }
            return false;
        });
    }

    private boolean canAddPlayer(playerEntity player) {
        if (player == null) {
            return false;
        }
        return SyncExecuteFunction.executeFunction(player, entity -> {
            Builder db_data = player.getDb_data();
            if (db_data == null) {
                return false;
            }
            if (db_data.getFriendInfo().getOwnedCount() >= FriendUtil.getFriendLimit(player.getIdx())) {
                LogUtil.info("RespondAddFriendHandler.canAddPlayer, playerIdx:" + player.getIdx() + ", friend list full");
                return false;
            }
            return true;
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_RespondAddFriend_VALUE, SC_RespondAddFriend.newBuilder().setRetCode(retCode));
    }
}
