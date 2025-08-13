package model.mistforest.mistobj.gridobj;

import common.GlobalTick;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import util.TimeUtil;

public class MistTransGrid extends MistGridObj {
    public MistTransGrid(MistRoom room, int objType) {
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
    public void beTouch(MistFighter fighter) {
        if (fighter == null || !isAlive()) {
            return;
        }
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchTransGrid, fighter, null);
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                room.buildMistTips(EnumMistTipsType.EMTT_UnitTriggered_VALUE, fighter, this, getType()), true);
        dead();
    }
}
