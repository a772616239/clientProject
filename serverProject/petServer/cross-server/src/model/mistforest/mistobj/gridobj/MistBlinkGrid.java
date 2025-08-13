package model.mistforest.mistobj.gridobj;

import common.GlobalTick;
import java.util.HashMap;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.cache.MistRoomCache;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import util.TimeUtil;

public class MistBlinkGrid extends MistGridObj {

    public MistBlinkGrid(MistRoom room, int objType) {
        super(room, objType);
    }


    @Override
    public void dead() {
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
        HashMap<Integer, Long> paramMap = new HashMap<>();
        long level = getAttribute(MistUnitPropTypeEnum.MUPT_MazeAreaLevel_VALUE);
        long deltaLevel = MistRoomCache.getInstance().getMazeDataManager().getTransDeltaLevel((int) level, getPos().build());
        paramMap.put(MistTriggerParamType.BlinkGridLevel, level);
        paramMap.put(MistTriggerParamType.BlinkGridTransLevel, deltaLevel);

        long toward = MistRoomCache.getInstance().getMazeDataManager().getTransToward((int) level, getPos().build());
        paramMap.put(MistTriggerParamType.BlinkGridTransToward, toward);
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchBlinkGrid, fighter, paramMap);
    }
}
