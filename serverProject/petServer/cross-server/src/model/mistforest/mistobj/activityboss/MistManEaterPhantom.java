package model.mistforest.mistobj.activityboss;

import cfg.CrossConstConfig;
import common.GameConst;
import model.mistforest.MistConst;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.ServerTransfer.EnumMistPveBattleType;

public class MistManEaterPhantom extends MistObject {
    public MistManEaterPhantom(MistRoom room, int objType) {
        super(room, objType);
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
    public void recoverHp(boolean init) {
        long maxHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
        if (maxHp > 0) {
            long rate = CrossConstConfig.getById(GameConst.ConfigId).getManeaterrebornhprate();
            long curHp = maxHp * rate / 1000;
            setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
            if (!init) {
                addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
            }
        }
    }

    public void beTouch(MistFighter fighter) {
        if (fighter.isBattling()) {
            return;
        }
        if (!isAlive()) {
            return;
        }
        fighter.enterPveBattle(EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE, this);
    }

    public void settleDamage(MistFighter fighter, long damage) {
        if (!isAlive()) {
            return;
        }
        long curHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
//        damage = curHp;
        curHp = Math.max(0, curHp - damage);
        setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        if (curHp > 0) {
            return;
        }
        dead();
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (null == player) {
            return;
        }
        getRoom().updateBossActivityRank(player, damage);
    }

    @Override
    public void dead() {
        MistBossManEater bossManEater = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
        if (null == bossManEater || !bossManEater.isAlive()) {
            setRebornTime(0);
            removeFromMaster();
            bossManEater.preDead();
            bossManEater.generateRewardObj();
        }
        super.dead();
    }

    @Override
    public void removeFromMaster() {
        if (getRebornTime() <= 0) {
            MistBossManEater obj = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
            if (null != obj) {
                obj.setPhantomObjId(0);
            }
        }
    }
}
