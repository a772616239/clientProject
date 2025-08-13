package model.mistforest.mistobj;

import common.GlobalTick;
import java.util.HashSet;
import java.util.Set;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.map.grid.Grid;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;

public class MistVolcanoStone extends MistObject {
    protected Set<Long> hitPlayerIdSet;
    protected long landTime;

    public MistVolcanoStone(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void clear() {
        super.clear();
        if (hitPlayerIdSet != null) {
            hitPlayerIdSet.clear();
        }
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        setAttribute(MistUnitPropTypeEnum.MUPT_IsVolcanoStoneLanded_VALUE, 0l);
        landTime = GlobalTick.getInstance().getCurrentTime() + getAttribute(MistUnitPropTypeEnum.MUPT_VolcanoStoneLandTime_VALUE);
    }

    @Override
    public void initByMaster(MistObject obj) {
        setAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE, obj.getId());
        setAttribute(MistUnitPropTypeEnum.MUPT_VolcanoStoneLandTime_VALUE, obj.getAttribute(MistUnitPropTypeEnum.MUPT_VolcanoStoneLandTime_VALUE));
    }

    @Override
    public void reborn() {
        super.reborn();
        setAttribute(MistUnitPropTypeEnum.MUPT_IsVolcanoStoneLanded_VALUE, 0l);
        landTime = GlobalTick.getInstance().getCurrentTime() + getAttribute(MistUnitPropTypeEnum.MUPT_VolcanoStoneLandTime_VALUE);
        if (hitPlayerIdSet != null) {
            hitPlayerIdSet.clear();
        }
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!fighter.canBeTouch()) {
            return;
        }
        if (!isAlive()) {
            return;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsVolcanoStoneLanded_VALUE) <= 0) {
            return;
        }
        if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsLavaImmuneState_VALUE) > 0) {
            return;
        }
        long playerId = fighter.getAttribute(MistUnitTypeEnum.MUT_Player_VALUE);
        if (hitPlayerIdSet != null && hitPlayerIdSet.contains(playerId)) {
            return;
        }

        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchVolcanoStone, this, null);
        if (hitPlayerIdSet == null) {
            hitPlayerIdSet = new HashSet<>();
        }
        hitPlayerIdSet.add(playerId);
    }

    protected void updateLandState(long curTime) {
        if (!isAlive()) {
            return;
        }
        if (landTime <= 0 || landTime > curTime) {
            return;
        }
        landTime = 0;
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsVolcanoStoneLanded_VALUE) <= 0) {
            setAttribute(MistUnitPropTypeEnum.MUPT_IsVolcanoStoneLanded_VALUE, 1l);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_IsVolcanoStoneLanded_VALUE, 1l);
        }
    }

    protected void checkTouchPlayer() {
        if (!isAlive()) {
            return;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsVolcanoStoneLanded_VALUE) <= 0) {
            return;
        }
        Grid grid = getRoom().getWorldMap().getGridByPos(getPos().getX(), getPos().getY());
        if (grid == null || grid.getFighterMap() == null) {
            return;
        }
        for (MistFighter fighter : grid.getFighterMap().values()) {
            beTouch(fighter);
        }
    }

    @Override
    public void onTick(long curTime) {
        updateLandState(curTime);
        checkTouchPlayer();
        super.onTick(curTime);
    }
}
