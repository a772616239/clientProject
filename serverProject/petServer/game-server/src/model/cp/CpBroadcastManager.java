package model.cp;

import cfg.CpTeamRewardCfg;
import cfg.MailTemplateUsed;
import com.google.protobuf.GeneratedMessageV3;
import common.GameConst;
import common.GlobalData;
import common.JedisUtil;
import common.tick.GlobalTick;
import common.tick.Tickable;
import helper.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import model.cp.broadcast.CpCopySettle;
import model.cp.broadcast.CpCopyUpdate;
import model.cp.broadcast.CpPlayerLeaveCopy;
import model.cp.broadcast.CpTeamUpdate;
import model.cp.entity.CpCopyMap;
import model.crossarena.CrossArenaManager;
import model.crossarena.CrossArenaUtil;
import model.player.dbCache.playerCache;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.CpFunction;
import protocol.CrossArena;
import server.handler.cp.CpFunctionUtil;
import util.EventUtil;
import util.LogUtil;
import util.ObjUtil;

import static model.cp.CpRedisKey.*;
import static protocol.MessageId.MsgIdEnum.SC_AddNewApplyJoinTeam_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_CpFunctionUpdateBroadcast_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_CpPlayerTeamUpdate_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PlayerLeaveCopy_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateCopyPlayerReviveTimes_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdateCpTeamPoint_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdatePlayerOnlineState_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_UpdatePlayerState_VALUE;


/**
 * 向其他玩家广播消息
 */
public class CpBroadcastManager implements Tickable {

    @Getter
    private static final CpBroadcastManager instance = new CpBroadcastManager();

    private final CpTeamCache cache = CpTeamCache.getInstance();

    private static final int interval = 1800;

    private long nextTickTime;

    public void broadcastCopySettle(CpCopyMap mapData) {

        CpCopySettle cpCopySettle = new CpCopySettle(mapData);

        CpTeamCache.getInstance().saveCopySettle(cpCopySettle);

    }

    public void broadcastCopyUpdate(CpCopyUpdate cpCopyUpdate) {
        CpFunction.SC_UpdateCpTeamPoint.Builder msg = buildCpCopyUpdateMsg(cpCopyUpdate);
        broadcastMsg(cpCopyUpdate.getMembers(), SC_UpdateCpTeamPoint_VALUE, msg.build());
    }

    public void broadcastTeamUpdate(CpTeamUpdate teamUpdate) {
        broadcastMsg(teamUpdate.getMembers(), SC_CpPlayerTeamUpdate_VALUE,
                buildTeamUpdateMsg(teamUpdate.getTeamId()).build());

    }

    public boolean init() {
        return GlobalTick.getInstance().addTick(this);
    }


    @Override
    public void onTick() {
        if (GlobalTick.getInstance().getCurrentTime() < nextTickTime) {
            return;
        }
        checkAndSendBroadcast();
        nextTickTime = GlobalTick.getInstance().getCurrentTime() + interval;
    }

    private void checkAndSendBroadcast() {
        checkAndSettleBroadcastCopy();
    }


    private CpFunction.SC_PlayerLeaveCopy.Builder buildCpCopyPlayerLeaveMsg(CpPlayerLeaveCopy entity) {
        CpFunction.SC_PlayerLeaveCopy.Builder msg = CpFunction.SC_PlayerLeaveCopy.newBuilder();
        msg.setPlayerIdx(entity.getLeavePlayerIdx());
        msg.setWin(entity.isWin());
        return msg;
    }


    private CpFunction.SC_UpdateCpTeamPoint.Builder buildCpCopyUpdateMsg(CpCopyUpdate entity) {
        CpFunction.SC_UpdateCpTeamPoint.Builder msg = CpFunction.SC_UpdateCpTeamPoint.newBuilder();
        if (entity.getPointId() > 0) {
            CpFunction.CpCopyPoint.Builder point = CpFunction.CpCopyPoint.newBuilder().setPointId(entity.getPointId())
                    .setDifficult(entity.getDifficult()).setEvent(CpFunction.CpFunctionEvent.forNumber(entity.getPointType()));
            if (StringUtils.isNotBlank(entity.getBattlePlayerIdx())) {
                point.setFightingPlayerIdx(entity.getBattlePlayerIdx());
            }
            msg.setUpdatePoint(point.build());
        }
        if (StringUtils.isNotBlank(entity.getPlayerIdx())) {
            CpFunction.CpCopyPlayer.Builder builder = CpFunction.CpCopyPlayer.newBuilder().setPlayerIdx(entity.getPlayerIdx()).setHeader(entity.getHeader())
                    .setBorderId(entity.getBorderId()).setStarScore(entity.getStarScore())

                    .addAllPassPoint(entity.getPassPoints())
                    .setReviveTimes(CpCopyManger.getInstance().queryPlayerReviveTime(entity.getPlayerIdx()))
                    .setLimitReviveTimes(CpCopyManger.getInstance().queryLimitReviveTimes(entity.getPlayerIdx()));
            msg.setUpdatePlayer(builder);
        }
        return msg;
    }


