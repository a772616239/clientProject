package model.matchArena;

import cfg.MatchArenaConfig;
import common.GameConst;
import common.GlobalThread;
import common.GlobalTick;
import common.IdGenerator;
import common.TimeUtil;
import helper.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import model.room.cache.RoomCache;
import model.warpServer.WarpServerManager;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import protocol.Battle.BattleSubTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer;
import protocol.ServerTransfer.ApplyPvpBattleData;
import protocol.ServerTransfer.BS_GS_MatchArenaEnterNormalPveBattle;
import protocol.ServerTransfer.BS_GS_ReplyPvpBattle;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import protocol.ServerTransfer.ReplyPvpBattleData;
import protocol.ServerTransfer.ServerTypeEnum;
import util.GameUtil;
import util.LogUtil;


public class MatchArenaNormalManager implements Runnable {

    @Getter
    private static MatchArenaNormalManager instance = new MatchArenaNormalManager();

    public static final Long SLEEP_TIME = 200L;

    public static final String ROBOT_MARK = "robot";

    private Map<String, ArenaSuccessMatch> successMatch = new ConcurrentHashMap<>();

    private MatchArenaNormalManager() {

    }

    private final Map<String, NormalMatchPlayer> playerInfoMap = new ConcurrentHashMap<>();

    private final AtomicBoolean run = new AtomicBoolean(true);

    public boolean init() {
        GlobalThread.getInstance().submit(this);
        return true;
    }

    private LinkedHashMap<String, Long> matchTimeMap = new LinkedHashMap<>();


    public boolean addMatchPlayer(NormalMatchPlayer player) {
        if (player == null || StringUtils.isBlank(player.getPlayerIdx())) {
            return false;
        }
        matchTimeMap.put(player.getPlayerIdx(), GlobalTick.getInstance().getCurrentTime());
        playerInfoMap.put(player.getPlayerIdx(), player);
        return true;
    }


    @Override
    public void run() {
        LogUtil.info("model.matchArena.MatchArenaManager.run, curTime = " + GlobalTick.getInstance().getCurrentTime());
        Thread.currentThread().setName("model.matchArena.MatchArenaManager.run");
        while (run.get()) {
            try {
                onTick();
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            } finally {
                GameUtil.sleep(SLEEP_TIME);
            }
        }
        LogUtil.warn("model.matchArena.MatchArenaManager.run is over");
    }

    public void close() {
        this.run.set(false);
    }


    private void onTick() {
        enterBattle();

        matchPlayerBattle(groupMatchPlayer());
    }

    private void enterBattle() {
        ArenaSuccessMatch match;
        for (Map.Entry<String, ArenaSuccessMatch> entry : successMatch.entrySet()) {
            match = entry.getValue();
            if (match.isFinishInit()) {
                if (enterPvpBattle(match.getPlayer(), match.getOpponent())) {
                    successMatch.remove(entry.getKey());
                    matchPlayerSuccess(match.getPlayer(), match.getOpponent());
                }
            }
        }
    }


    private List<DefaultKeyValue<String, String>> groupMatchPlayer() {
        if (MapUtils.isEmpty(matchTimeMap)) {
            return Collections.emptyList();
        }
        LinkedHashMap<String, Long> matchMap = matchTimeMap;
        matchTimeMap = new LinkedHashMap<>();

        List<DefaultKeyValue<String, String>> result = new ArrayList<>();

        DefaultKeyValue<String, String> match = null;
        for (Map.Entry<String, Long> entry : matchMap.entrySet()) {
            if (match == null || match.getValue() != null) {
                match = new DefaultKeyValue<>();
                match.setKey(entry.getKey());
            } else {
                match.setValue(entry.getKey());
            }

            if (match.getValue() != null) {
                result.add(match);
            }
        }
        if (match == null) {
            return result;
        }
        if (match.getValue() == null) {
            String key = match.getKey();
            Long matchTime = matchMap.get(key);
            if (GlobalTick.getInstance().getCurrentTime() - matchTime > TimeUtil.MS_IN_A_MIN) {
                match.setValue(ROBOT_MARK);
                result.add(match);
            } else {
                matchTimeMap.put(key, matchTime);
            }
        }
        return result;
    }


    private void matchPlayerBattle(List<DefaultKeyValue<String, String>> matchResult) {
        if (matchResult.isEmpty()) {
            return;
        }

        for (DefaultKeyValue<String, String> keyValue : matchResult) {
            if (Objects.equals(keyValue.getValue(), ROBOT_MARK)) {
                if (enterPve(keyValue.getKey())) {
                    matchRobotSuccess(keyValue.getKey());
                }
            } else {
                findPlayerInfoFromGameServer(keyValue.getKey(), keyValue.getValue());
            }
        }
    }

    private void matchRobotSuccess(String playerId) {
        removeMatchPlayer(playerId);
    }

