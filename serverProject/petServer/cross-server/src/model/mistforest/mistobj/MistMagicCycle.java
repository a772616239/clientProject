package model.mistforest.mistobj;

import cfg.MistMagicCycleConfig;
import cfg.MistMagicCycleConfigObject;
import java.util.HashMap;
import java.util.Map;
import model.mistforest.mistobj.rewardobj.MistMagicCycleBox;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.UnitMetadata;

public class MistMagicCycle extends MistObject {
    protected Map<Long, Long> playerLightStateFlag;
    protected long rewardFlag; // 提前算好解密结果

    public MistMagicCycle(MistRoom room, int objType) {
        super(room, objType);
        playerLightStateFlag = new HashMap<>();
    }

    @Override
    public void clear() {
        super.clear();
        playerLightStateFlag.clear();
    }

    @Override
    public void reborn() {
        super.reborn();
        playerLightStateFlag.clear();
        initCycleFlag();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_MagicLightFlag_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata metadata = super.getMetaData(fighter);
        UnitMetadata.Builder builder = metadata.toBuilder();
        if (isAlive() && fighter != null) {
            long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
            Long flagObj = playerLightStateFlag.get(playerId);
            long flag = flagObj != null ? flagObj : getAttribute(MistUnitPropTypeEnum.MUPT_MagicLightFlag_VALUE);
            builder.getPropertiesBuilder().addKeys(MistUnitPropTypeEnum.MUPT_MagicLightFlag).addValues(flag);
        }
        return builder.build();
    }

    public void changeCycleFlag(MistFighter fighter, int index) {
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        Long flagObj = playerLightStateFlag.get(playerId);
        long flag = flagObj != null ? flagObj : getAttribute(MistUnitPropTypeEnum.MUPT_MagicLightFlag_VALUE);
        if (flag == rewardFlag) {
            return;
        }
        flag = operateCycleFlag(flag, index);
        playerLightStateFlag.put(playerId, flag);
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_MagicLightFlag_VALUE, flag);
        if (flag == rewardFlag) {
            unlockRewardBox(fighter, playerId);
        }
    }

    protected void unlockRewardBox(MistFighter fighter, long playerId) {
        long masterId = getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE);
        MistMagicCycleBox masterBox = getRoom().getObjManager().getMistObj(masterId);
        if (masterBox == null) {
            return;
        }
        masterBox.addUnlockPlayer(fighter, playerId);
    }

    protected long operateCycleFlag(long playerFlag, int index) {
        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_MagicCyclePlayConfig_VALUE);
        MistMagicCycleConfigObject cfg = MistMagicCycleConfig.getById(cfgId);
        if (cfg == null) {
            return 0;
        }
        if (cfg.getCannotlightflag() == null || cfg.getOperatedata() == null) {
            return 0;
        }
        for (int i = 0; i < cfg.getOperatedata().length; i++) {
            if (cfg.getOperatedata()[i] == null || cfg.getOperatedata()[i].length < 4) {
                continue;
            }
            if (cfg.getOperatedata()[i][0] == index) {
                if (cfg.getOperatedata()[i][1] > 0) {
                    playerFlag ^= 1 << (cfg.getOperatedata()[i][1] - 1);
                }
                if (cfg.getOperatedata()[i][2] > 0) {
                    playerFlag ^= 1 << (cfg.getOperatedata()[i][2] - 1);
                }
                if (cfg.getOperatedata()[i][3] > 0) {
                    playerFlag &= ~(1 << (cfg.getOperatedata()[i][3] - 1));
                }
                break;
            }
        }
        return playerFlag;
    }

    public void initCycleFlag() {
        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_MagicCyclePlayConfig_VALUE);
        MistMagicCycleConfigObject cfg = MistMagicCycleConfig.getById(cfgId);
        if (cfg == null) {
            return;
        }
        if (cfg.getCannotlightflag() == null || cfg.getLightnum() <= 0) {
            return;
        }
        for (int i = 0; i < cfg.getLightnum(); i++) {
            rewardFlag |= 1 << i;
        }

        long tmpFlag;
        long cannotLightFlag = 0;
        for (int i = 0; i < cfg.getCannotlightflag().length; i++) {
            if (cfg.getCannotlightflag()[i] <= 0) {
                continue;
            }
            tmpFlag = 1 << (cfg.getCannotlightflag()[i] - 1);
            cannotLightFlag |= tmpFlag;
            rewardFlag &= ~tmpFlag;
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_MagicCannotLightFlag_VALUE, cannotLightFlag);
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