    private void checkAndSettleBroadcastCopy() {
        if (!JedisUtil.lockRedisKey(CpBroadcastExpireCheck, 3000)) {
            return;
        }
        excuteSettleBroadcast();
        JedisUtil.unlockRedisKey(CpBroadcastExpireCheck);
    }

    private boolean excuteSettleBroadcast() {
        Map<byte[], byte[]> map = JedisUtil.jedis.hgetAll(CpBattleSettle.getBytes(StandardCharsets.UTF_8));
        if (CollectionUtils.isEmpty(map)) {
            return true;
        }

        for (byte[] data : map.values()) {
            CpCopySettle settleEntity = (CpCopySettle) ObjUtil.byteToObject(data);

            Set<String> localPlayerIdx = settleEntity.getNeedSettlePlayers().stream().filter(this::localPlayer)
                    .filter(e -> !settleEntity.getAlreadySettlePlayerIdx().contains(e)).collect(Collectors.toSet());
            localPlayerIdx.forEach(playerId -> settleCopy(playerId, settleEntity));
            settleEntity.addAllSettlePlayerIdx(localPlayerIdx);
            if (settleEntity.allSettle()) {
                JedisUtil.jedis.hdel(CpBattleSettle, settleEntity.getMapId());
                CpTeamCache.getInstance().removeCopyMapData(settleEntity.getMapId());
            } else {
                CpTeamCache.getInstance().saveCopySettle(settleEntity);
            }

        }
        return true;
    }

