package model.mistforest.mistobj;

import java.util.HashMap;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistDoor extends MistObject {
    public MistDoor(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter toucher) {
        long toLevel = getAttribute(MistUnitPropTypeEnum.MUPT_ExitToLevel_VALUE);
        long permitLevel = toucher.getAttribute(MistUnitPropTypeEnum.MUPT_PermitLevel_VALUE);
        if (toLevel > permitLevel) {
            return;
        }
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.DoorId, getId());
        toucher.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchBuilding, this, params);
    }

    public void dead() {
        room.getObjGenerator().removeInitMeta(id);
    }

    @Override
    public void onTick(long curTime) {
    }
}
