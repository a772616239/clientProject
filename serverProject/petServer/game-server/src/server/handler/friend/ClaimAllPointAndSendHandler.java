package server.handler.friend;

import cfg.GameConfig;
import com.google.protobuf.ProtocolStringList;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.FriendUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Friend.CS_ClaimAllPointAndSend;
import protocol.Friend.SC_ClaimAllPointAndSend;
import protocol.Friend.SC_RecvPoint;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_FriendInfo;
import protocol.PlayerDB.DB_OwnedFriendInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimAllPointAndSend_VALUE)
public class ClaimAllPointAndSendHandler extends AbstractBaseHandler<CS_ClaimAllPointAndSend> {
    @Override
    protected CS_ClaimAllPointAndSend parse(byte[] bytes) throws Exception {
        return CS_ClaimAllPointAndSend.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimAllPointAndSend req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimAllPointAndSend.Builder resultBuilder = SC_ClaimAllPointAndSend.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimAllPointAndSend_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        List<String> sendPlayerIdx = new ArrayList<>();
        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_FriendInfo.Builder friendInfoBuilder = player.getDb_data().getFriendInfoBuilder();

            Map<String, Boolean> recvPointMap = friendInfoBuilder.getRecvPointMap();
            int addCount = 0;
            if (recvPointMap != null && recvPointMap.size() > 0) {
                for (Entry<String, Boolean> entry : recvPointMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue()) {
                        addCount++;
                        friendInfoBuilder.putRecvPoint(entry.getKey(), false);
                        resultBuilder.addClaim(entry.getKey());
                    }
                }
            }

            if (addCount > 0) {
                Reward reward = RewardUtil.parseAndMulti(GameConfig.getById(GameConst.CONFIG_ID).getSendfriendpointreawrd(), addCount);
                int canGetCount = FriendUtil.getFriendItemGainLimit(playerIdx) - friendInfoBuilder.getTodayGainFriendItemCount();
                if (canGetCount < reward.getCount()) {
                    reward = reward.toBuilder().setCount(Math.max(0, canGetCount)).build();
                    resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Friend_FriendItemGainLimit));
                }
                RewardManager.getInstance().doReward(playerIdx, reward, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Friend), true);

                int dailyOwnedCount = friendInfoBuilder.getTodayGainFriendItemCount() + reward.getCount();
                friendInfoBuilder.setTodayGainFriendItemCount(dailyOwnedCount);
                entity.getDb_data().getMistForestDataBuilder().putMistDailyGainRewards(GameConst.FriendPointMistCfgId, dailyOwnedCount);
            }
            resultBuilder.setTodayGainItem(friendInfoBuilder.getTodayGainFriendItemCount());

            //送友情点
            if (friendInfoBuilder.getOwnedCount() > 0) {
                ProtocolStringList sendFriendshipPointList = friendInfoBuilder.getSendFriendshipPointList();
                Map<String, DB_OwnedFriendInfo> ownedMap = friendInfoBuilder.getOwnedMap();

                for (String idx : ownedMap.keySet()) {
                    if (!sendFriendshipPointList.contains(idx)) {
                        sendPlayerIdx.add(idx);
                    }
                }

                friendInfoBuilder.addAllSendFriendshipPoint(sendPlayerIdx);
            }
        });


        if (CollectionUtils.isNotEmpty(sendPlayerIdx)) {
            SC_RecvPoint.Builder recvBuilder = SC_RecvPoint.newBuilder();
            recvBuilder.setSenderPlayerIdx(playerIdx);
            for (String sendIdx : sendPlayerIdx) {
                playerEntity sendPlayer = playerCache.getByIdx(sendIdx);
                if (sendPlayer == null) {
                    LogUtil.error("playerIdx[" + sendIdx + "] entity is null");
                    continue;
                }

                SyncExecuteFunction.executeConsumer(sendPlayer, entity -> {
                    sendPlayer.getDb_data().getFriendInfoBuilder().putRecvPoint(playerIdx, true);

                    if (sendPlayer.isOnline()) {
                        GlobalData.getInstance().sendMsg(sendIdx, MsgIdEnum.SC_RecvPoint_VALUE, recvBuilder);
                    }
                });
            }
        }

        resultBuilder.addAllSend(sendPlayerIdx);
        gsChn.send(MsgIdEnum.SC_ClaimAllPointAndSend_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Friend;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimAllPointAndSend_VALUE, SC_ClaimAllPointAndSend.newBuilder().setRetCode(retCode));
    }
}
