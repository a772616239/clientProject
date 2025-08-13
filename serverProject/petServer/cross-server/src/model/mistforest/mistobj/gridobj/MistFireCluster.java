package model.mistforest.mistobj.gridobj;

import common.GlobalTick;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.map.grid.Grid;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.TimeUtil;

public class MistFireCluster extends MistGridObj {
    public MistFireCluster(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);

        setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime());
    }

    @Override
    public void reborn() {
        setAttribute(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Survival_VALUE);
        rebornChangeProp();
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Survival_VALUE);
        long lifeTime = getAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE);
        if (lifeTime > 0) {
            setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime() + lifeTime * TimeUtil.MS_IN_A_S);
        }
        setCreateTimeStamp(GlobalTick.getInstance().getCurrentTime());
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        Grid grid = room.getWorldMap().getGridByPos(getPos().getX(), getPos().getY());
        if (grid != null) {
            grid.setGridObj(this);
        }
    }

    @Override
    public void onPlayerEnter(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.EnterFireCluster, this, null);
        fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_TouchFifeCluster, 0, 1);
    }

    @Override
    public void onPlayerLeave(MistFighter fighter) {
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.LeaveFireCluster, this, null);
    }
}
