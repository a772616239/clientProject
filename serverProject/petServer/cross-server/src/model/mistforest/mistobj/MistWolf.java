package model.mistforest.mistobj;

import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.enmity.MistAttackEnmity;
import model.mistforest.room.entity.MistRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;

public class MistWolf extends MistObject {
    protected MistAttackEnmity enmity;

    public MistWolf(MistRoom room, int objType) {
        super(room, objType);
        enmity = new MistAttackEnmity(this);
    }

    @Override
    public void clear() {
        super.clear();
        enmity.clear();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        enmity.setAttackTimes((int) getAttribute(MistUnitPropTypeEnum.MUPT_WolfAttackTimes_VALUE));
    }

    @Override
    public void reborn() {
        super.reborn();
        enmity.setAttackTimes((int) getAttribute(MistUnitPropTypeEnum.MUPT_WolfAttackTimes_VALUE));
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        if (fighter == null || fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
            return;
        }
        if (enmity.getAttackTimes() <= 0) {
            return;
        }
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchWolf, fighter, null);
        getRoom().broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildMistTips(EnumMistTipsType.EMTT_CaughtByWolf_VALUE,
                fighter, null, (int)getAttribute(MistUnitPropTypeEnum.MUPT_ImageId_VALUE)), true);
        fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_TouchWolf, 0, 1);
    }

    @Override
    public void onTick(long curTime) {
        if (isAlive()) {
            enmity.onTick(curTime);
        }
        super.onTick(curTime);
    }
}
