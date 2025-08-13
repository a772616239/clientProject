package model.battle.pve;

import cfg.FightMake;
import cfg.FightMakeObject;
import cfg.FunctionOpenLvConfig;
import cfg.ResourceCopy;
import cfg.ResourceCopyConfig;
import cfg.ResourceCopyObject;
import common.GameConst.EventType;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.StatisticsLogUtil;
import platform.logs.entity.GamePlayLog;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PlayerDB.DB_ResourceCopy;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.ResourceCopy.ResourceCopyTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import server.event.Event;
import server.event.EventManager;
import util.EventUtil;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/04/26
 */
public class ResourceCopyBattleController extends AbstractPveBattleController {

    private final String COPY_TYPE = "copyType";
    private final String COPY_INDEX = "copyIndex";

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (enterParams == null || enterParams.size() < 2) {
            return false;
        }
        putEnterParam(COPY_TYPE, enterParams.get(0));
        putEnterParam(COPY_INDEX, enterParams.get(1));
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        playerEntity player = playerCache.getByIdx(getPlayerIdx());
        if (player == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        if (!player.functionUnLock(EnumFunction.ResCopy)) {
            return RetCodeEnum.RCE_FunctionNotUnLock;
        }

        int copyType = getIntEnterParam(COPY_TYPE);
        int copyIndex = getIntEnterParam(COPY_INDEX);
        ResourceCopyObject copyCfg = ResourceCopy.getInstance().getCopyCfgByTypeAndIndex(copyType, copyIndex);
        if (copyCfg == null || player.getLevel() < copyCfg.getUnlocklv()) {
            return RetCodeEnum.RCE_LvNotEnough;
        }

        DB_ResourceCopy.Builder resourceCopy = player.getResourceCopyData(copyType);
        if (resourceCopy == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        if ((resourceCopy.getBuyTimes() + ResourceCopyConfig.getById(copyType).getChallengetimes())
                <= resourceCopy.getChallengeTimes()) {
            return RetCodeEnum.RCE_ResCopy_FightIsLimit;
        }

        if (!resourceCopy.getUnlockProgressList().contains(copyIndex)) {
            return RetCodeEnum.RCE_ResCopy_IndexCanNotFight;
        }

        setFightMakeId(copyCfg.getFightmakeid());
        return RetCodeEnum.RCE_Success;
    }

    @Override
    protected void initSuccess() {
        //目标：累积参加资源副本战斗
        EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_CumuJoinResCopy, 1, 0);
    }

    public static EnumFunction getFunctionByResType(int copyType) {
        if (copyType == ResourceCopyTypeEnum.RCTE_Crystal_VALUE) {
            return EnumFunction.RelicsRes;
        } else if (copyType == ResourceCopyTypeEnum.RCTE_SoulStone_VALUE) {
            return EnumFunction.SoulRes;
        } else if (copyType == ResourceCopyTypeEnum.RCTE_Rune_VALUE) {
            return EnumFunction.ArtifactRes;
        } else if (copyType == ResourceCopyTypeEnum.RCTE_Awaken_VALUE) {
            return EnumFunction.RuinsRes;
        } else if (copyType == ResourceCopyTypeEnum.RCTE_Gold_VALUE) {
            return EnumFunction.GoldenRes;
        }
        return null;
    }

    @Override
    public int getPointId() {
        return getIntEnterParam(COPY_INDEX);
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
            return null;
        }

        FightMakeObject fightMakeCfg = FightMake.getById(getFightMakeId());
        if (fightMakeCfg == null) {
            return null;
        }
        playerEntity player = playerCache.getByIdx(getPlayerIdx());
        if (player == null) {
            return null;
        }
        List<Reward> rewardsByReward = RewardUtil.getRewardsByRewardId(fightMakeCfg.getRewardid());
        List<Reward> rewards = RewardUtil.additionResourceCopyRewardByVip(player.getVip(), rewardsByReward);
        RewardManager.getInstance().doRewardByList(getPlayerIdx(), rewards,
                ReasonManager.getInstance().borrowReason(getRewardSourceType(), getLogExInfo()), false);
        return rewards;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        if (realResult.getWinnerCamp() != getCamp()) {
            return;
        }
        playerEntity player = playerCache.getByIdx(getPlayerIdx());
        if (player != null) {
            Event event = Event.valueOf(EventType.ET_RESOURCE_COPY_BATTLE_SETTLE,
                    GameUtil.getDefaultEventSource(), player);
            event.pushParam(getIntEnterParam(COPY_TYPE), getIntEnterParam(COPY_INDEX));
            EventManager.getInstance().dispatchEvent(event);
        }

        //玩法统计,胜利才统计
        LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), getFunctionByResType(getIntEnterParam(COPY_TYPE))));
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_ResourceCopy;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_ResCopy;
    }

    @Override
    public String getLogExInfo() {
        return StatisticsLogUtil.getResCopyName(getIntEnterParam(COPY_TYPE));
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Common;
    }
}
