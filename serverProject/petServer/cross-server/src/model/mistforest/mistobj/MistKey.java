package model.mistforest.mistobj;

import java.util.HashMap;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistBossKeyState;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.room.entity.MistRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistKey extends MistObject {
    private long broadcastTime;

    public MistKey(MistRoom room, int objType) {
        super(room, objType);
    }

    public void init() {
        super.init();
        this.setDailyObj(true);
        room.getObjGenerator().addDailyObjCount(getType());
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildShowBossTimeCmd(MistBossKeyState.keyNotPicked), true);
    }

    @Override
    public void beTouch(MistFighter toucher) {
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.KeyId, getId());
        toucher.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchKey, this, params);
    }

    @Override
    public void dead() {
        super.dead();
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildShowBossTimeCmd(MistBossKeyState.keyNotBorn), true);
    }

    public boolean isKeyHide() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_WaitingBossState_VALUE) > 0;
    }

    @Override
    public void onTick(long curTime) {
        if (!isKeyHide() && broadcastTime < curTime) {
            broadcastTime = curTime + MistConst.MistKeyBroadcastTime;
            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildMistTips(EnumMistTipsType.EMTT_BossKeyAppear_VALUE, this, this), true);
        }
        super.onTick(curTime);
    }
}