    private void findPlayerInfoFromGameServer(String playerIdx1, String opponentIdx) {
        NormalMatchPlayer player = playerInfoMap.get(playerIdx1);
        NormalMatchPlayer opponent = playerInfoMap.get(opponentIdx);
        if (player == null || opponent == null) {
            return;
        }
        String battleId = IdGenerator.getInstance().generateId();
        ArenaSuccessMatch arenaSuccessMatch = new ArenaSuccessMatch();
        arenaSuccessMatch.setPlayer(player);
        arenaSuccessMatch.setOpponent(opponent);
        successMatch.put(battleId,arenaSuccessMatch);
        ServerTransfer.BS_GS_BuildMatchArenaPet.Builder msg = ServerTransfer.BS_GS_BuildMatchArenaPet.newBuilder();
        msg.setBattleId(battleId);
        msg.addAllPlayerPetCfgId(player.getPetCfgIds());
        msg.addAllOpponentPetCfgId(opponent.getPetCfgIds());
        WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, player.getFromSvrIndex(),
                MsgIdEnum.BS_GS_BuildMatchArenaPet_VALUE, msg);

    }


    private void matchPlayerSuccess(NormalMatchPlayer player1, NormalMatchPlayer player2) {
        if (player1 != null) {
            removeMatchPlayer(player1.getPlayerIdx());
        }
        if (player2 != null) {
            removeMatchPlayer(player2.getPlayerIdx());
        }
    }

    private synchronized void removeMatchPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        this.matchTimeMap.remove(playerIdx);
    }


    private boolean enterPve(String playerIdx) {
        NormalMatchPlayer player = this.playerInfoMap.get(playerIdx);
        if (player == null) {
            return false;
        }
        BS_GS_MatchArenaEnterNormalPveBattle.Builder pveEnterBuilder = BS_GS_MatchArenaEnterNormalPveBattle.newBuilder();
        pveEnterBuilder.setPlayerIdx(playerIdx);

        return WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, player.getFromSvrIndex(),
                MsgIdEnum.BS_GS_MatchArenaEnterNormalPveBattle_VALUE, pveEnterBuilder);
    }

    private boolean enterPvpBattle(NormalMatchPlayer firstPlayer, NormalMatchPlayer secondPlayer) {

        PvpBattlePlayerInfo firstPvpInfo = firstPlayer.buildPvpPlayerInfo(1, 14);
        PvpBattlePlayerInfo secondPvpInfo = secondPlayer.buildPvpPlayerInfo(2, 14);

        ApplyPvpBattleData.Builder applyPvpBuilder = ApplyPvpBattleData.newBuilder();
        applyPvpBuilder.setFightMakeId(MatchArenaConfig.getById(GameConst.ConfgId).getPvpfightmakeid());
        applyPvpBuilder.setSubBattleType(BattleSubTypeEnum.BSTE_ArenaMatchNormal);
        applyPvpBuilder.addPlayerInfo(firstPvpInfo);
        applyPvpBuilder.addPlayerInfo(secondPvpInfo);

        ReplyPvpBattleData.Builder replyBuilder = RoomCache.getInstance().createRoom(applyPvpBuilder.build(),
                ServerTypeEnum.STE_GameServer, firstPlayer.getFromSvrIndex());

        BS_GS_ReplyPvpBattle.Builder builder = BS_GS_ReplyPvpBattle.newBuilder();
        builder.setReplyPvpBattleData(replyBuilder);

        WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, firstPlayer.getFromSvrIndex(),
                MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);

        if (firstPlayer.getFromSvrIndex() != secondPlayer.getFromSvrIndex()) {
            WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, secondPlayer.getFromSvrIndex(),
                    MsgIdEnum.BS_GS_ReplyPvpBattle_VALUE, builder);
        }
        return true;
    }



    public NormalMatchPlayer getPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        return this.playerInfoMap.get(playerIdx);
    }

    public synchronized RetCodeEnum cancelMatch(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (!this.matchTimeMap.containsKey(playerIdx)) {
            return RetCodeEnum.RCE_MatchArena_NotInMatchStatus;
        }
        return this.matchTimeMap.remove(playerIdx) != null ? RetCodeEnum.RCE_Success : RetCodeEnum.RCE_UnknownError;
    }


    public void initSuccessMatchPlayerPets(ServerTransfer.GS_BS_BuildMatchArenaPet req) {
        ArenaSuccessMatch arenaSuccessMatch = successMatch.get(req.getBattleId());
        if (arenaSuccessMatch == null) {
            LogUtil.warn("match arena match build pets from game server success" +
                    ",but battle server has no this battle info");
            return;
        }
        NormalMatchPlayer player = arenaSuccessMatch.getPlayer();
        player.setBattlePetData(req.getPlayerPetsList());
        arenaSuccessMatch.getOpponent().setBattlePetData(req.getOpponentPetsList());
        arenaSuccessMatch.setFinishInit(true);
    }

    public boolean playerIsInMatch(String playerId) {
        return matchTimeMap.get(playerId)!=null;
    }
}

