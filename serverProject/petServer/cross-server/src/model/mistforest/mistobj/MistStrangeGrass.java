package model.mistforest.mistobj;

import cfg.MistStrangeGrassConfig;
import cfg.MistStrangeGrassConfigObject;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;

public class MistStrangeGrass extends MistObject {
    public MistStrangeGrass(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        generateRewardObj();
        dead();
        fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_TouchStrangeGrassCluster, 0, 1);
    }

    protected void generateRewardObj() {
        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistStrangeGrassConfigObject cfg = MistStrangeGrassConfig.getById(cfgId);
        if (cfg == null) {
            return;
        }
        MistObject obj = getRoom().getObjManager().createObj(cfg.getGenerateobjtype());
        obj.addAttributes(cfg.getGenerateobjprop());
        obj.initByMaster(this);
        obj.afterInit(new int[]{getPos().getX(), getPos().getY()}, null);

        getRoom().getWorldMap().objFirstEnter(obj);
    }
}
