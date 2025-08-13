package model.battle.pve;

import cfg.CpTeamCfg;
import cfg.Head;
import cfg.MatchArenaConfig;
import common.GameConst;
import common.JedisUtil;
import model.battle.AbstractPveBattleController;
import model.cp.CpBroadcastManager;
import model.cp.CpCopyManger;
import model.cp.CpTeamCache;
import model.cp.broadcast.CpCopyUpdate;
import model.cp.entity.CpCopyMap;
import model.cp.entity.CpCopyMapPoint;
import model.cp.entity.CpTeamCopyPlayerProgress;
import model.cp.entity.CpTeamMember;
import model.matcharena.MatchArenaUtil;
import model.pet.dbCache.petCache;
import model.player.util.PlayerUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import protocol.Battle;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.CpFunction;
import protocol.CpFunction.CpCopyPlayerState;
import protocol.PrepareWar;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.LogUtil;
import util.ObjUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CpPveBattleController extends AbstractPveBattleController {

    private int pointId;

    private CpCopyMapPoint cpCopyMapPoint;

    private CpFunction.CpCopyPlayerState playerState;

    private String mapId;


    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        CpCopyMap mapData = CpTeamCache.getInstance().findPlayerCopyMapInfo(getPlayerIdx());
        if (mapData == null) {
            LogUtil.error("CpPveBattleController tailSettle error,mapData is null by playerIdx:{}", getPlayerIdx());
            return;
        }
        JedisUtil.syncExecBooleanSupplier(CpCopyManger.getInstance().getCopyUpdateRedisKey(mapData.getMapId()), () -> {
            boolean win = realResult.getWinnerCamp() == 1;
            CpTeamCopyPlayerProgress progress = mapData.getProgress(getPlayerIdx());
            if (progress == null) {
                LogUtil.error("CpPveBattleController tailSettle error,progress is null by playerIdx:{}", getPlayerIdx());
                return false;
            }
            //玩家失败
            if (!win) {
                settleFail(progress, mapData);
                // CpCopyManger.getInstance().trySettleCopy(mapData);
            } else {
                progress.addPassPoint(pointId);
                progress.setFloor(CpFunctionUtil.queryPointFloor(pointId));
                int addScore = queryAddScore(progress.isDoubleStarReward());
                progress.addStarScore(addScore);
                progress.setDoubleStarReward(false);

                CpCopyManger.getInstance().settleRobot(mapData, pointId, true);
            }
            CpTeamCache.getInstance().saveCopyMap(mapData);
            CpBroadcastManager.getInstance().broadcastCopyUpdate(new CpCopyUpdate(cpCopyMapPoint, progress, mapData.getMembers()));
            return true;
        });
    }

    private int queryAddScore(boolean doubleReward) {
        int i = CpTeamCfg.queryDiffScore(cpCopyMapPoint.getDifficulty());
        if (doubleReward) {
            return 2 * i;
        }
        return i;
    }


    private void settleFail(CpTeamCopyPlayerProgress progress, CpCopyMap mapData) {
        cpCopyMapPoint = mapData.queryPointById(pointId);
        if (cpCopyMapPoint != null) {
            cpCopyMapPoint.setPlayerIdx(null);
        }

        if (CpCopyManger.getInstance().canRevive(progress.getPlayerIdx())) {
            mapData.updatePlayerState(getPlayerIdx(), CpFunction.CpCopyPlayerState.CCPS_ReviveAble);
            CpBroadcastManager.getInstance().broadcastPlayerState(getPlayerIdx(), mapData.getOnPlayPlayer(), CpFunction.CpCopyPlayerState.CCPS_ReviveAble);
            return;
        }
        mapData.updatePlayerState(getPlayerIdx(), CpFunction.CpCopyPlayerState.CCPS_Fail);
        CpBroadcastManager.getInstance().broadcastPlayerState(getPlayerIdx(), mapData.getOnPlayPlayer(), CpFunction.CpCopyPlayerState.CCPS_Fail);

        // CpCopyManger.getInstance().playerLeaveCopy(getPlayerIdx(), mapData, false);
/*        progress.setSuccess(false);
        progress.setFinish(true);*/
    }


    @Override
    public List<Battle.BattlePetData> buildBuildBattlePetData(String playerIdx, PrepareWar.TeamNumEnum teamNum) {
        CpTeamMember cpTeamMember = CpTeamCache.getInstance().loadPlayerInfo(playerIdx);
        if (cpTeamMember == null) {
            return Collections.emptyList();
        }
        return cpTeamMember.getPetData();
    }


    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        return Collections.emptyList();
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_LTCpTeam;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_LT_CP;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_LtCP;
    }

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (CollectionUtils.isEmpty(enterParams)) {
            return false;
        }
        try {
            pointId = Integer.parseInt(enterParams.get(0));

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        RetCodeEnum codeEnum = checkCanFight();

        if (codeEnum != RetCodeEnum.RCE_Success) {
            return codeEnum;
        }

        Battle.BattlePlayerInfo.Builder opponentInfo = Battle.BattlePlayerInfo.newBuilder();
        Battle.PlayerBaseInfo.Builder basePlayerInfo = Battle.PlayerBaseInfo.newBuilder();
        basePlayerInfo.setPlayerName(ObjUtil.createRandomName(PlayerUtil.queryPlayerLanguage(getPlayerIdx())));
        basePlayerInfo.setAvatar(Head.randomGetAvatar());
        basePlayerInfo.setLevel(MatchArenaUtil.randomRobotPlayerLv(PlayerUtil.queryPlayerLv(getPlayerIdx())));
        opponentInfo.setPlayerInfo(basePlayerInfo);
        opponentInfo.setCamp(2);
        opponentInfo.addAllPetList(petCache.getInstance().buildPetBattleData(null, cpCopyMapPoint.getMonsters(), getSubBattleType(), false));
        opponentInfo.setIsAuto(true);

        addPlayerBattleData(opponentInfo.build());

        setTeamBuff();

        setFightMakeId(MatchArenaConfig.getById(GameConst.CONFIG_ID).getPvefightmakeid());

        CpCopyMap mapData = CpTeamCache.getInstance().findPlayerCopyMapInfo(getPlayerIdx());
        if (mapData != null) {
            CpBroadcastManager.getInstance().broadcastCopyUpdate(new CpCopyUpdate(cpCopyMapPoint, getPlayerIdx(), mapData.getMembers()));
        }
        return RetCodeEnum.RCE_Success;
    }

    private RetCodeEnum checkCanFight() {
        String mapId = CpTeamCache.getInstance().findPlayerCopyMapId(getPlayerIdx());
        if (StringUtils.isEmpty(mapId)) {
            return RetCodeEnum.RCE_CP_CopyNotExists;
        }
        return JedisUtil.syncExecSupplier(CpCopyManger.getInstance().getCopyUpdateRedisKey(mapId), () -> {

            CpCopyMap copyMap = CpCopyManger.getInstance().findCopyMapData(mapId);
            if (copyMap == null) {
                return RetCodeEnum.RCE_CP_CopyNotExists;
            }
            CpTeamCopyPlayerProgress progress = copyMap.getProgress(getPlayerIdx());
            if (progress == null) {
                return RetCodeEnum.RCE_UnknownError;
            }
            playerState = copyMap.queryPlayerState(getPlayerIdx());

            if (CpFunctionUtil.queryPointFloor(pointId) != progress.getFloor() + 1) {
                return RetCodeEnum.RCE_ErrorParam;
            }
            cpCopyMapPoint = copyMap.queryPointById(pointId);
            if (cpCopyMapPoint == null) {
                return RetCodeEnum.RCE_ErrorParam;
            }

            if (playerState != null && CpCopyPlayerState.CCPS_Survive != playerState) {
                return RetCodeEnum.RCE_ErrorParam;
            }

            if (!StringUtils.isEmpty(cpCopyMapPoint.getPlayerIdx()) && !cpCopyMapPoint.getPlayerIdx().equals(getPlayerIdx())) {
                return RetCodeEnum.RCE_CP_PlayerIsFighting;
            }
            cpCopyMapPoint.setPlayerIdx(getPlayerIdx());

            CpTeamCache.getInstance().saveCopyMap(copyMap);

            return RetCodeEnum.RCE_Success;
        });
    }


    private void setTeamBuff() {

        Map<Integer, Integer> buffs = CpCopyManger.getInstance().queryPlayerBattleBuff(getPlayerIdx());
        if (CollectionUtils.isEmpty(buffs)) {
            return;
        }
        Battle.ExtendProperty.Builder extendProperty = Battle.ExtendProperty.newBuilder();

        extendProperty.setCamp(1);

        for (Map.Entry<Integer, Integer> buffItem : buffs.entrySet()) {
            extendProperty.addBuffData(Battle.PetBuffData.newBuilder().setBuffCfgId(buffItem.getKey()).setBuffCount(buffItem.getValue()));
        }
        addExtendProp(extendProperty.build());
    }

    @Override
    protected void initSuccess() {
    }

    @Override
    public int getPointId() {
        return 0;
    }
}
