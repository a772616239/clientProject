package model.mistforest.mistobj;

import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistPoisonMushroom extends MistObject {
    public MistPoisonMushroom(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
//        getBufMachine().addBuff(Mist)
    }

    @Override
    public void beTouch(MistFighter toucher) {
        if (!isAlive()) {
            return;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsMushroomPoisonous_VALUE) <= 0) {
            return;
        }

    }
}
