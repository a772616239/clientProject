package model.mistforest.mistobj;

import java.util.HashMap;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistLavaBadge extends MistObject {
    public MistLavaBadge(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!fighter.canBeTouch()) {
            return;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE) > 0) {
            return;
        }
        int needStamina = (int) getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
        if (needStamina > 0) {
            MistPlayer player = fighter.getOwnerPlayer();
            if (player == null) {
                return;
            }
            if (player.getMistStamina() < needStamina) {
                return;
            }
        }

        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.LavaBadgeId, getId());
        params.put(MistTriggerParamType.RemoveObjId, getId());
        params.put(MistTriggerParamType.ChangeStaminaVal, (long) needStamina);
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchLavaBadge, this, params);
    }
}
