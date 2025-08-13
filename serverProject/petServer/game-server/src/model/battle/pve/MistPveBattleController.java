package model.battle.pve;

import cfg.GameConfig;
import common.GameConst;
import java.util.List;
import model.battle.AbstractPveBattleController;
import model.mistforest.MistConst;
import model.warpServer.crossServer.CrossServerManager;
import platform.logs.LogService;
import platform.logs.entity.FightWithMistBossLog;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetBuffData;
import protocol.Battle.SC_BattleResult;
import protocol.Battle.SC_EnterFight;
import protocol.BattleMono.FightParamDict;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.EnumMistPveBattleType;
import protocol.ServerTransfer.GS_CS_MistPveBattleResult;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;

/**
 * @author huhan
 * @date 2020/04/27
 */
public class MistPveBattleController extends AbstractPveBattleController {

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        return RetCodeEnum.RCE_Success;
    }

    @Override
    protected void initSuccess() {

    }

    @Override
    public int getPointId() {
        return 0;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        return null;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        GS_CS_MistPveBattleResult.Builder mistPveResult = GS_CS_MistPveBattleResult.newBuilder();
        mistPveResult.setIdx(getPlayerIdx());
        mistPveResult.setIsWinner(realResult.getWinnerCamp() == getCamp());

        int pveType = getIntEnterParam("pveType");
        mistPveResult.setPveTypeValue(pveType);
        mistPveResult.addAllRemainPetData(realResult.getRemainPetList());
        for (FightParamDict param : realResult.getFightParamsList()) {
            if (param.getKey() == FightParamTypeEnum.FPTE_PM_BossDamage && param.getValue() > 0) {
                mistPveResult.setDamage(param.getValue());
                break;
            }
        }

        //目标：迷雾深林胜利
        if (mistPveResult.getIsWinner()) {
            EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_CumuMistBattleVictory, 1, 0);
            //目标：累积击杀x次迷雾深林boss
            if (mistPveResult.getPveType() == EnumMistPveBattleType.EMPBT_BossBattle) {
                EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_CumuMistKillBoss, 1, 0);
                EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_MistSeasonTask_KillBossCount, 1, 0);
            }
            if (mistPveResult.getPveType() == EnumMistPveBattleType.EMPBT_MonsterBattle) {
                EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TTE_Mist_CumuKillMonster, 1,
                        MistConst.getMonsterType(getIntEnterParam("monsterCfgId")));
            }

        }
        if (mistPveResult.getPveType() == EnumMistPveBattleType.EMPBT_BossBattle) {
            LogService.getInstance().submit(new FightWithMistBossLog(getPlayerIdx(), mistPveResult.getIsWinner()));
        }

        // 后发送,保证顺序
        CrossServerManager.getInstance().sendMsgToMistForest(
                getPlayerIdx(), MsgIdEnum.GS_CS_MistPveBattleResult_VALUE, mistPveResult, false);
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_MistForest;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MistForest;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Common;
    }

    @Override
    public SC_EnterFight.Builder buildEnterBattleBuilder() {
        SC_EnterFight.Builder builder = super.buildEnterBattleBuilder();
        // 迷雾森林pve附件buff
        int[][] buffList = GameConfig.getById(GameConst.CONFIG_ID).getMistpvemonsterbuff();
        if (buffList != null && buffList.length > 0) {
            ExtendProperty.Builder extPropBuilder = ExtendProperty.newBuilder();
            extPropBuilder.setCamp(2);
            for (int i = 0; i < buffList.length; i++) {
                if (buffList[i] == null || buffList[i].length < 2) {
                    continue;
                }

                PetBuffData.Builder buffBuilder = PetBuffData.newBuilder();
                buffBuilder.setBuffCfgId(buffList[i][0]);
                buffBuilder.setBuffCount(buffList[i][1]);
                extPropBuilder.addBuffData(buffBuilder);
            }
            builder.addExtendProp(extPropBuilder);
        }
        return builder;
    }
}
