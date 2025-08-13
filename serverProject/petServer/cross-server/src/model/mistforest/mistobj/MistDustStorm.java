package model.mistforest.mistobj;

import model.mistforest.room.entity.MistRoom;

public class MistDustStorm extends MistGuardMonster {
    public MistDustStorm(MistRoom room, int objType) {
        super(room, objType);
    }

    protected void checkMasterAlive(long curTime) {
        if (isAlive()) {
            updateGuardState(curTime);
        }
    }
}
