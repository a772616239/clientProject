package model.mistforest.mistobj.rewardobj;

import java.util.HashMap;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import util.LogUtil;

public class MistItem extends MistRewardObj {
    public MistItem(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter toucher) {
        if (!isQualifiedPlayer(toucher)) {
            return;
        }
        long visibleId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visibleId > 0 && visibleId != toucher.getId()) {
            return;
        }
        if (toucher.getSkillMachine().isItemSkillFull()) {
            LogUtil.debug("fighter touch item failed: skill full");
            return;
        }
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.ItemId, getId());
        toucher.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchItem, this, params);
    }

    @Override
    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
        super.dead();
    }
}
