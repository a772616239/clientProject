package model.mistforest.mistobj.rewardobj;

import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.mistforest.mistobj.MistDecipheringColumn;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.activityboss.MistBornPosController;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.UnitMetadata;

public class MistDecipheringBox extends MistCrystalBox {
    protected Set<Long> decipheredFighters;
    protected Map<Long, List<Long>> aroundColumnMap;
    protected MistBornPosController posController;

    public MistDecipheringBox(MistRoom room, int objType) {
        super(room, objType);
        decipheredFighters = new HashSet<>();
        aroundColumnMap = new HashMap<>();
        posController = new MistBornPosController();
    }

    @Override
    public void clear() {
        super.clear();
        decipheredFighters.clear();
        aroundColumnMap.clear();
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
        if (fighter != null && decipheredFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
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
        decipheredFighters.clear();
        aroundColumnMap.clear();
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
        if (!decipheredFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
            return;
        }
        super.beTouch(fighter);
    }

    protected void generateColumn() {
        int maxCount = posController.getEmptyPosCount() + posController.getUsePosCount();
        int count = maxCount;// Math.min(maxCount, cfg.getColumncount());
        if (count <= 0) {
            return;
        }
        int complexBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        List<Integer> tmpList = new ArrayList<>(count);
        tmpList.add(0);
        tmpList.add(1);
        for (int i = 0; i < count - 2; i++) {
            tmpList.add(RandomUtils.nextInt(2));
        }
        Collections.shuffle(tmpList);
        for (int i = 0; i < count; i++) {
            ProtoVector pos = posController.getAndUseEmptyPos();
            if (null == pos) {
                continue;
            }
            MistDecipheringColumn decipherColumn = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_DecipheringColumn_VALUE);
            decipherColumn.initByMaster(this);
            decipherColumn.afterInit(new int[]{pos.getX(), pos.getY()}, null);
            decipherColumn.setAttribute(MistUnitPropTypeEnum.MUPT_DecipheringColumnSwitch_VALUE, tmpList.get(i));
            decipherColumn.setAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE, complexBornPosId); // 需在初始化位置之后设置

            addSlaveObj(decipherColumn.getId());
            getRoom().getWorldMap().objFirstEnter(decipherColumn);
        }

        for (int i = 0; i < slaveObjList.size(); i++) {
            long slaveId = slaveObjList.get(i);
            List<Long> aroundList = aroundColumnMap.get(slaveId);
            if (aroundList == null) {
                aroundList = new ArrayList<>();
            }
            if (i-1 >= 0) {
                aroundList.add(slaveObjList.get(i-1));
            } else {
                aroundList.add(slaveObjList.get(slaveObjList.size() - 1));
            }
            if (i + 1 < slaveObjList.size()) {
                aroundList.add(slaveObjList.get(i+1));
            } else {
                aroundList.add(slaveObjList.get(0));
            }
            aroundColumnMap.put(slaveId, aroundList);
        }
    }

    public boolean canListColumn(MistFighter fighter) {
        return isAlive() && !decipheredFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
    }

    public void lightAroundColumn(MistFighter fighter, MistDecipheringColumn column) {
        List<Long> aroundList = aroundColumnMap.get(column.getId());
        if (aroundList == null) {
            return;
        }
        MistDecipheringColumn aroundColumn;
        for (Long columnId : aroundList) {
            aroundColumn = getRoom().getObjManager().getMistObj(columnId);
            if (aroundColumn == null) {
                continue;
            }
            aroundColumn.lightTheColumn(fighter);
        }
        checkAllDecipherColumn(fighter);
    }

    public void checkAllDecipherColumn(MistFighter fighter) {
        if (decipheredFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
            return;
        }
        MistDecipheringColumn column;
        boolean tmpRet;
        int lightingState = -1;
        for (long objId : slaveObjList) {
            column = getRoom().getObjManager().getMistObj(objId);
            if (column == null) {
                return;
            }
            tmpRet = column.getLightingState(fighter);
            if (lightingState == -1) {
                lightingState = tmpRet ? 1 : 0;
            } else {
                if (lightingState == 0 && tmpRet) {
                    return;
                } else if (lightingState == 1 && !tmpRet) {
                    return;
                }
            }
        }

        for (long objId : slaveObjList) {
            column = getRoom().getObjManager().getMistObj(objId);
            if (column == null) {
                continue;
            }
            column.addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_DecipheringColumnState_VALUE, 1l); // 关
        }

        decipheredFighters.add(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_SealBoxState_VALUE, 1l);
    }
}
