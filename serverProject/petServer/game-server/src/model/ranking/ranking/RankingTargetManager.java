package model.ranking.ranking;

import cfg.RankConfig;
import cfg.RankConfigObject;
import cfg.RankRewardTargetConfig;
import cfg.RankRewardTargetConfigObject;
import common.GlobalData;
import common.SyncExecuteFunction;
import db.entity.BaseEntity;
import helper.ObjectUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import model.arena.ArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.player.util.PlayerUtil;
import model.rank.dbCache.rankCache;
import model.rank.entity.TargetRank;
import model.rank.entity.rankEntity;
import model.ranking.RankTargetItemDto;
import model.redpoint.RedPointManager;
import model.redpoint.RedPointOptionEnum;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.springframework.util.CollectionUtils;
import protocol.Activity;
import protocol.Common;
import protocol.MessageId;
import util.LogUtil;

import static protocol.Activity.EnumRankingType.ERT_Ability;
import static protocol.Activity.EnumRankingType.ERT_AbyssPet;
import static protocol.Activity.EnumRankingType.ERT_HellPet;
import static protocol.Activity.EnumRankingType.ERT_MainLine;
import static protocol.Activity.EnumRankingType.ERT_NaturePet;
import static protocol.Activity.EnumRankingType.ERT_PlayerLevel;
import static protocol.Activity.EnumRankingType.ERT_Spire;
import static protocol.Activity.EnumRankingType.ERT_Team1Ability;
import static protocol.Activity.EnumRankingType.ERT_WildPet;
import static protocol.RedPointIdEnum.RedPointId.*;

public class RankingTargetManager {

    @Getter
    private static final RankingTargetManager instance = new RankingTargetManager();


    private final List<Integer> canClaimTarget = Collections.synchronizedList(new ArrayList<>());

    private static final Map<Integer, Long> nextTargetMap = new ConcurrentHashMap<>();

    private static final Map<Integer, List<RankRewardTargetConfigObject>> cfgMap = new HashMap<>();

    private static final Map<Integer, List<RankTargetItemDto>> rewardInfoMap = new ConcurrentHashMap<>();

    private static Map<Integer, List<RankTargetItemDto>> showRewardInfoMap = new ConcurrentHashMap<>();


    private static final List<Activity.EnumRankingType> targetRankType = Arrays.asList(
            ERT_Team1Ability, ERT_Ability, ERT_PlayerLevel, ERT_MainLine, ERT_Spire, ERT_Ability,
            ERT_NaturePet, ERT_WildPet, ERT_AbyssPet, ERT_HellPet

    );

    public void updateRankTarget(int rankType, String playerId, long score) {
        if (!newHighestScore(rankType, score)) {
            return;
        }
        rankEntity rankEntity = rankCache.getInstance().getByRankTypeNumber(rankType);
        if (rankEntity == null) {
            return;
        }
        List<RankRewardTargetConfigObject> targetCfgList = cfgMap.get(rankType);
        if (CollectionUtils.isEmpty(targetCfgList)) {
            return;
        }
        SyncExecuteFunction.executeConsumer(rankEntity, entity -> {
            if (!newHighestScore(rankType, score)) {
                return;
            }
            LogUtil.info("playerIdx:{} updateRankTarget ,rankType:{},score:{}", playerId,  rankType, score);
            saveRecord(rankType, playerId, score, rankEntity, targetCfgList);

            updateNextTargetScore(rankType);
        });

    }

    private void saveRecord(int rankType, String playerId, long score, rankEntity rankEntity, List<RankRewardTargetConfigObject> targetCfgList) {
        playerEntity player = playerCache.getByIdx(playerId);

        targetCfgList.stream().filter(cfg -> cfg.getTargetvalue() <= score && !canClaimTarget.contains(cfg.getId())).forEach(
                cfg -> {
                    rankEntity.getDb_data().addTargetRankAchieve(new TargetRank(playerId, cfg.getId()));
                    saveRankTargetItemDto(rankType, player, cfg.getId());
                }
        );
    }

