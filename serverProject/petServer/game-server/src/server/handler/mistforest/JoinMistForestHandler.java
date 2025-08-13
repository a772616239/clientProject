package server.handler.mistforest;

import cfg.MistCommonConfig;
import cfg.MistCommonConfigObject;
import cfg.MistMoveEffectConfig;
import cfg.MistMoveEffectConfigObject;
import cfg.MistWorldMapConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.battle.BattleManager;
import model.crossarena.CrossArenaManager;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.mainLine.dbCache.mainlineCache;
import model.matcharena.MatchArenaLTManager;
import model.matcharena.MatchArenaManager;
import model.mistforest.MistForestManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.team.dbCache.teamCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.crossServer.CrossServerManager;
import org.springframework.util.CollectionUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_JoinMistForest;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.SC_JoinMistForest;
import protocol.MistForest.SC_StartMatchGhostBuster;
import protocol.PlayerInfo.MistMoveEffectInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.ServerTransfer.EnumJoinMistForestType;
import protocol.ServerTransfer.GS_CS_JoinMistForest;
import protocol.TargetSystem.TargetMission;
import protocol.TransServerCommon.EnumMistSelfOffPropData;
import protocol.TransServerCommon.MistMazeSyncData;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_JoinMistForest_VALUE)
public class JoinMistForestHandler extends AbstractBaseHandler<CS_JoinMistForest> {
    @Override
    protected CS_JoinMistForest parse(byte[] bytes) throws Exception {
        return CS_JoinMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_JoinMistForest req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        MistRetCode joinCode =  checkPlayerInOtherFunction(playerId);
        if (joinCode == MistRetCode.MRC_Success) {
            switch (req.getMistRuleValue()) {
                case EnumMistRuleKind.EMRK_Common_VALUE: {
                    joinCode = joinCommonMist(player);
                    break;
                }
                case EnumMistRuleKind.EMRK_Maze_VALUE: {
                    joinCode = joinMazeMist(player);
                    break;
                }
                case EnumMistRuleKind.EMRK_GhostBuster_VALUE: {
                    joinCode = joinGhostBusterMist(player);
                    break;
                }
                default:
                    joinCode = MistRetCode.MRC_OtherError;
                    break;
            }
        }

        if (joinCode != MistRetCode.MRC_Success) {
            SC_JoinMistForest.Builder builder = SC_JoinMistForest.newBuilder();
            builder.setRetCode(joinCode);
            gsChn.send(MsgIdEnum.SC_JoinMistForest_VALUE, builder);
        }
    }

    private MistRetCode checkPlayerInOtherFunction(String playerId) {
        if (BattleManager.getInstance().isInBattle(playerId)) {
            return MistRetCode.MRC_Battling;
        }
        if (MatchArenaLTManager.getInstance().checkPlayerAtLeitai(playerId)) {
            return MistRetCode.MRC_MatchArenaLT_LTING;
        }
        if (MatchArenaManager.getInstance().isMatching(playerId)) {
            return MistRetCode.MRC_MatchArena_PlayerMatching;
        }
        if (CrossArenaManager.getInstance().isPlayerInLt(playerId)) {
            return MistRetCode.MRC_MatchArenaLT_LTING;
        }
        return MistRetCode.MRC_Success;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_JoinMistForest_VALUE,
                SC_JoinMistForest.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }

