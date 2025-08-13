package model.mistforest.mistobj;

import model.mistforest.enmity.MistEnmity;
import model.mistforest.enmity.MistEscapeEnmity;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistTaskTargetType;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistDeer extends MistObject {
    protected MistEnmity enmity;
    public MistDeer(MistRoom room, int objType) {
        super(room, objType);
        enmity = new MistEscapeEnmity(this);
    }

    @Override
    public void clear() {
        super.clear();
        enmity.clear();
    }

    public boolean canTouch(MistFighter fighter) {
        if (!isAlive()) {
            return false;
        }
        if (fighter.isBattling()) {
            return false;
        }
        if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public void beTouch(MistFighter fighter) {
        generatePrivateDropObj(fighter);
        fighter.getNpcTask().doNpcTask(MistTaskTargetType.MTTT_CatchDeer_VALUE, 1, 0);
        dead();
    }

    @Override
    public void onTick(long curTime) {
        if (isAlive()) {
            enmity.onTick(curTime);
        }
        super.onTick(curTime);
    }
}
