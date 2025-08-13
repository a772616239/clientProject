package model.mistforest.mistobj.gridobj;

import common.GlobalTick;
import java.util.Map;
import model.mistforest.MistConst;
import model.mistforest.map.grid.Grid;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import timetool.TimeHelper;
import util.LogUtil;
import util.TimeUtil;

public class MistBlockGrid extends MistGridObj {

    private int tryRebornTime; // 额外等待时间，保证刷新时间一致

    public MistBlockGrid(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void clear() {
        tryRebornTime = 0;
        super.clear();
    }

    @Override
    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);

        setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime());
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
    public boolean isGridBlock(MistFighter fighter) {
        return isAlive();
    }

    // 检查是否有玩家在格子上
    public boolean isAnyFighterOn() {
        Grid grid = getRoom().getWorldMap().getGridByPos(getPos().getX(), getPos().getY());
        if (grid == null) {
            return false;
        }
        Map<Long, MistFighter> fighterMap = grid.getFighterMap();
        return fighterMap != null && !fighterMap.isEmpty();
    }

    @Override
    public void onTick(long curTime) {
        updateBattleCmd();
        battleCmdList.clear();
        if (isAlive()) {
            long lifeTime = getAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE);
            if (lifeTime > 0 && curTime - createTimeStamp > lifeTime * TimeHelper.SEC) {
                dead();
            }
        } else {
            long deadTimeStamp = getDeadTimeStamp();
            if (rebornTime <= 0) {
                if (deadTimeStamp == 0 || curTime - deadTimeStamp >= TimeUtil.MS_IN_A_S * MistConst.MistDelayRemoveTime - 100) {
                    LogUtil.debug("remove obj id = " + getId());
                    room.getObjManager().removeObj(getId());
                    clear();
                }
            } else if (curTime - deadTimeStamp >= (tryRebornTime + rebornTime) * TimeUtil.MS_IN_A_S) {
                if (isAnyFighterOn()) {
                    tryRebornTime += rebornTime + getAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE);
                } else {
                    reborn();
                    tryRebornTime = 0;
                }
            }
        }
    }
}
