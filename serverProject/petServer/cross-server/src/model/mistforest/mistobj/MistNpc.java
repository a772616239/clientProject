package model.mistforest.mistobj;

import common.GlobalData;
import java.util.HashSet;
import java.util.Set;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.UnitMetadata;
import protocol.ServerTransfer.CS_GS_ReqChangeMistStamina;

public class MistNpc extends MistObject {
    protected Set<Long> acceptedTaskFighters;

    public MistNpc(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void clear() {
        super.clear();
        if (acceptedTaskFighters != null) {
            acceptedTaskFighters.clear();
        }
    }

    @Override
    public void reborn() {
        super.reborn();
        if (acceptedTaskFighters != null) {
            acceptedTaskFighters.clear();
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_CanAcceptNpcTask_VALUE, 1l);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_CanAcceptNpcTask_VALUE, 1l);
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        setAttribute(MistUnitPropTypeEnum.MUPT_CanAcceptNpcTask_VALUE, 1l);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_CanAcceptNpcTask_VALUE, 1l);
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_CanAcceptNpcTask_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata metaData = super.getMetaData(fighter);
        if (fighter != null) {
            long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
            if (acceptedTaskFighters != null && acceptedTaskFighters.contains(playerId)) {
                return metaData;
            }
        }
        UnitMetadata.Builder builder = metaData.toBuilder();
        builder.getPropertiesBuilder().addKeys(MistUnitPropTypeEnum.MUPT_CanAcceptNpcTask).addValues(1l);
        return builder.build();
    }

    public MistRetCode acceptNpcTask(MistFighter fighter) {
        if (!isAlive()) {
            return MistRetCode.MRC_NpcStateError;
        }
        MistPlayer owner = fighter.getOwnerPlayerInSameRoom();
        if (owner == null) {
            return MistRetCode.MRC_OtherError;
        }
        int needStamina = (int) getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
        if (owner.getMistStamina() < needStamina) {
            return MistRetCode.MRC_StaminaNotEnough;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (acceptedTaskFighters != null && acceptedTaskFighters.contains(playerId)) {
            return MistRetCode.MRC_AcceptedTask;
        }
        if (acceptedTaskFighters == null) {
            acceptedTaskFighters = new HashSet<>();
        }

        MistRetCode retCode = fighter.getNpcTask().acceptTask((int) getAttribute(MistUnitPropTypeEnum.MUPT_NpcTaskCfgId_VALUE));
        if (retCode == MistRetCode.MRC_Success) {
            CS_GS_ReqChangeMistStamina.Builder builder = CS_GS_ReqChangeMistStamina.newBuilder();
            builder.setPlayerIdx(owner.getIdx());
            builder.setChangeValue(-needStamina);
            GlobalData.getInstance().sendMsgToServer(owner.getServerIndex(), MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE, builder);

            acceptedTaskFighters.add(playerId);
            addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_CanAcceptNpcTask_VALUE, 0l);
        }
        return retCode;
    }
}
