package model.battle.pve;


import cfg.MainLineEpisodeConfig;
import cfg.MainLineEpisodeNodeConfig;
import cfg.MainLineEpisodeNodeConfigObject;
import common.SyncExecuteFunction;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import model.battle.AbstractPveBattleController;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.dbCache.teamCache;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.LogService;
import platform.logs.entity.GamePlayLog;
import protocol.Battle;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MainLine;
import protocol.PrepareWar;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@Slf4j
public abstract class BaseEpisodePveBattleController extends AbstractPveBattleController {

    private int episodeNode;

    private Integer episodeId;

    MainLineEpisodeNodeConfigObject nodeCfg;


    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (GameUtil.collectionIsEmpty(enterParams)) {
            return false;
        }
        this.episodeNode = Integer.parseInt(enterParams.get(0));
        episodeId = MainLineEpisodeConfig.getEpisodeIdByNode(episodeNode);
        nodeCfg = MainLineEpisodeNodeConfig.getById(episodeNode);
        if (nodeCfg == null) {
            return false;
        }
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(getPlayerIdx());
        if (entity == null) {
            LogUtil.error("playerIdx[" + getPlayerIdx() + "] mainLineEntity is null");
            return RetCodeEnum.RCE_UnknownError;
        }

        if (nodeCfg.getFightmakeid() <= 0) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        if (episodeId == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        MainLine.EpisodeProgress episodeProgress = entity.getDBBuilder().getEpisodeProgressMap().get(episodeId);

        if (episodeProgress == null || episodeProgress.getCurEpisodeId() < episodeNode) {
            //未解锁
            return RetCodeEnum.RCE_ErrorParam;
        }
        if (episodeProgress.getEpisodeId() > episodeNode || episodeProgress.getCompleteProgressList().contains(MainLine.EpisodeProgressType.EPT_Fight)) {
            //已挑战
            return RetCodeEnum.RCE_Battle_RepeatedEnterBattle;
        }

        setFightMakeId(nodeCfg.getFightmakeid());


        return RetCodeEnum.RCE_Success;
    }

    protected void buildSkillData(String playerIdx, PrepareWar.TeamNumEnum teamNum, Battle.BattlePlayerInfo.Builder battlePlayerInfo) {
        for (int[] skill : nodeCfg.getPlayerskill()) {
            if (skill.length != 2) {
                continue;
            }
            battlePlayerInfo.addPlayerSkillIdList(Battle.SkillBattleDict.newBuilder()
                    .setSkillId(skill[0]).setSkillLv(skill[1]).build());
        }
    }


    @Override
    protected void initSuccess() {
        LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.MainLine));
    }

    @Override
    public int getPointId() {
        return 0;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Common;
    }

    @Override
    public void tailSettle(CS_BattleResult resultData, List<Reward> rewardList, SC_BattleResult.Builder resultBuilder) {

        if (resultData.getWinnerCamp() != 1) {
            return;
        }

        log.info("player:{} win episode battle,episodeId:{},nodeId:{}", getPlayerIdx(), episodeId, episodeNode);

        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(getPlayerIdx());
        if (entity == null) {
            LogUtil.error("playerIdx[" + getPlayerIdx() + "] mainLineEntity is null");
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, e ->
                entity.addEpisodeProgress(episodeId, MainLine.EpisodeProgressType.EPT_Fight)
        );
        entity.sendEpisodeUpdate(episodeId);
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MainLineCheckPoint;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_Null;
    }

}