    private void settleCopy(String playerId, CpCopySettle settle) {
        if (CpFunctionUtil.isRobot(playerId)){
            return;
        }
        int score = settle.getTeamScore();
        List<Integer> alreadySettle = settle.getAlreadyClaimRewardId().get(playerId);
        List<Common.Reward> rewardByScore = CpTeamRewardCfg.getRewardByScore(score, CrossArenaManager.getInstance().findPlayerMaxSceneId(playerId), alreadySettle);
        CrossArenaManager.getInstance().savePlayerDBInfo(playerId, CrossArena.CrossArenaDBKey.ZD_STARMAX, score, CrossArenaUtil.DbChangeRepMax);
        //领取过奖励,自动结算没有新奖励的不发放邮件
        if (!CollectionUtils.isEmpty(alreadySettle)&&CollectionUtils.isEmpty(rewardByScore)){
            return;
        }
        LogUtil.info("settle cp function copy,mapId:{}, playerIdx:{} ,score:{},reward:{}", settle.getMapId(), playerId, score, rewardByScore);
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_LT_CP);
        EventUtil.triggerAddMailEvent(playerId, getSettleMail(rewardByScore), rewardByScore
                , reason, settle.getTeamName());
    }

    private int getSettleMail(List<Common.Reward> rewardByScore) {
        if (CollectionUtils.isEmpty(rewardByScore)) {
            return MailTemplateUsed.getById(GameConst.CONFIG_ID).getNoreward();
        }
        return MailTemplateUsed.getById(GameConst.CONFIG_ID).getLtcopysettle();
    }


    private CpFunction.SC_CpPlayerTeamUpdate.Builder buildTeamUpdateMsg(int teamId) {
        CpFunction.SC_CpPlayerTeamUpdate.Builder msg = CpFunction.SC_CpPlayerTeamUpdate.newBuilder();

        CpFunction.CpPlayerTeam.Builder team = CpFunctionUtil.buildCpPlayerTeam(teamId);

        if (team != null) {
            msg.setTeam(team);
        }
        return msg;
    }


    private boolean localPlayer(String member) {
        return playerCache.getByIdx(member) != null;
    }

    public void playerLeaveCopy(String playerIdx, List<String> members, boolean win, int leaveType) {
        CpFunction.SC_PlayerLeaveCopy.Builder msg = buildCpCopyPlayerLeaveMsg(playerIdx,win,leaveType);
        broadcastMsg(members, SC_PlayerLeaveCopy_VALUE, msg.build());
    }

    private CpFunction.SC_PlayerLeaveCopy.Builder buildCpCopyPlayerLeaveMsg(String playerIdx, boolean win, int leaveType) {
        CpFunction.SC_PlayerLeaveCopy.Builder msg = CpFunction.SC_PlayerLeaveCopy.newBuilder();
        msg.setWin(win).setPlayerIdx(playerIdx).setLeaveType(leaveType);
        return msg;
    }

    public void broadcastCopyActive(List<String> playerIds) {
        CpFunction.SC_CpFunctionUpdateBroadcast.Builder msg = CpFunction.SC_CpFunctionUpdateBroadcast.newBuilder().setActiveCopy(true);
        broadcastMsg(playerIds, SC_CpFunctionUpdateBroadcast_VALUE, msg.build());
    }

    public void broadcastAddInvite(String playerIdx, String leaderIdx) {
        CpFunction.SC_AddNewApplyJoinTeam.Builder msg = CpFunction.SC_AddNewApplyJoinTeam.newBuilder();
        CpFunction.InviteCpPlayer.Builder player = CpFunctionUtil.queryInviteCpPlayer(playerIdx);
        if (player == null) {
            return;
        }
        msg.setPlayer(player);

        broadcastMsg(Collections.singletonList(leaderIdx), SC_AddNewApplyJoinTeam_VALUE, msg.build());
    }

    public void broadcastPlayerState(String playerIdx, List<String> sendPlayerIdx, CpFunction.CpCopyPlayerState state) {

        CpFunction.SC_UpdatePlayerState.Builder msg = CpFunction.SC_UpdatePlayerState.newBuilder();
        msg.setPlayerIdx(playerIdx).setState(state);

        broadcastMsg(sendPlayerIdx, SC_UpdatePlayerState_VALUE, msg.build());
    }

    public void broadcastMsg(List<String> sendPlayers, int msgId, GeneratedMessageV3 msgData) {
        GlobalData.getInstance().forwardMsg(findPlayerSvrIndex(sendPlayers), msgId, msgData);
    }

    private Map<String, String> findPlayerSvrIndex(List<String> playerIdx) {
        if (CollectionUtils.isEmpty(playerIdx)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (String idx : playerIdx) {
            if (CpFunctionUtil.isRobot(idx)) {
                continue;
            }
            String playerSvrIndex = cache.findPlayerSvrIndex(idx);
            result.put(idx, playerSvrIndex);
        }
        return result;
    }


    public void broadcastPlayerOnlineState(String playerIdx, List<String> sendPlayerIdx, boolean online) {
        sendPlayerIdx.remove(playerIdx);

        CpFunction.SC_UpdatePlayerOnlineState.Builder msg = CpFunction.SC_UpdatePlayerOnlineState.newBuilder();
        msg.setPlayerIdx(playerIdx).setOnline(online);

        broadcastMsg(sendPlayerIdx, SC_UpdatePlayerOnlineState_VALUE, msg.build());

    }

    public void broadcastTeamKickOut(String kickOutPlayer) {
        CpFunction.SC_CpPlayerTeamUpdate.Builder msg = CpFunction.SC_CpPlayerTeamUpdate.newBuilder();
        msg.setTeam(CpFunction.CpPlayerTeam.getDefaultInstance());
        broadcastMsg(Collections.singletonList(kickOutPlayer), SC_CpPlayerTeamUpdate_VALUE,
                msg.build());
    }

    public void broadcastPlayerReviveTimes(String playerIdx, List<String> onPlayPlayer) {
        CpFunction.SC_UpdateCopyPlayerReviveTimes.Builder msg = CpFunction.SC_UpdateCopyPlayerReviveTimes.newBuilder();
        msg.setPlayerIdx(playerIdx);
        msg.setReviveTimes(CpCopyManger.getInstance().queryPlayerReviveTime(playerIdx));
        msg.setLimitReviveTimes(CpCopyManger.getInstance().queryLimitReviveTimes(playerIdx));
        broadcastMsg(onPlayPlayer, SC_UpdateCopyPlayerReviveTimes_VALUE,
                msg.build());

    }
}