    protected MistRetCode joinCommonMist(playerEntity player) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(player.getIdx());
        if (target == null) {
            return MistRetCode.MRC_TargetNotFound;
        }
        int mistLevel = target.getDb_Builder().getMistTaskData().getCurEnterLevel();
        MistCommonConfigObject cfg = MistCommonConfig.getByMistlevel(mistLevel);
        if (cfg == null) {
            return MistRetCode.MRC_NotFoundMistLevel;
        }
        BaseNettyClient nettyClient = CrossServerManager.getInstance().getAvailableMistForestClient(EnumMistRuleKind.EMRK_Common_VALUE, mistLevel);
        if (nettyClient == null || nettyClient.getState() != 2) {
            return MistRetCode.MRC_NoFoundMistForest;
        }
        List<BattlePetData> petData = teamCache.getInstance().buildBattlePetData(player.getIdx(), TeamNumEnum.TNE_MistForest_1, BattleSubTypeEnum.BSTE_MistForest);
        if (CollectionUtils.isEmpty(petData)) {
            return MistRetCode.MRC_NoPetTeam;
        }
        GS_CS_JoinMistForest.Builder builder = GS_CS_JoinMistForest.newBuilder();
        builder.setMistRule(EnumMistRuleKind.EMRK_Common);
        builder.setServerIndex(ServerConfig.getInstance().getServer());
        builder.setPlayerBaseData(player.getBattleBaseData());
        builder.setMistForestLevel(mistLevel);
        builder.setCrossArenaVipLv(CrossArenaManager.getInstance().findPlayerGradeLv(player.getIdx()));
        builder.setJoinType(EnumJoinMistForestType.EJFT_InitJoin);
        builder.setMainLineUnlockLevel(mainlineCache.getInstance().getPlayerCurCheckPoint(player.getIdx()));
        builder.addAllPetList(petData);
        builder.addAllItemData(player.getDb_data().getMistForestData().getMistItemDataList());
        builder.setIsBattling(BattleManager.getInstance().isInBattle(player.getIdx()));
        builder.setMistStamina(player.getDb_data().getMistForestData().getStamina());
        List<Integer> skills = teamCache.getInstance().getCurUsedTeamSkillList(player.getIdx(), TeamTypeEnum.TTE_MistForest);
        if (skills != null) {
            builder.addAllPlayerSkillIdList(player.getSkillBattleDict(skills));
        }
        builder.putAllPlayerBaseAdditions(player.getDb_data().getPetPropertyAdditionMap());
        builder.putAllCarryRewards(player.getDb_data().getMistForestData().getMistCarryRewardsMap());
        builder.putAllDailyOwnedRewards(player.getDb_data().getMistForestData().getMistDailyGainRewardsMap());

        builder.addAllOfflineBuffs(player.getDb_data().getMistForestData().getOfflineBuffsList());
        if (player.getDb_data().getMistForestData().getOfflineJewelryMistLevel() == mistLevel) {
            builder.setOfflineJewelryCount(player.getDb_data().getMistForestData().getOfflineJewelryCount());
            builder.setHiddenEvilId(player.getDb_data().getMistForestData().getHiddenEvilId());
            builder.setHiddenEvilExpireTime(player.getDb_data().getMistForestData().getHiddenEvilExpireTime());
        }
        int gainInMistLevel = MistForestManager.getInstance().getBossActivityManager().getPlayerGainBossRewardMistLevel(player.getIdx());
        if (gainInMistLevel == mistLevel) {
            builder.setGainBossActivityBoxFlag(true);
        }

        if (mistLevel == player.getDb_data().getMistForestData().getLastEnterMistLevel()) {
            builder.addAllAlchemyData(player.getDb_data().getMistForestDataBuilder().getAlchemyDataList());
            builder.setEliteMonsterRewardTimes(player.getDb_data().getMistForestDataBuilder().getEliteMonsterRewardTimes());
        }
        if (mistLevel == MistWorldMapConfig.getInstance().getDefaultCommonMistLevel()) {
            TargetMission newbieTask = target.getDb_Builder().getMistTaskData().getCurNewbieTask();
            if (newbieTask.getCfgId() > 0 && newbieTask.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                builder.setNewbieTaskId(newbieTask.getCfgId());
            }
        }

        Long expireTime = player.getDb_data().getMistForestData().getSelfOffPropDataMap().get(EnumMistSelfOffPropData.EMSOPD_FateDoorExpireTime_VALUE);
        if (expireTime != null && expireTime > 0 && expireTime < GlobalTick.getInstance().getCurrentTime()) {
            SyncExecuteFunction.executeConsumer(player, entity->{
                entity.getDb_data().getMistForestDataBuilder().removeOfflinePropData(MistUnitPropTypeEnum.MUPT_FateDoorIndex_VALUE);
                entity.getDb_data().getMistForestDataBuilder().removeSelfOffPropData(EnumMistSelfOffPropData.EMSOPD_FateDoorExpireTime_VALUE);
            });
        }
        builder.putAllOffPropData(player.getDb_data().getMistForestData().getOfflinePropDataMap());
        builder.putAllSelfOffPropData(player.getDb_data().getMistForestData().getSelfOffPropDataMap());
        builder.addAllVipSkillData(player.getDb_data().getMistForestData().getVipSkillDataList());

        builder.setMoveEffectId(getPlayerCurMistMoveEffectId(player));

