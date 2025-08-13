package model.mistforest.mistobj.activityboss;

import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import model.mistforest.MistConst.MistActivityBossStage;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;

public class MistBossSlime extends MistActivityBoss {
    public MistBossSlime(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void changeToStage(int stage) {
        super.changeToStage(stage);
        if (stage == MistActivityBossStage.furyStage) {
            generateSlimeMonster();
        }
    }

    protected void generateSlimeMonster() {
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject posCfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (null == posCfg || null == posCfg.getSlaveobjposlist()) {
            return;
        }
        for (int i = 0; i < posCfg.getSlaveobjposlist().length; i++) {
            int[] posData = posCfg.getSlaveobjposlist()[i];
            if (null == posData || posData.length < 4) {
                continue;
            }
            MistSlimeMonster slimeMonster = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_SlimeMonster_VALUE);
            slimeMonster.setAttribute(MistUnitPropTypeEnum.MUPT_BossRebornPosCfgId_VALUE, getAttribute(MistUnitPropTypeEnum.MUPT_BossRebornPosCfgId_VALUE));
            slimeMonster.setAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE, posCfg.getSlaveobjposlist()[i][2]);
            slimeMonster.setAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE, posCfg.getSlaveobjposlist()[i][3]);
            slimeMonster.afterInit(posData, null);
            slimeMonster.initByMaster(this);

            addSlaveObj(slimeMonster.getId());
            getRoom().getWorldMap().objFirstEnter(slimeMonster);
        }
    }
}
