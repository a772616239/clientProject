package model.mistforest.mistobj;

import model.mistforest.room.entity.MistRoom;

public class MistPosObj extends MistObject {
    public MistPosObj(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void initPos(int[] initialPos, int[] initialToward) {
        setInitPos(initialPos[0], initialPos[1]);
        setInitToward(initialToward[0], initialToward[1]);
    }

    @Override
    public void onTick(long curTime) {
    }
}
