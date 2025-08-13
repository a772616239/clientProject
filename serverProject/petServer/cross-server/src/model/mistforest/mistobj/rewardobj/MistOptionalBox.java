package model.mistforest.mistobj.rewardobj;

import cfg.MistOptionalBoxConfig;
import cfg.MistOptionalBoxConfigObject;
import common.GameConst.EventType;
import common.GlobalData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_UpdateComplexProperty;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.ComplexPropTypeEnum;
import protocol.MistForest.ComplexPropertyValues;
import protocol.MistForest.EnumUpdateComplexPropType;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.UnitMetadata;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_ReqChangeMistStamina;
import server.event.Event;
import server.event.EventManager;
import util.LogUtil;

public class MistOptionalBox extends MistBaseBox {
    protected Map<Long, Set<Integer>> claimRewardData = new HashMap<>();

    public MistOptionalBox(MistRoom room, int objType) {
        super(room, objType);
        LogUtil.error("MistRoom[{}] level={} create optionalBox, stackData:{}",getRoom().getIdx(),getRoom().getLevel(),Thread.currentThread().getStackTrace());
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE;
    }

    @Override
    public void reborn() {
        super.reborn();
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE, getAttribute(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE));
        claimRewardData.clear();
        addClearClaimedOptionalBoxIndexCmd();
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata.Builder metaData = UnitMetadata.newBuilder();
        metaData.mergeFrom(super.getMetaData(fighter));
        long canChosenTimes = getAttribute(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE);
        if (canChosenTimes > 0) {
            canChosenTimes = Math.max(0, canChosenTimes - claimRewardData.size());
            metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE).addValues(canChosenTimes);
        }
        if (fighter != null && !claimRewardData.isEmpty()) {
            Set<Integer> claimedIndexSet = claimRewardData.get(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
            if (!CollectionUtils.isEmpty(claimedIndexSet)) {
                metaData.addComplexKeys(ComplexPropTypeEnum.CPTE_ClaimedOptionalBoxRewardIndex);
                ComplexPropertyValues.Builder builder = ComplexPropertyValues.newBuilder();
                for (Integer index : claimedIndexSet) {
                    builder.addValues(index);
                }
                metaData.addComplexValues(builder);
            }
        }
        return metaData.build();
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        long visibleId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visibleId > 0 && visibleId != fighter.getId()) {
            return;
        }
        long canChosenTimes = getAttribute(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE);
        if (canChosenTimes > 0 && canChosenTimes <= claimRewardData.size()) {
            return;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsBossActivityBox_VALUE) > 0) {
            MistPlayer player = fighter.getOwnerPlayerInSameRoom();
            if (player.getGainActivityBossBoxFlag()) {
                return;
            }
        }
        Set<Integer> claimedSet = claimRewardData.get(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
        if (!CollectionUtils.isEmpty(claimedSet)) {
            int rewardId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
            MistOptionalBoxConfigObject cfg = MistOptionalBoxConfig.getById(rewardId);
            if (cfg != null && cfg.getOptionallist().length <= claimedSet.size()) {
                return;
            }
        }
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.TreasureBoxId, getId());
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchOptionalBox, this, params);
    }

    public RetCodeEnum claimReward(MistFighter fighter, List<Integer> chooseIndexList) {
        if (!isAlive()) {
            return RetCodeEnum.RCE_Mist_BoxDisappear;
        }
        long visibleId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visibleId > 0 && visibleId != fighter.getId()) {
            return RetCodeEnum.RCE_MistForest_CanNotTouch;
        }
        if (CollectionUtils.isEmpty(chooseIndexList)) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        if (getId() != fighter.getAttribute(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE)) {
            return RetCodeEnum.RCE_Mist_InvalidClaimReward;
        }
        int rewardId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistOptionalBoxConfigObject cfg = MistOptionalBoxConfig.getById(rewardId);
        if (cfg == null || cfg.getOptionallist() == null) {
            return RetCodeEnum.RCE_Mist_CfgError;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return RetCodeEnum.RCE_Mist_PlayerNotFound;
        }
        long ownerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        int deltaStamina = 0;
        int stamina = player.getMistStamina();
        Map<Integer, Integer> reward = new HashMap<>();
        Set<Integer> claimedData = new HashSet<>();
        Set<Integer> plyClaimData = claimRewardData.get(ownerId);
        for (Integer index : chooseIndexList) {
            if (index < 0 || index >= cfg.getOptionallist().length) {
                continue;
            }
            if (cfg.getOptionallist()[index].length < 2) {
                continue;
            }
            if (plyClaimData != null && plyClaimData.contains(index)) {
                continue;
            }
            if (claimedData.contains(index)) {
                continue;
            }
            if (stamina < cfg.getOptionallist()[index][1]) {
                return RetCodeEnum.RCE_Mist_StaminaNotEnough;
            }
            deltaStamina += cfg.getOptionallist()[index][1];
            stamina -= cfg.getOptionallist()[index][1];
            reward.merge(cfg.getOptionallist()[index][0], 1, (oldVal, newVal) -> oldVal + newVal);
            claimedData.add(index);
        }

        Event event = Event.valueOf(EventType.ET_GainMistCarryReward, room, player);
        event.pushParam(reward, false);
        EventManager.getInstance().dispatchEvent(event);
        if (!player.isRobot()) {
            CS_GS_ReqChangeMistStamina.Builder builder = CS_GS_ReqChangeMistStamina.newBuilder();
            builder.setPlayerIdx(player.getIdx());
            builder.setChangeValue(-deltaStamina);
            GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE, builder);
        }
        addComplexPropertyCmd(player, claimedData);
        if (plyClaimData == null) {
            claimRewardData.put(ownerId, claimedData);
        } else {
            plyClaimData.addAll(claimedData);
        }

        fighter.setAttribute(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE, 0);
        fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE, 0);

        long canChosenTimes = getAttribute(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE);
        if (canChosenTimes > 0) {
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE, canChosenTimes - claimedData.size());
        }
        return RetCodeEnum.RCE_Success;
    }

    protected void addComplexPropertyCmd(MistPlayer player, Set<Integer> values) {
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder batCmdBuilder = BattleCmdData.newBuilder();
        batCmdBuilder.setCMDType(MistBattleCmdEnum.MBC_UpdateComplexProperty);
        BattleCMD_UpdateComplexProperty.Builder cmdBuilder = BattleCMD_UpdateComplexProperty.newBuilder();
        cmdBuilder.setTargetUnitID(getId());
        cmdBuilder.setUpdateType(EnumUpdateComplexPropType.EUCPT_Add);
        cmdBuilder.addComplexKeys(ComplexPropTypeEnum.CPTE_ClaimedOptionalBoxRewardIndex);

        ComplexPropertyValues.Builder valBuilder = ComplexPropertyValues.newBuilder();
        for (Integer val : values) {
            valBuilder.addValues(val);
        }
        cmdBuilder.addComplexValues(valBuilder);

        batCmdBuilder.setCMDContent(cmdBuilder.build().toByteString());
        builder.addCMDList(batCmdBuilder);
        player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE,  builder);
    }

    protected void addClearClaimedOptionalBoxIndexCmd() {
        BattleCmdData.Builder batCmdBuilder = BattleCmdData.newBuilder();
        batCmdBuilder.setCMDType(MistBattleCmdEnum.MBC_UpdateComplexProperty);
        BattleCMD_UpdateComplexProperty.Builder cmdBuilder = BattleCMD_UpdateComplexProperty.newBuilder();
        cmdBuilder.setTargetUnitID(getId());
        cmdBuilder.setUpdateType(EnumUpdateComplexPropType.EUCPT_All);
        cmdBuilder.addComplexKeys(ComplexPropTypeEnum.CPTE_ClaimedOptionalBoxRewardIndex);

        cmdBuilder.addComplexValues(ComplexPropertyValues.newBuilder());

        batCmdBuilder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(batCmdBuilder);
    }
}
