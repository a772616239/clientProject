package model.mistforest.mistobj.activityboss;

import cfg.CrossConstConfig;
import common.GameConst;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.ServerTransfer.EnumMistPveBattleType;
import util.TimeUtil;

public class MistManEaterMonster extends MistObject {
    long recoverTime;

    public MistManEaterMonster(MistRoom room, int objType) {
        super(room, objType);
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
        if (curHp <= 0) {
            dead();
        }
    }

    @Override
    public void dead() {
        super.dead();
        if (getRebornTime() <= 0) {
            MistBossManEater boss = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
            if (null != boss) {
                boss.getPosController().returnUsedPos(getInitPos().build());
            }
        }
    }

    @Override
    public void removeFromMaster() {
        if (getRebornTime() <= 0) {
            MistBossManEater obj = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
            if (null != obj) {
                obj.removeSlaveId(getId());
            }
        }
    }

    protected void recoverBoss(long curTime) {
        if (!isAlive()) {
            return;
        }

        if (recoverTime > curTime) {
            return;
        }
        if (recoverTime > 0) {
            MistBossManEater boss = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
            if (null != boss && boss.isAlive()) {
                long maxHp = boss.getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
                long remainHp = boss.getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
                if (maxHp > remainHp) {
                    remainHp = Math.min(maxHp, remainHp + maxHp * CrossConstConfig.getById(GameConst.ConfigId).getManeatermonsterrecoverhprate() / 1000);
                    boss.setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, remainHp);
                    boss.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, remainHp);
                }
            }
        }
        recoverTime = curTime + CrossConstConfig.getById(GameConst.ConfigId).getManeatermonsterrecoverhpinterval() * TimeUtil.MS_IN_A_S;
    }

    @Override
    public void onTick(long curTime) {
        super.onTick(curTime);
        recoverBoss(curTime);
    }
}
