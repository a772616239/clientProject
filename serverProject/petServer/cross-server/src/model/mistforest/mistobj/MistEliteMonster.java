package model.mistforest.mistobj;

import cfg.GameConfig;
import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import cfg.MistEliteMonsterConfig;
import cfg.MistEliteMonsterConfigObject;
import common.GameConst;
import common.GameConst.EventType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.mistforest.MistConst;
import model.mistforest.mistobj.rewardobj.MistRewardObj;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_ExplodeReward;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.ServerTransfer.EnumMistPveBattleType;
import protocol.TargetSystem.TargetTypeEnum;
import server.event.Event;
import server.event.EventManager;
import util.LogUtil;

public class MistEliteMonster extends MistObject {
    protected Set<Long> fightedPlayers;

    public MistEliteMonster(MistRoom room, int objType) {
        super(room, objType);
        fightedPlayers = new HashSet<>();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        long configId = getAttribute(MistUnitPropTypeEnum.MUPT_ImageId_VALUE);
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildMistTips(EnumMistTipsType.EMTT_EliteMonsterAppear_VALUE, null, null, configId), true);

        if (getAttribute(MistUnitPropTypeEnum.MUPT_NewbieTaskId_VALUE) == 0) {
            generateEliteDoorKeeper();
        }
    }

    @Override
    public void initRebornTime() {
        int rebornTime = (int) getAttribute(MistUnitPropTypeEnum.MUPT_RebornTime_VALUE);
        if (rebornTime > 0) {
            rebornTime = Math.max(MistConst.MistDelayRemoveTime, rebornTime);
        }
        setRebornTime(rebornTime);
    }

    @Override
    public void reborn() {
        super.reborn();
        fightedPlayers.clear();
    }

    @Override
    public void dead() {
        super.dead();
        clearSlaveObj();
        long configId = getAttribute(MistUnitPropTypeEnum.MUPT_ImageId_VALUE);
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildMistTips(EnumMistTipsType.EMTT_EliteMonsterDisappear_VALUE, null, null, configId), true);
    }

    public void beKilled() {
        super.dead();
        clearSlaveObj();
        long configId = getAttribute(MistUnitPropTypeEnum.MUPT_ImageId_VALUE);
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildMistTips(EnumMistTipsType.EMTT_EliteMonsterDead_VALUE, null, null, configId), true);
    }

    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        if (fighter.isBattling()) {
            return;
        }
        long visibleId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visibleId > 0 && visibleId != fighter.getId()) {
            return;
        }
        fighter.enterPveBattle(EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE, this);
    }

    public void settleBattle(MistFighter fighter, boolean isWinner, long damage) {
        if (!isAlive()) {
            return;
        }

        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (playerId > 0) {
            fightedPlayers.add(playerId);
        }
        long curHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
//        damage = curHp;
        curHp = Math.max(0, curHp - damage);
        setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        if (curHp > 0) {
            if (!isWinner) {
                Long posData = MistConst.buildComboRebornPos((int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE));
                fighter.changeFighterPos(posData);
            }
            gainSingleBattleReward(fighter, false);
            return;
        }
        long newbieTaskId = getAttribute(MistUnitPropTypeEnum.MUPT_NewbieTaskId_VALUE);
        long visiblePlayerUnitId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (newbieTaskId > 0 && visiblePlayerUnitId == fighter.getId()) {
            fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_BeatMistNewbieEliteMonster, 0, 1);
        }
        gainSingleBattleReward(fighter, true);
        generateRewardObj();
        beKilled();
    }

    protected void generateEliteDoorKeeper() {
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject posCfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (null == posCfg) {
            return;
        }
        if (null == posCfg.getSlaveobjposlist()) {
            return;
        }
        int length = posCfg.getSlaveobjposlist().length;
        if (length <= 0) {
            return;
        }
        int rand = RandomUtils.nextInt(length);
        int[] posData = posCfg.getSlaveobjposlist()[rand];
        if (null == posData || posData.length < 2) {
            return;
        }
        MistEliteDoorKeeper doorKeeper = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_EliteDoorKeeper_VALUE);
        doorKeeper.initByMaster(this);
        if (posData.length > 2 && posData[2] > 0) {
            doorKeeper.setAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE, posData[2]);
        }
        doorKeeper.afterInit(posData, null);

        addSlaveObj(doorKeeper.getId());
        getRoom().getWorldMap().objFirstEnter(doorKeeper);
    }

    protected void generateRewardObj() {
        int fightCfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE);
        List<MistRewardObj> rewardObjs = getRoom().buildMonsterBattleRewardObj(null, fightCfgId, getPos());
        if (rewardObjs == null) {
            return;
        }

        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder().setCMDType(MistBattleCmdEnum.MBC_ExplodeReward);
        BattleCMD_ExplodeReward.Builder dropRewardCmd = BattleCMD_ExplodeReward.newBuilder();
        dropRewardCmd.setExplodePos(getPos());
        MistObject visibleObj = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE));
        for (MistRewardObj rewardObj : rewardObjs) {
            rewardObj.addQualifiedPlayers(fightedPlayers);
            if (visibleObj != null) {
                rewardObj.setAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, visibleObj.getId());
            }
            dropRewardCmd.addDropObjs(rewardObj.getMetaData(null));
        }
        cmdBuilder.setCMDContent(dropRewardCmd.build().toByteString());
        battleCmdList.addCMDList(cmdBuilder);
    }

    protected void gainSingleBattleReward(MistFighter fighter, boolean mustGainReward) {
        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistEliteMonsterConfigObject cfg = MistEliteMonsterConfig.getById(cfgId);
        if (cfg == null) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }

        Map<Integer, Integer> rewardMap = null;
        if (mustGainReward) {
            rewardMap = MistConst.buildCommonRewardMap(cfg.getMustgainreward(), getRoom().getMistRule(), player.getLevel());
        } else  {
            int playerRewardTimes = player.getEliteMonsterRewardTimes();
            if (playerRewardTimes >= GameConfig.getById(GameConst.ConfigId).getDailyelitemonsterrewradtimes()) {
                 return;
            }
            int[][] rewardData = cfg.getRadnombattlereward();
            if (rewardData == null || rewardData.length <=0) {
                return;
            }
            for (int i = 0; i < rewardData.length; i++) {
                if (rewardData[i] == null || rewardData[i].length < 3) {
                    continue;
                }
                if (RandomUtils.nextInt(1000) > rewardData[i][0]) {
                    continue;
                }
                if (rewardMap == null) {
                    rewardMap = new HashMap<>();
                }
                rewardMap.put(rewardData[i][1], rewardData[i][2]);
            }
            if (rewardMap != null) {
                Event event = Event.valueOf(EventType.ET_UpdateEliteMonsterRewardTimes, room, player);
                event.pushParam(++playerRewardTimes);
                EventManager.getInstance().dispatchEvent(event);
            }
        }
        if (rewardMap != null) {
            Event event = Event.valueOf(EventType.ET_GainMistCarryReward, room, player);
            event.pushParam(rewardMap, false);
            EventManager.getInstance().dispatchEvent(event);
        }

    }
}
