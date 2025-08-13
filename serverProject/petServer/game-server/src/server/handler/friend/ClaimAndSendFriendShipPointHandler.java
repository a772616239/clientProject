package server.handler.friend;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.FriendUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Friend.CS_ClaimAndSendFriendShipPoint;
import protocol.Friend.SC_ClaimAndSendFriendShipPoint;
import protocol.Friend.SC_ClaimAndSendFriendShipPoint.Builder;
import protocol.Friend.SC_RecvPoint;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_FriendInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimAndSendFriendShipPoint_VALUE)
public class ClaimAndSendFriendShipPointHandler extends AbstractBaseHandler<CS_ClaimAndSendFriendShipPoint> {
    @Override
    protected CS_ClaimAndSendFriendShipPoint parse(byte[] bytes) throws Exception {
        return CS_ClaimAndSendFriendShipPoint.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimAndSendFriendShipPoint req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        String targetIdx = req.getTargetIdx();
        playerEntity player = playerCache.getByIdx(playerIdx);
        playerEntity targetPlayer = playerCache.getByIdx(targetIdx);
        Builder resultBuilder = SC_ClaimAndSendFriendShipPoint.newBuilder();
        if (player == null || targetPlayer == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimAndSendFriendShipPoint_VALUE, resultBuilder);
            return;
        }

        //收取
        RetCodeEnum claimRetCode = SyncExecuteFunction.executeFunction(player, p -> {
            DB_FriendInfo.Builder friendInfoBuilder = player.getDb_data().getFriendInfoBuilder();

            if (!friendInfoBuilder.getOwnedMap().containsKey(targetIdx)) {
                return RetCodeEnum.RCE_Friend_TargetIsNotFriend;
            }

            Boolean aBoolean = friendInfoBuilder.getRecvPointMap().get(targetIdx);
            if (aBoolean == null || Boolean.FALSE.equals(aBoolean)) {
                resultBuilder.setTodayGainItem(friendInfoBuilder.getTodayGainFriendItemCount());
                return RetCodeEnum.RCE_Success;
            }

            friendInfoBuilder.putRecvPoint(targetIdx, false);
            resultBuilder.setClaim(false);

            int canGetCount = FriendUtil.getFriendItemGainLimit(playerIdx) - friendInfoBuilder.getTodayGainFriendItemCount();
            if (canGetCount <= 0) {
                resultBuilder.setTodayGainItem(friendInfoBuilder.getTodayGainFriendItemCount());
                return RetCodeEnum.RCE_Friend_FriendItemGainLimit;
            }

            Reward reward = RewardUtil.parseReward(GameConfig.getById(GameConst.CONFIG_ID).getSendfriendpointreawrd());
            if (canGetCount < reward.getCount()) {
                reward = reward.toBuilder().setCount(canGetCount).build();
            }
            RewardManager.getInstance().doReward(playerIdx, reward, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Friend), true);

            int dailyOwnedCount = friendInfoBuilder.getTodayGainFriendItemCount() + reward.getCount();
            friendInfoBuilder.setTodayGainFriendItemCount(dailyOwnedCount);
            resultBuilder.setTodayGainItem(friendInfoBuilder.getTodayGainFriendItemCount());

            p.getDb_data().getMistForestDataBuilder().putMistDailyGainRewards(GameConst.FriendPointMistCfgId, dailyOwnedCount);
            return RetCodeEnum.RCE_Success;
        });

        //赠送
        if (claimRetCode == RetCodeEnum.RCE_Success
                || claimRetCode == RetCodeEnum.RCE_Friend_FriendItemGainLimit) {
            //是否需要赠送
            boolean needSend = SyncExecuteFunction.executePredicate(player, p -> {
                DB_FriendInfo.Builder friendInfoBuilder = player.getDb_data().getFriendInfoBuilder();
                if (friendInfoBuilder.getSendFriendshipPointList().contains(targetIdx)) {
                    return false;
                }

                friendInfoBuilder.addSendFriendshipPoint(targetIdx);

                resultBuilder.setSend(false);

                return true;
            });

            if (needSend) {
                //赠送目标好友
                SyncExecuteFunction.executeConsumer(targetPlayer, t -> {
                    DB_FriendInfo.Builder friendInfoBuilder = targetPlayer.getDb_data().getFriendInfoBuilder();
                    friendInfoBuilder.putRecvPoint(playerIdx, true);

                    if (targetPlayer.isOnline()) {
                        SC_RecvPoint.Builder recvBuilder = SC_RecvPoint.newBuilder();
                        recvBuilder.setSenderPlayerIdx(playerIdx);
                        GlobalData.getInstance().sendMsg(targetIdx, MsgIdEnum.SC_RecvPoint_VALUE, recvBuilder);
                    }
                });
            }
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(claimRetCode));
        gsChn.send(MsgIdEnum.SC_ClaimAndSendFriendShipPoint_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimAndSendFriendShipPoint_VALUE, SC_ClaimAndSendFriendShipPoint.newBuilder().setRetCode(retCode));
    }
}
