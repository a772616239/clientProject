package server.handler.pet.rune;

import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.PetMessage.CS_PetRuneEquip;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneEquip;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetRuneEquip_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetRuneEquip_VALUE;

/**
 * @author xiao_FL
 * @date 2019/6/4
 */
@MsgId(msgId = CS_PetRuneEquip_VALUE)
public class PetRuneEquipHandler extends AbstractBaseHandler<CS_PetRuneEquip> {

    @Override
    protected CS_PetRuneEquip parse(byte[] bytes) throws Exception {
        return CS_PetRuneEquip.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetRuneEquip req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petruneEntity entity = petruneCache.getInstance().getEntityByPlayer(playerId);

        SC_PetRuneEquip.Builder resultBuilder = SC_PetRuneEquip.newBuilder();
        if (null == entity || req.getRuneIdCount() > GameConst.EACH_PET_MAX_EQUIP_RUNE_COUNT
                || null == petCache.getInstance().getPetById(playerId, req.getPetId())) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_PetRuneEquip_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            for (String runeIdx : req.getRuneIdList()) {
                Rune rune = entity.getRuneById(runeIdx);
                if (rune == null) {
                    continue;
                }
                //判断符文是否已经装备
                if (entity.runeIsEquippedByPet(runeIdx, req.getPetId())) {
                    continue;
                }

                String unEquipIdx = entity.unEquipSameTypeRune(req.getPetId(), entity.getRuneType(runeIdx));
                if (unEquipIdx != null) {
                    resultBuilder.addUnEquipRuneId(unEquipIdx);
                }

                if (entity.runeIsEquipped(runeIdx)) {
                    entity.unEquipRuneById(runeIdx, true, true);
                }

                entity.equipRune(runeIdx, req.getPetId(), false);
                resultBuilder.addEquipRuneId(runeIdx);
            }
        });

        // 重新计算宠物属性，通知
        petCache.getInstance().refreshPetProperty(playerId, req.getPetId(),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_EquipRune), true);

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setPetId(req.getPetId());
        gsChn.send(SC_PetRuneEquip_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetRuneEquip;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetRuneEquip_VALUE, SC_PetRuneEquip.newBuilder().setResult(retCode));
    }


}
