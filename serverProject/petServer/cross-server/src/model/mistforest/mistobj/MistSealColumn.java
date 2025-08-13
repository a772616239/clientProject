package model.mistforest.mistobj;

import cfg.MistSealBoxConfig;
import cfg.MistSealBoxConfigObject;
import common.GameConst.EventType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import model.mistforest.MistConst;
import model.mistforest.mistobj.rewardobj.MistSealBox;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.UnitMetadata;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;

public class MistSealColumn extends MistObject {
    protected Set<Long> fighterSubmitData;

    public MistSealColumn(MistRoom room, int objType) {
        super(room, objType);
        fighterSubmitData = new HashSet<>();
    }

    @Override
    public void clear() {
        super.clear();
        fighterSubmitData.clear();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_SealColumnState_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        if (fighter != null && fighterSubmitData.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
            UnitMetadata.Builder metaData = UnitMetadata.newBuilder();
            metaData.mergeFrom(super.getMetaData(fighter));
            metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_SealColumnState_VALUE).addValues(1l);
            return metaData.build();
        } else {
            return super.getMetaData(fighter);
        }
    }

    public boolean checkUnseal(MistFighter fighter) {
        return fighterSubmitData.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
    }

    public RetCodeEnum submitJewelry(MistFighter fighter, int rewardId) {
        if (!isAlive()) {
            return RetCodeEnum.RCE_MistForest_CanNotTouch;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return RetCodeEnum.RCE_Mist_PlayerNotFound;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (fighterSubmitData.contains(playerId)) {
            return RetCodeEnum.RCE_MistForest_AlreadySubmitJewelry;
        }
        MistSealBox sealBox = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
        if (sealBox == null || !sealBox.isAlive()) {
            return RetCodeEnum.RCE_MistForest_NotFoundSealBox;
        }
        Event event = Event.valueOf(EventType.ET_ConsumeLootPackReward, getRoom(), player);
        event.pushParam(rewardId, 1);
        EventManager.getInstance().dispatchEvent(event);

        int needRewardId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_SealColumnSubmitRewardId_VALUE);
        if (needRewardId == rewardId) {
            fighterSubmitData.add(playerId);
            addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_SealColumnState_VALUE, 1l);

            sealBox.checkUnseal(fighter);
            return RetCodeEnum.RCE_Success;
        } else {
            int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_SealBoxCfgId_VALUE);
            MistSealBoxConfigObject cfg = MistSealBoxConfig.getById(cfgId);
            if (cfg != null) {
                Map<Integer, Integer> rewardMap = MistConst.buildCommonRewardMap(cfg.getFailedsubmitreward(), EnumMistRuleKind.EMRK_Common_VALUE, player.getLevel());
                if (rewardMap != null) {
                    Event rewardEvent = Event.valueOf(EventType.ET_GainMistCarryReward, getRoom(), player);
                    rewardEvent.pushParam(rewardMap, false);
                    EventManager.getInstance().dispatchEvent(rewardEvent);
                }
            }
            return RetCodeEnum.RCE_MistForest_JewelryIdNotMatch;
        }
    }
}