    private void saveRankTargetItemDto(int rankType, playerEntity player, int cfgId) {
        List<RankTargetItemDto> rankTargetItems = rewardInfoMap.computeIfAbsent(rankType, a -> Collections.synchronizedList(new ArrayList<>()));
        rankTargetItems.add(buildItemDto( player, cfgId));
        canClaimTarget.add(cfgId);
    }

    private RankTargetItemDto buildItemDto( playerEntity player, int cfgId) {
        RankTargetItemDto itemDto = new RankTargetItemDto();
        if (player != null) {
            itemDto.setTargetRewardId(cfgId);
            itemDto.setPlayerId(player.getIdx());
            itemDto.setPlayerName(player.getName());
            itemDto.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
            if (player.getDb_data().getCurAvatarBorder() == playerConstant.AvatarBorderWithRank) {
                itemDto.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(player.getIdx()));
            }
            itemDto.setTitleId(player.getTitleId());
            itemDto.setPlayerAvatar(player.getAvatar());
            itemDto.setCanClaimReward(true);
        }
        return itemDto;
    }

    private boolean newHighestScore(int rankType, long score) {
        Long target = nextTargetMap.get(rankType);
        if (target != null && score < target) {
            return false;
        }
        return true;
    }

    public boolean init() {
        for (Activity.EnumRankingType rankingType : targetRankType) {
            List<RankRewardTargetConfigObject> rankConfigList = new ArrayList<>();
            int rankId = rankingType.getNumber();
            RankConfigObject rankCfg = RankConfig.getByRankid(rankId);
            if (rankCfg == null) {
                LogUtil.error("rankType:{},init error by rankCfg is null", rankingType);
                return false;
            }
            for (int targetId : rankCfg.getRankreward_target()) {
                RankRewardTargetConfigObject rewardTargetCfg = RankRewardTargetConfig.getById(targetId);
                if (rewardTargetCfg == null) {
                    LogUtil.error("rankType:{},init error by rewardTargetCfg is null,targetId:{}", rankingType);
                    return false;
                }
                rankConfigList.add(rewardTargetCfg);
            }
            cfgMap.put(rankingType.getNumber(), rankConfigList);
            updateNextTargetScore(rankId);
        }

        initRewardInfoMap();
        showRewardInfoMap = new ConcurrentHashMap<>(rewardInfoMap);
        return true;
    }

    private void initRewardInfoMap() {
        for (BaseEntity value : rankCache.getInstance()._ix_id.values()) {
            for (TargetRank targetRank : ((rankEntity) value).getDb_data().getTargetRankAchieve()) {
                String playerId = targetRank.getPlayerId();
                saveRankTargetItemDto(((rankEntity) value).getDb_data().getRankId(), playerCache.getByIdx(playerId), targetRank.getTargetId());
            }

        }
    }


    private void updateNextTargetScore(int rankId) {
        rankEntity rankEntity = rankCache.getInstance().getByRankTypeNumber(rankId);
        if (rankEntity == null) {
            return;
        }
        int curtTargetId = rankEntity.getDb_data().getTargetRankAchieve().stream().mapToInt(TargetRank::getTargetId).max().orElse(0);

        List<RankRewardTargetConfigObject> targetMap = cfgMap.get(rankId);
        Optional<RankRewardTargetConfigObject> min = targetMap.stream().filter(e -> e.getId() > curtTargetId).min(Comparator.comparingInt(RankRewardTargetConfigObject::getTargetvalue));
        if (min.isPresent()) {
            nextTargetMap.put(rankId, (long) min.get().getTargetvalue());
            sendRewardRedPointToAllPlayer();
        } else {
            nextTargetMap.put(rankId, Long.MAX_VALUE);
        }
    }

    private void sendRewardRedPointToAllPlayer() {
        for (String playerIdx : GlobalData.getInstance().getAllOnlinePlayerIdx()) {
            if (PlayerUtil.queryFunctionUnlock(playerIdx, Common.EnumFunction.EF_RankingEntrance)) {
                RedPointManager.getInstance().sendRedPoint(playerIdx, RP_HONORWALL_VALUE, RedPointOptionEnum.ADD);
            }
        }
    }

    public boolean targetRewardUnLock(int targetId) {
        return canClaimTarget.contains(targetId);
    }

    public void sendPlayerRankTargetRewardInfo(Activity.EnumRankingType rankingType, String playerIdx) {
        Activity.SC_ClaimRankingTargetRewardInfo.Builder scMsg = Activity.SC_ClaimRankingTargetRewardInfo.newBuilder();
        List<RankTargetItemDto> rankTargetItems = showRewardInfoMap.get(rankingType.getNumber());
        if (CollectionUtils.isEmpty(rankTargetItems)) {
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_ClaimRankingTargetRewardInfo_VALUE, scMsg);
            return;
        }

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_ClaimRankingTargetRewardInfo_VALUE, scMsg);
            return;
        }
        List<Integer> claimedList = target.getDb_Builder().getClaimedRankTargetRewardList();

        for (RankTargetItemDto item : rankTargetItems) {
            scMsg.addTargetReward(getRankingTargetRewardItemBuilder(claimedList, item));
        }
        GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_ClaimRankingTargetRewardInfo_VALUE, scMsg);
    }

    private Activity.RankingTargetRewardItem.Builder getRankingTargetRewardItemBuilder(List<Integer> claimedList, RankTargetItemDto item) {
        Activity.RankingTargetRewardItem.Builder builder = Activity.RankingTargetRewardItem.newBuilder();
        builder.setPlayerId((String) ObjectUtil.defaultIfNull(item.getPlayerId(), ""));
        builder.setPlayerName((String) ObjectUtil.defaultIfNull(item.getPlayerName(), ""));
        builder.setPlayerAvatar(item.getPlayerAvatar());
        builder.setAvatarBorder(item.getAvatarBorder());
        builder.setAvatarBorderRank(item.getAvatarBorderRank());
        builder.setTitleId(item.getTitleId());
        builder.setTargetRewardId(item.getTargetRewardId());
        builder.setBonusState(getBonusStateEnum(claimedList, item));
        return builder;
    }

    private Activity.BonusStateEnum getBonusStateEnum(List<Integer> claimedList, RankTargetItemDto item) {
        if (!item.isCanClaimReward()) {
            return Activity.BonusStateEnum.BSE_CanNotSign;
        }
        if (!claimedList.contains(item.getTargetRewardId())) {
            return Activity.BonusStateEnum.BSE_WaitSignOn;
        }
        return Activity.BonusStateEnum.BSE_AlreadySignOn;


    }

    public void updateRankingTargetRewardInfo(Activity.EnumRankingType rankingType) {
        List<RankTargetItemDto> rankTargetItemDtos = rewardInfoMap.get(rankingType.getNumber());
        if (CollectionUtils.isEmpty(rankTargetItemDtos)) {
            return;
        }
        showRewardInfoMap.put(rankingType.getNumber(), rankTargetItemDtos);
    }

    public List<Integer> getUnLockTargetIds(Activity.EnumRankingType rankingType) {
        if (rankingType == null) {
            return Collections.emptyList();
        }
        RankConfigObject rankCfg = RankConfig.getByRankid(rankingType.getNumber());
        if (rankCfg == null) {
            return Collections.emptyList();
        }
        int[] cfgTargetIds = rankCfg.getRankreward_target();

        return Arrays.stream(cfgTargetIds).filter(canClaimTarget::contains).boxed().collect(Collectors.toList());
    }

    public boolean getRedPointStateClaimReward(String playerId) {
        return canPlayerClaimRankTargetReward(playerId);
    }

    public boolean canPlayerClaimRankTargetReward(String playerIdx) {
        if (PlayerUtil.queryFunctionLock(playerIdx, Common.EnumFunction.EF_RankingEntrance)) {
            return false;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return false;
        }
        List<Integer> claimedList = target.getDb_Builder().getClaimedRankTargetRewardList();

        List<RankTargetItemDto> rankTargetItems;
        for (Map.Entry<Integer, List<RankTargetItemDto>> entry : showRewardInfoMap.entrySet()) {
            rankTargetItems = entry.getValue();
            if (CollectionUtils.isEmpty(rankTargetItems)) {
                continue;
            }
            for (RankTargetItemDto item : rankTargetItems) {
                if (Activity.BonusStateEnum.BSE_WaitSignOn == getBonusStateEnum(claimedList, item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
