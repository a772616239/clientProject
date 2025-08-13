package model.mistforest.mistobj;

import common.GlobalTick;
import java.util.HashMap;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistGuardMonsterState;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.room.entity.MistRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistLavaLord extends MistGuardMonster {
    public MistLavaLord(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter fighter) {
        Long posData = MistConst.buildComboRebornPos((int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE));
        HashMap<Integer, Long> param = new HashMap<>();
        param.put(MistTriggerParamType.TranPosData, posData);
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchLavaLord, this, param);

        if (guardState != MistGuardMonsterState.arrest) {
            if (guardState == MistGuardMonsterState.patrol) {
                addMovePathCmd(true);
            }
            guardState = MistGuardMonsterState.arrest;
            updateStateTime = GlobalTick.getInstance().getCurrentTime();
        }
        int buffId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_GuardMonsterBuffId_VALUE);
        if (buffId > 0 && bufMachine.getBuff(buffId) == null) {
            bufMachine.addBuff(buffId,this, null);
        }
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                room.buildMistTips(EnumMistTipsType.EMTT_HitByLavaLord_VALUE, fighter, this, getAttribute(MistUnitPropTypeEnum.MUPT_ImageId_VALUE)), true);
    }

    protected void checkMasterAlive(long curTime) {
        if (isAlive()) {
            updateGuardState(curTime);
        }
    }
}
