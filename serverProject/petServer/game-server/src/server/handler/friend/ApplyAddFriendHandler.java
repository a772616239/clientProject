package server.handler.friend;

import cfg.GameConfig;
import com.google.protobuf.ProtocolStringList;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.FriendUtil;
import protocol.Common.EnumFunction;
import protocol.Friend.CS_ApplyAddFriend;
import protocol.Friend.FriendBaseInfo;
import protocol.Friend.SC_AddFriendApply;
import protocol.Friend.SC_ApplyAddFriend;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB;
import protocol.PlayerDB.DB_FriendInfo;
import protocol.PlayerDB.DB_PlayerData;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ApplyAddFriend_VALUE)
public class ApplyAddFriendHandler extends AbstractBaseHandler<CS_ApplyAddFriend> {
    @Override
    protected CS_ApplyAddFriend parse(byte[] bytes) throws Exception {
        return CS_ApplyAddFriend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ApplyAddFriend req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ApplyAddFriend.Builder resultBuilder = SC_ApplyAddFriend.newBuilder();
        if (req.getTargetPlayerIdxCount() <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ApplyAddFriend_VALUE, resultBuilder);
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ApplyAddFriend_VALUE, resultBuilder);
            return;
        }
        ProtocolStringList targetPlayerIdxList = req.getTargetPlayerIdxList();

        List<String> allowIdx = new ArrayList<>();
        RetCodeEnum retCodeEnum = SyncExecuteFunction.executeFunction(player, entity -> {
            DB_PlayerData.Builder playerDbData = player.getDb_data();
            if (playerDbData == null) {
                return RetCodeEnum.RCE_UnknownError;
            }
            DB_FriendInfo friendData = playerDbData.getFriendInfo();

            //玩家好友达上限
            if (friendData.getOwnedCount() >= FriendUtil.getFriendLimit(playerIdx)) {
                return RetCodeEnum.RCE_Friend_FriendUpperLimit;
            }

            //剔除已经是好友的Idx,已经发送申请的玩家
            for (String targetIdx : targetPlayerIdxList) {
                if (playerIdx.equals(targetIdx) || friendData.getOwnedMap().containsKey(targetIdx)
                        || friendData.getApplyMap().containsKey(targetIdx)) {
                    continue;
                }
                allowIdx.add(targetIdx);
            }

            return RetCodeEnum.RCE_Success;
        });

        if (retCodeEnum != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ApplyAddFriend_VALUE, resultBuilder);
            return;
        }

        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (!allowIdx.isEmpty()) {
            SC_AddFriendApply.Builder addApply = SC_AddFriendApply.newBuilder();
            FriendBaseInfo.Builder friendBaseInfo = FriendUtil.builderFriendBaseInfo(player, curTime);
            if (friendBaseInfo != null) {
                addApply.setAddFriednApply(friendBaseInfo);
            }

            for (String targetPlayerIdx : allowIdx) {
                playerEntity targetPlayer = playerCache.getByIdx(targetPlayerIdx);
                if (targetPlayer == null) {
                    continue;
                }

                SyncExecuteFunction.executeConsumer(targetPlayer, entity -> {
                    DB_PlayerData.Builder targetBuilder = targetPlayer.getDb_data();
                    if (targetBuilder == null) {
                        LogUtil.error("playerIdx[" + targetPlayerIdx + "] DBData is null");
                        return;
                    }

                    //目标玩家申请列表上限
                    if (targetBuilder.getFriendInfo().getApplyCount() >= GameConfig.getById(GameConst.CONFIG_ID).getApplylimit()) {
                        return;
                    }

                    DB_FriendInfo.Builder targetFriendInfoBuilder = targetBuilder.getFriendInfoBuilder();

                    //已经存在于玩家的申请列表中
                    if (targetFriendInfoBuilder.getApplyMap().containsKey(playerIdx)) {
                        return;
                    }

                    //添加到目标玩家申请列表
                    PlayerDB.DB_ApplyFriendInfo.Builder newApply = PlayerDB.DB_ApplyFriendInfo.newBuilder();
                    newApply.setApplyTime(curTime);
                    targetFriendInfoBuilder.putApply(playerIdx, newApply.build());

                    //发送申请信息,到目标玩家
                    if (GlobalData.getInstance().checkPlayerOnline(targetPlayerIdx)) {
                        GlobalData.getInstance().sendMsg(targetPlayerIdx, MsgIdEnum.SC_AddFriendApply_VALUE, addApply);
                    }

                    resultBuilder.addSuccessPlayerIdx(targetPlayerIdx);
                });
            }
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ApplyAddFriend_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ApplyAddFriend_VALUE, SC_ApplyAddFriend.newBuilder().setRetCode(retCode));
    }
}
