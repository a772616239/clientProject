package model.mistforest.mistobj.rewardobj;

import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import java.util.HashSet;
import java.util.Set;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistMagicCycle;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.UnitMetadata;
import protocol.RetCodeId.RetCodeEnum;

public class MistMagicCycleBox extends MistCrystalBox {
    protected Set<Long> unlockPlayers;
    public MistMagicCycleBox(MistRoom room, int objType) {
        super(room, objType);
        unlockPlayers = new HashSet<>();
    }

    @Override
    public void clear() {
        super.clear();
        unlockPlayers.clear();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        generateMagicCycle();
    }

    @Override
    public void reborn() {
        super.reborn();
        unlockPlayers.clear();
        generateMagicCycle();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_SealBoxState_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata metadata = super.getMetaData(fighter);
        UnitMetadata.Builder builder = metadata.toBuilder();
        if (isAlive()) {
            long state = 0; // 封闭
            if (fighter != null) {
                long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
                state = unlockPlayers.contains(playerId) ? 1 : 0;
            }
            builder.getPropertiesBuilder().addKeys(MistUnitPropTypeEnum.MUPT_SealBoxState).addValues(state);
        }
        return builder.build();
    }

    public void addUnlockPlayer(MistFighter fighter, long playerId) {
        unlockPlayers.add(playerId);
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_SealBoxState_VALUE, 1);
    }

    @Override
    public RetCodeEnum canTouch(MistFighter fighter, int curStamina) {
        if (!unlockPlayers.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
            return RetCodeEnum.RCE_MistForest_CanNotTouch;
        }
        return super.canTouch(fighter, curStamina);
    }

    protected void generateMagicCycle() {
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject cfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (cfg == null || cfg.getSlaveobjposlist() == null || cfg.getSlaveobjposlist().length <= 0) {
            return;
        }
        int[] posData = cfg.getSlaveobjposlist()[0];
        if (null == posData || posData.length < 2) {
            return;
        }
        MistMagicCycle magicCycle = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_MagicCycle_VALUE);
        magicCycle.initByMaster(this);
        magicCycle.afterInit(posData, null);
        magicCycle.setAttribute(MistUnitPropTypeEnum.MUPT_MagicCyclePlayConfig_VALUE, getAttribute(MistUnitPropTypeEnum.MUPT_MagicCyclePlayConfig_VALUE));
        magicCycle.initCycleFlag();

        addSlaveObj(magicCycle.getId());
        getRoom().getWorldMap().objFirstEnter(magicCycle);
    }
}
