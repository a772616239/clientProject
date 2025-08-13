package model.mistforest.mistobj;

import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.room.entity.MistRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistTipsType;

public class MistDriftSand extends MistObject {
    public MistDriftSand(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchDriftSand, fighter, null);
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                room.buildMistTips(EnumMistTipsType.EMTT_UnitTriggered_VALUE, fighter, this, getType()), true);
    }
}
