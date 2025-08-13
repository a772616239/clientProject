package model.mistforest.mistobj.rewardobj;

import cfg.MistCommonRewardConfig;
import cfg.MistCommonRewardConfigObject;
import common.GameConst.EventType;
import java.util.HashMap;
import java.util.Map;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistUnitPropTypeEnum;
import server.event.Event;
import server.event.EventManager;

// 通用读条奖励对象
public class MistCommonRewardObj extends MistRewardObj {
    public MistCommonRewardObj(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        if (!fighter.canBeTouch()) {
            return;
        }
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.RemoveObjId, getId());
        params.put(MistTriggerParamType.CommonRewardObjId, getId());
        long needStamina = getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
        if (needStamina > 0) {
            MistPlayer player = fighter.getOwnerPlayerInSameRoom();
            if (player == null || player.getMistStamina() < needStamina) {
                return;
            }
            params.put(MistTriggerParamType.ChangeStaminaVal, needStamina);
        }
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchCommonRewardObj, this, params);
    }

    public void gainReward(MistFighter fighter) {
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistCommonRewardConfigObject cfg = MistCommonRewardConfig.getById(cfgId);
        if (cfg == null) {
            return;
        }
        Map<Integer, Integer> rewardMap = MistConst.buildCommonRewardMap(cfg.getCommonrewardlist(), EnumMistRuleKind.EMRK_Common_VALUE, player.getLevel());
        if (rewardMap != null) {
            Event event = Event.valueOf(EventType.ET_GainMistCarryReward, getRoom(), player);
            event.pushParam(rewardMap, false);
            EventManager.getInstance().dispatchEvent(event);
        }
    }
}
