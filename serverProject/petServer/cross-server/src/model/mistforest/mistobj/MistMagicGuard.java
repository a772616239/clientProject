package model.mistforest.mistobj;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import model.mistforest.mistobj.rewardobj.MistMagicBox;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.EnumMistSubBoxType;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.UnitMetadata;
import protocol.ServerTransfer.EnumMistPveBattleType;

public class MistMagicGuard extends MistObject {
    protected Map<Long, Long> playerRemainHpData;

    public MistMagicGuard(MistRoom room, int objType) {
        super(room, objType);
        playerRemainHpData = new HashMap<>();
    }

    @Override
    public void clear() {
        super.clear();
        playerRemainHpData.clear();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_LifeState_VALUE
                || propType == MistUnitPropTypeEnum.MUPT_DecipheringColumnSwitch_VALUE
                || propType == MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata metaData = super.getMetaData(fighter);
        UnitMetadata.Builder builder = metaData.toBuilder();

        long remainHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
        if (fighter != null) {
            Long remainHpObj = playerRemainHpData.get(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
            if (remainHpObj != null) {
                remainHp = remainHpObj;
            }
        }
        if (remainHp <= 0) {
            builder.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_LifeState_VALUE).addValues(LifeStateEnum.LSE_Dead_VALUE);
        } else {
            builder.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE).addValues(remainHp);
        }
        return builder.build();
    }

    @Override
    public void initByMaster(MistObject obj) {
        super.initByMaster(obj);

        long fightCfgId = getAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE);
        if (fightCfgId > 0) {
            return;
        }
        fightCfgId = obj.getAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE);
        if (fightCfgId <= 0) {
            return;
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE, fightCfgId);
    }

    public void beTouch(MistFighter fighter) {
        if (fighter.isBattling()) {
            return;
        }
        if (!isAlive()) {
            return;
        }
        Long remainHp = playerRemainHpData.get(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
        if (remainHp != null && remainHp == 0) {
            return;
        }
        Collection<Integer> buffIdList = null;
        MistMagicBox magicBox = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
        if (magicBox != null) {
            buffIdList = magicBox.getPlayerExtBuffList(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
        }
        fighter.enterMagicGuardBattle(EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE, this, buffIdList);
    }

    public void settleBattle(MistFighter fighter, long damage) {
        if (!isAlive()) {
            return;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (playerId <= 0) {
            return;
        }
        long remainHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
        Long remainHpObj = playerRemainHpData.get(playerId);
        if (remainHpObj != null) {
            remainHp = remainHpObj;
        }
        remainHp = Math.max(0, remainHp - damage);
        playerRemainHpData.put(playerId, remainHp);
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, remainHp);
        if (remainHp > 0) {
            return;
        }
        decreaseMasterGuardCount(fighter);
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);
    }

    public void decreaseMasterGuardCount(MistFighter fighter) {
        long targetId = getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE);
        MistMagicBox box = getRoom().getObjManager().getMistObj(targetId);
        if (null == box || box.getAttribute(MistUnitPropTypeEnum.MUPT_SubBoxType_VALUE) != EnumMistSubBoxType.EMSBT_MagicGuardBox_VALUE) {
            return;
        }
        Collection<Integer> buffData = box.getPlayerExtBuffList(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
        if (buffData != null && buffData.size() > box.getInitMagicGardUnlockCount()) {
            return;
        }
        long guardCount = box.storePlayerBuffData(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), getId(), (int) getAttribute(MistUnitPropTypeEnum.MUPT_MagicGuardExtBuffID_VALUE));
        guardCount = Math.max(0, box.getInitMagicGardUnlockCount() - guardCount);
        box.addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_MagicGuardUnlockCount_VALUE, guardCount);
    }

    @Override
    public void onTick(long curTime) {
        if (isAlive()) {
            long masterId = getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE);
            MistObject masterObj = getRoom().getObjManager().getMistObj(masterId);
            if (null == masterObj || !masterObj.isAlive()) {
                dead();
            }
        }
        super.onTick(curTime);
    }
}
