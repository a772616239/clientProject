package model.mistforest.enmity;

import lombok.Getter;
import lombok.Setter;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistEnmityState;
import model.mistforest.mistobj.MistObject;
import protocol.MistForest.MistUnitPropTypeEnum;

@Getter
@Setter
public class MistAttackEnmity extends MistEnmity {
    protected int attackTimes;

    public MistAttackEnmity(MistObject obj) {
        super(obj);
    }

    @Override
    protected boolean checkTouchTargets() {
        boolean touchTarget = super.checkTouchTargets();
        if (touchTarget) {
            --attackTimes;
        }
        return touchTarget;
    }

    @Override
    protected void pursueTarget(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime, owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfPursueSpeed_VALUE));
            if (posChanged) {
                boolean touchPursueTarget = checkTouchTargets();
                if (touchPursueTarget && getAttackTimes()<=0) {
                    owner.dead();
                } else if (touchPursueTarget || !checkContinuePursue()) {
                    movePath = owner.getRoom().getWorldMap().findPath(astar, owner.getPos().build(), owner.getInitPos().build());
                    dangerousFighter = null;
                    setEnmityState(MistEnmityState.reback);
                    updateStateTime = curTime;
                    addMovePathCmd(false);
                } else {
                    if (isPathEmpty() || !movePath.get(movePath.size() - 1).equals(dangerousFighter.getPos())) {
                        movePath = owner.getRoom().getWorldMap().findPath(astar, owner.getPos().build(), dangerousFighter.getPos().build());
                    }
                    addMovePathCmd(false);
                }
            } else if (isPathEmpty()) {
                movePath = owner.getRoom().getWorldMap().findPath(astar, owner.getPos().build(), owner.getInitPos().build());
                dangerousFighter = null;
                setEnmityState(MistEnmityState.reback);
                updateStateTime = curTime;
                addMovePathCmd(false);
            }
            lastMoveTime = curTime;
        }
    }

    @Override
    public void onTick(long curTime) {
        if (owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfAttackTimes_VALUE) > 0 && attackTimes <= 0) {
            return;
        }
        super.onTick(curTime);
    }
}
