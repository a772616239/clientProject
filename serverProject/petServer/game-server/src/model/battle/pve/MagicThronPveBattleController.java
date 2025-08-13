package model.battle.pve;

import cfg.FightMake;
import cfg.FightMakeObject;
import cfg.ShuraArenaBossConfig;
import cfg.ShuraArenaBossConfigObject;
import common.GameConst.EventType;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.magicthron.MagicThronManager;
import model.magicthron.dbcache.magicthronCache;
import model.magicthron.entity.magicthronEntity;
import protocol.Battle;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetBuffData;
import protocol.Battle.PetBuffData.Builder;
import protocol.Battle.SC_BattleResult;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MagicThronDB.DB_MagicThron;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

import java.util.Collections;
import java.util.List;

public class MagicThronPveBattleController extends AbstractPveBattleController {


    private int BattleType;
    private int areaId;
    private int difficult;


    //会根据fightMakId替换
    private final BattleSubTypeEnum battleSubType = BattleSubTypeEnum.BSTE_magicthron;

    private ShuraArenaBossConfigObject bossCfg;

    @Override
    public Battle.SC_EnterFight.Builder buildEnterBattleBuilder() {
        Battle.SC_EnterFight.Builder builder = super.buildEnterBattleBuilder();
        FightMakeObject cfg = FightMake.getById(builder.getFightMakeId());
        if (cfg != null) {
            builder.setSubTypeValue(cfg.getType());
        }
        builder.setMonsterDiffLevel(bossCfg.getPetlvincr() + GlobalData.getInstance().getWorldMapInfo().getPetLv());
        return builder;
    }

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        if (enterParams == null || enterParams.size() < 2) {
            return false;
        }
        try {
            areaId = Integer.parseInt(enterParams.get(0));
            difficult = Integer.parseInt(enterParams.get(1));
            BattleType = Integer.parseInt(enterParams.get(2));
        } catch (Exception ex) {
            LogUtil.printStackTrace(ex);
            return false;
        }
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        magicthronEntity player = magicthronCache.getByIdx(getPlayerIdx());
        if (player == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        bossCfg = MagicThronManager.getInstance().getBossCfgByAreaAndDifficult(areaId, difficult);
        if (bossCfg == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        Integer areaId = MagicThronManager.getInstance().findPlayerArea(getPlayerIdx());
        if (areaId == null) {
            return RetCodeEnum.RSE_ConfigNotExist;
        }

        if (bossCfg.getArea() != areaId) {
            return RetCodeEnum.RCE_Activity_MissionIsExpire;
        }

        DB_MagicThron.Builder magicThron = player.getInfoDB();
        if (magicThron.getBossTimesMap().getOrDefault(areaId, 0) > 0) {
            return RetCodeEnum.RCE_ChooseAreaNotMatch;
        }

        addFightParams(FightParamTypeEnum.FPTE_PM_BossDamage);

        addExtendProp(ExtendProperty.newBuilder().setCamp(1).addBuffData(getExCommonBuff()).build());

       // setFightMakeId(MagicThronManager.getInstance().getFightMakeId(areaId, difficult));
        setFightMakeId(311002);

        return RetCodeEnum.RCE_Success;
    }

    private Builder getExCommonBuff() {
        Builder dataBuilder = PetBuffData.newBuilder();
        dataBuilder.setBuffCount(1);
        dataBuilder.setBuffCfgId(MagicThronManager.getInstance().getCommonBuff());
        return dataBuilder;
    }


    @Override
    protected void initSuccess() {

    }

    @Override
    public int getPointId() {
        return 0;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        if (BattleType == 1) {//挑战位可以模拟
            return;
        }

        magicthronEntity player = magicthronCache.getByIdx(getPlayerIdx());
        if (player == null) {
            return;
        }

        long damage = BattleUtil.getFightParamsValue(realResult.getFightParamsList(), FightParamTypeEnum.FPTE_PM_BossDamage);

        long finalDamage = (long) (damage * (bossCfg.getScoreaddition() / 1000.0));

        SyncExecuteFunction.executeConsumer(player, e -> {
            DB_MagicThron.Builder builder = player.getInfoDB();
            builder.putBossTimes(areaId, builder.getBossTimesMap().getOrDefault(areaId, 0) + 1);
            builder.setLastDamage(finalDamage);
            if (builder.getMaxDamageOnce() < finalDamage) {
                builder.setMaxDamageOnce(finalDamage);
            }
            builder.setCumuDamage(builder.getCumuDamage() + finalDamage);
        });

        player.sendMagicUpdate();

        saveBattleRecord();
    }

    private void saveBattleRecord() {
        Event event1 = Event.valueOf(EventType.ET_MagicThronRecord, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event1.pushParam(getPlayerIdx(), areaId, difficult, getBattleId(), getFightMakeId(), GlobalTick.getInstance().getCurrentTime(), 0);
        EventManager.getInstance().dispatchEvent(event1);
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return battleSubType;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_MAGICTHRON;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_MagicThron;
    }

    @Override
    public EnumFunction getFunctionEnum() {
        return EnumFunction.MagicThron;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        return Collections.emptyList();
    }

    @Override
    public boolean checkSpeedUp(CS_BattleResult clientResult, long realBattleTime) {
        if (isSkipBattle()) {
            return true;
        }
        return super.checkSpeedUp(clientResult, realBattleTime);
    }

    @Override
    public boolean checkRealResultSpeedUp(CS_BattleResult realResult, long realBattleTime) {
        if (isSkipBattle()) {
            return true;
        }
        return super.checkRealResultSpeedUp(realResult, realBattleTime);
    }
}
