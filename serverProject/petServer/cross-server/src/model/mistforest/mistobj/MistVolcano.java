package model.mistforest.mistobj;

import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import model.mistforest.MistConst;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;

public class MistVolcano extends MistObject {
    protected long eruptTime;

    public MistVolcano(MistRoom room, int objType) {
        super(room, objType);
    }

    public void generateVolcanoStone() {
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject posCfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (null == posCfg) {
            return;
        }
        if (null == posCfg.getSlaveobjposlist()) {
            return;
        }
        int length = posCfg.getSlaveobjposlist().length;
        if (length <= 0) {
            return;
        }
        int rand = RandomUtils.nextInt(length);
        int[] posData = posCfg.getSlaveobjposlist()[rand];
        if (null == posData || posData.length < 2) {
            return;
        }
        MistVolcanoStone mistVolcanoStone = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_VolcanoStone_VALUE);
        mistVolcanoStone.initByMaster(this);
        mistVolcanoStone.afterInit(posData, null);

        getRoom().getWorldMap().objFirstEnter(mistVolcanoStone);
    }

    protected void volcanoErupt(long curTime) {
        if (!isAlive()) {
            return;
        }
        if (eruptTime > curTime) {
            return;
        }
        eruptTime = curTime + getAttribute(MistUnitPropTypeEnum.MUPT_VolcanoEruptInterval_VALUE);
        getBufMachine().addBuff(MistConst.MistVolcanoBuffId, this, null);
    }

    @Override
    public void onTick(long curTime) {
        volcanoErupt(curTime);
        super.onTick(curTime);
    }
}
