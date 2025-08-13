package model.mistforest.mistobj.rewardobj;

import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import cfg.MistMagicGuardConfig;
import cfg.MistMagicGuardConfigObject;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistMagicGuard;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.UnitMetadata;
import protocol.RetCodeId.RetCodeEnum;
import util.TimeUtil;

public class MistMagicBox extends MistCrystalBox {
    protected Map<Long, Map<Long, Integer>> playerBuffData;
    protected long initMagicGardUnlockCount = 0;

    public MistMagicBox(MistRoom room, int objType) {
        super(room, objType);
        playerBuffData = new HashMap<>();
    }

    @Override
    public void clear() {
        playerBuffData .clear();
        super.clear();
    }
    public long getInitMagicGardUnlockCount() {
        return initMagicGardUnlockCount;
    }

    public void setInitMagicGardUnlockCount(long initMagicGardUnlockCount) {
        this.initMagicGardUnlockCount = initMagicGardUnlockCount;
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        setInitMagicGardUnlockCount(getAttribute(MistUnitPropTypeEnum.MUPT_MagicGuardUnlockCount_VALUE));
        createMagicGuard();
    }

    @Override
    public void reborn() {
        super.reborn();
        createMagicGuard();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_MagicGuardUnlockCount_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata metadata = super.getMetaData(fighter);
        UnitMetadata.Builder builder = metadata.toBuilder();
        if (isAlive()) {
            long count = getInitMagicGardUnlockCount();
            if (fighter != null) {
                Map<Long, Integer> playerBuffMap = playerBuffData.get(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
                count = playerBuffMap != null ? playerBuffMap.size() : 0;
                count = Math.max(0, getInitMagicGardUnlockCount() - count);
            }
            builder.getPropertiesBuilder().addKeys(MistUnitPropTypeEnum.MUPT_MagicGuardUnlockCount).addValues(count);
        }
        return builder.build();
    }

    public void createMagicGuard() {
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject cfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (null == cfg || null == cfg.getSlaveobjposlist()) {
            return;
        }
        int magicGuardCfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_MagicBoxConfig_VALUE);
        MistMagicGuardConfigObject magicGuardCfg = MistMagicGuardConfig.getById(magicGuardCfgId);
        if (null == magicGuardCfg || null == magicGuardCfg.getExtbufflist()) {
            return;
        }
        int count = Math.min((int) getInitMagicGardUnlockCount(), cfg.getSlaveobjposlist().length);
        if (magicGuardCfg.getExtbufflist().length < count) {
            return;
        }
        if (magicGuardCfg.getRewardconfigid() > 0) {
            setAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE, magicGuardCfg.getRewardconfigid());
            setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime() + magicGuardCfg.getLifetime() * TimeUtil.MS_IN_A_S);
        }
        if (count > 0) {
            List<Integer> randBuffList = new ArrayList<>();
            for (int i = 0; i < magicGuardCfg.getExtbufflist().length; i++) {
                randBuffList.add(i);
            }
            Collections.shuffle(randBuffList);
            for (int i = 0; i < count; i++) {
                int[] posData = cfg.getSlaveobjposlist()[i];
                if (null == posData || posData.length < 3) {
                    continue;
                }
                MistMagicGuard guard = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_MagicGuard_VALUE);
                guard.setAttribute(MistUnitPropTypeEnum.MUPT_MagicGuardExtBuffID_VALUE, magicGuardCfg.getExtbufflist()[i][0]);
                guard.setAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE, magicGuardCfg.getExtbufflist()[i][1]);
                guard.setAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE, posData[2]);
                guard.initByMaster(this);
                guard.afterInit(posData, null);

                addSlaveObj(guard.getId());
                getRoom().getWorldMap().objFirstEnter(guard);
            }
            setInitMagicGardUnlockCount(count);
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_MagicGuardUnlockCount_VALUE, count);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_MagicGuardUnlockCount_VALUE, count);
    }

    @Override
    public RetCodeEnum canTouch(MistFighter fighter, int curStamina) {
        Map<Long, Integer> buffData = playerBuffData.get(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
        if (buffData == null || buffData.size() < initMagicGardUnlockCount) {
            return RetCodeEnum.RCE_Mist_MagicBoxHasGuards;
        }
        return super.canTouch(fighter, curStamina);
    }

    public long storePlayerBuffData(long playerId, long magicGuardId, int buffId) {
        Map<Long, Integer> playerData = playerBuffData.get(playerId);
        if (playerData == null) {
            playerData = new HashMap<>();
            playerBuffData.put(playerId, playerData);
        }
        playerData.put(magicGuardId, buffId);
        return playerData.size();
    }

    public Collection<Integer> getPlayerExtBuffList(long playerId) {
        Map<Long, Integer> playerData = playerBuffData.get(playerId);
        if (playerData == null) {
            return null;
        }
        return playerData.values();
    }
}