        nettyClient.send(MsgIdEnum.GS_CS_JoinMistForest_VALUE, builder);
        return MistRetCode.MRC_Success;
    }

    protected MistRetCode joinMazeMist(playerEntity player) {
//        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(player.getIdx());
//        if (itemBag == null) {
//            return MistRetCode.MRC_OtherError;
//        }
//        if (player.getDb_data().getMazeDataBuilder().getFreeTickets() <= 0 && itemBag.getItemCount(GameConst.MazeTicketItemId) <= 0) {
//            return MistRetCode.MRC_NotEnoughTicket;
//        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        MistMazeSyncData.Builder mazeData = MistForestManager.getInstance().getMazeManager().getMazeSyncData();
        if (mazeData.getActivityId() == 0 || mazeData.getStartTime() > curTime || mazeData.getEndTime() <= curTime) {
            return MistRetCode.MRC_MazeActivityNotOpen;
        }
        BaseNettyClient nettyClient = CrossServerManager.getInstance().getAvailableMistForestClient(EnumMistRuleKind.EMRK_Maze_VALUE, 1);
        if (nettyClient == null || nettyClient.getState() != 2) {
            return MistRetCode.MRC_NoFoundMistForest;
        }
        GS_CS_JoinMistForest.Builder builder = GS_CS_JoinMistForest.newBuilder();
        builder.setMistRule(EnumMistRuleKind.EMRK_Maze);
        builder.setServerIndex(ServerConfig.getInstance().getServer());
        builder.setPlayerBaseData(player.getBattleBaseData());
        builder.setMistForestLevel(1);
        builder.setJoinType(EnumJoinMistForestType.EJFT_InitJoin);
        builder.addAllItemData(player.getDb_data().getMazeData().getMistMazeItemDataList());
        builder.setIsBattling(BattleManager.getInstance().isInBattle(player.getIdx()));

        builder.putAllDailyOwnedRewards(player.getDb_data().getMazeDataBuilder().getMistMazeDailyGainRewardsMap());

        builder.setMoveEffectId(getPlayerCurMistMoveEffectId(player));
        nettyClient.send(MsgIdEnum.GS_CS_JoinMistForest_VALUE, builder);
        return MistRetCode.MRC_Success;
    }

    protected MistRetCode joinGhostBusterMist(playerEntity player) {
        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(player.getIdx());
        if (itemBag == null) {
            return MistRetCode.MRC_OtherError;
        }
        if (player.getDb_data().getGhostBusterDataBuilder().getFreeTickets() <= 0 && itemBag.getItemCount(GameConst.GhostBusterTicketItemId) <= 0) {
            return MistRetCode.MRC_NotEnoughTicket;
        }
        if (BattleManager.getInstance().isInBattle(player.getIdx())) {
            return MistRetCode.MRC_Battling;
        }
        if (CrossServerManager.getInstance().getActiveNettyClientCount() <= 0) {
            return MistRetCode.MRC_NoFoundMistForest;
        }
        if (CrossServerManager.getInstance().getMistForestPlayerServerInfo(player.getIdx()) != null) {
            return MistRetCode.MRC_InMistForest;
        }

        // 加入匹配队列
        MistRetCode ret = MistForestManager.getInstance().getGhostBusterManager().startMatch(player);
        if (ret == MistRetCode.MRC_Success) {
            SC_StartMatchGhostBuster.Builder builder = SC_StartMatchGhostBuster.newBuilder();
            builder.setStartMatchTime(GlobalTick.getInstance().getCurrentTime());
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_StartMatchGhostBuster_VALUE, builder);
        }
        return ret;
    }

    protected int getPlayerCurMistMoveEffectId(playerEntity player) {
        if (player == null) {
            return 0;
        }
        int curMoveEffectId = player.getDb_data().getMistForestData().getCurMistEffectId();
        MistMoveEffectConfigObject moveEffectCfg = MistMoveEffectConfig.getById(curMoveEffectId);
        if (moveEffectCfg == null) {
            return 0;
        }
        if (moveEffectCfg.getExpiretime() <= 0) {
            return curMoveEffectId;
        }
        for (MistMoveEffectInfo effectInfo : player.getDb_data().getMistForestData().getMoveEffectInfoList()) {
            if (effectInfo.getMoveEffectId() != curMoveEffectId) {
                continue;
            }
            if (effectInfo.getExpireTime() > GlobalTick.getInstance().getCurrentTime()) {
                return curMoveEffectId;
            }
            break;
        }
        return 0;
    }
}
