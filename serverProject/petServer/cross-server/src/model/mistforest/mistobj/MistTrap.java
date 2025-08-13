package model.mistforest.mistobj;

import java.util.HashMap;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.room.entity.MistRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistTrap extends MistObject {
    public MistTrap(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter toucher) {
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.TrapId, getId());
        toucher.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchTrap, this, params);

        int trapType = (int) getAttribute(MistUnitPropTypeEnum.MUPT_TrapType_VALUE);
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                room.buildMistTips(EnumMistTipsType.EMTT_TrapTriggered_VALUE, toucher, this, trapType), true);
    }

    public void dead() {
        int ownerId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_CreatorId_VALUE);
        MistObject owner = room.getObjManager().getMistObj(ownerId);
        if (owner instanceof MistFighter) {
            MistFighter fighter = (MistFighter) owner;
            fighter.removeDropTrap(getId());
        }
        super.dead();
    }

    public void onTick(long curTime) {
        if (getAttribute(MistUnitPropTypeEnum.MUPT_Invisional_VALUE) <= 0 && curTime - createTimeStamp > 2000l) {
            setAttribute(MistUnitPropTypeEnum.MUPT_Invisional_VALUE, 1l);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_Invisional_VALUE, 1l);
        }
        super.onTick(curTime);
    }
}
