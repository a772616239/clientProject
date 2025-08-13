package model.mistforest.mistobj.activityboss;

import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import java.util.List;
import java.util.stream.Collectors;
import model.mistforest.MistConst.MistActivityBossStage;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;

public class MistBossGargoyle extends MistActivityBoss {
    public MistBossGargoyle(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (getAttribute(MistUnitPropTypeEnum.MUPT_ActivityBossStage_VALUE) == MistActivityBossStage.furyStage) {
            return;
        }
        super.beTouch(fighter);
    }

    @Override
    public void changeToStage(int stage) {
        super.changeToStage(stage);
        if (stage == MistActivityBossStage.furyStage) {
            generateGargoyleMonster();
        } else if (stage == MistActivityBossStage.weakStage){
            removeAllGargoyleMonster();
        }
    }

    protected void generateGargoyleMonster() {
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
        for (int i = 0; i < length; i++) {
            int[] posData = posCfg.getSlaveobjposlist()[i];
            if (null == posData || posData.length < 2) {
                continue;
            }
            MistGargoyleMonster monster = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_GargoyleMonster_VALUE);
            monster.initByMaster(this);
            if (rand == i) {
                monster.setAttribute(MistUnitPropTypeEnum.MUPT_IsTrulyStatue_VALUE, 1l);
            }
            monster.setAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE, 1);
            monster.setAttribute(MistUnitPropTypeEnum.MUPT_IsShowInMiniMap_VALUE, 1);
            monster.afterInit(posData, null);

            addSlaveObj(monster.getId());
            getRoom().getObjGenerator().addOverallObjId(monster.getId());
            monster.addCreateObjCmd();
        }
    }

    protected void removeAllGargoyleMonster() {
        if (!CollectionUtils.isEmpty(slaveObjList)) {
            List<Long> tmpList = slaveObjList.stream().collect(Collectors.toList());
            for (Long slaveId : tmpList) {
                MistObject obj = getRoom().getObjManager().getMistObj(slaveId);
                if (null == obj) {
                    continue;
                }
                obj.dead();
            }
        }
    }
}
