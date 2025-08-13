package model.mistforest.mistobj.rewardobj;

import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import cfg.MistSealBoxConfig;
import cfg.MistSealBoxConfigObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistSealColumn;
import model.mistforest.mistobj.activityboss.MistBornPosController;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.UnitMetadata;

public class MistSealBox extends MistCrystalBox {
    protected Set<Long> unsealFighters;

    protected MistBornPosController posController;

    public MistSealBox(MistRoom room, int objType) {
        super(room, objType);
        unsealFighters = new HashSet<>();
        posController = new MistBornPosController();
    }

    @Override
    public void clear() {
        super.clear();
        unsealFighters.clear();
        posController.clear();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        int complexBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject cfg = MistComboBornPosConfig.getById(complexBornPosId);
        if (cfg != null) {
            posController.init(cfg.getSlaveobjposlist());
        }
        generateColumn();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_SealBoxState_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        if (fighter != null && unsealFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
            UnitMetadata.Builder metaData = UnitMetadata.newBuilder();
            metaData.mergeFrom(super.getMetaData(fighter));
            metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_SealBoxState_VALUE).addValues(1l);
            return metaData.build();
        } else {
            return super.getMetaData(fighter);
        }
    }

    @Override
    public void reborn() {
        super.reborn();
        unsealFighters.clear();
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_SealBoxState_VALUE, 0l);

        generateColumn();
    }

    @Override
    public void clearSlaveObj() {
        super.clearSlaveObj();
        posController.resetEmptyPos();
    }

    @Override
    public void dead() {
        clearSlaveObj();
        super.dead();
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!unsealFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
            return;
        }
        super.beTouch(fighter);
    }

    protected void generateColumn() {
        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_SealBoxCfgId_VALUE);
        MistSealBoxConfigObject cfg = MistSealBoxConfig.getById(cfgId);
        if (cfg == null || cfg.getNeeditemlist() == null) {
            return;
        }
        List<Integer> tmpList = new ArrayList<>();
        for (int i = 0; i < cfg.getNeeditemlist().length; i++) {
            tmpList.add(cfg.getNeeditemlist()[i]);
        }
        Collections.shuffle(tmpList);
        int maxCount = Math.min(posController.getEmptyPosCount() + posController.getUsePosCount(), tmpList.size());
        int count = Math.min(maxCount, cfg.getColumncount());
        if (count <= 0) {
            return;
        }
        int complexBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        for (int i = 0; i < count; i++) {
            ProtoVector pos = posController.getAndUseEmptyPos();
            if (null == pos) {
                continue;
            }
            MistSealColumn sealColumn = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_SealColumn_VALUE);
            sealColumn.initByMaster(this);
            sealColumn.afterInit(new int[]{pos.getX(), pos.getY()}, null);
            sealColumn.setAttribute(MistUnitPropTypeEnum.MUPT_SealColumnSubmitRewardId_VALUE, tmpList.get(i));
            sealColumn.setAttribute(MistUnitPropTypeEnum.MUPT_SealBoxCfgId_VALUE, cfgId);
            sealColumn.setAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE, complexBornPosId); // 需在初始化位置之后设置

            addSlaveObj(sealColumn.getId());
            getRoom().getWorldMap().objFirstEnter(sealColumn);
        }
    }

    public void checkUnseal(MistFighter fighter) {
        if (unsealFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
            return;
        }
        MistSealColumn obj;
        boolean allUnseal = true;
        for (Long slaveObjId : slaveObjList) {
            obj = getRoom().getObjManager().getMistObj(slaveObjId);
            if (obj == null || !obj.isAlive()) {
                allUnseal = false;
                break;
            }
            if (!obj.checkUnseal(fighter)) {
                allUnseal = false;
                break;
            }
        }
        if (allUnseal) {
            unsealFighters.add(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
            addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_SealBoxState_VALUE, 1l);
        }
    }
}