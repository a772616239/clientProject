package server.handler.pet.inscrption;

import cfg.GameConfig;
import cfg.InscriptionCfg;
import cfg.InscriptionCfgObject;
import cfg.PetGemConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Collections;
import lombok.Data;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.inscription.dbCache.petinscriptionCache;
import model.inscription.petinscriptionEntity;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_InscriptionEquip;
import protocol.PetMessage.Gem;
import protocol.PetMessage.SC_InscriptionEquip;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.Common.EnumFunction.EF_Inscription;
import static protocol.MessageId.MsgIdEnum.CS_InscriptionEquip_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_InscriptionEquip_VALUE;

@MsgId(msgId = CS_InscriptionEquip_VALUE)
public class InscriptionEquipHandler extends AbstractBaseHandler<CS_InscriptionEquip> {

    @Override
    protected CS_InscriptionEquip parse(byte[] bytes) throws Exception {
        return CS_InscriptionEquip.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_InscriptionEquip req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petgemEntity petgemEntity = petgemCache.getInstance().getEntityByPlayer(playerId);

        SC_InscriptionEquip.Builder resultBuilder = SC_InscriptionEquip.newBuilder();
        petgemEntity gemEntity = petgemCache.getInstance().getEntityByPlayer(playerId);
        petinscriptionEntity entity = petinscriptionCache.getInstance().getEntityByPlayer(playerId);
        if (gemEntity == null || entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_InscriptionEquip_VALUE, resultBuilder);
            return;
        }
        Gem gem = gemEntity.getGemById(req.getGemId());
        if (gem == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_GemNotExist));
            gsChn.send(SC_InscriptionEquip_VALUE, resultBuilder);
            return;
        }
        //判断等级
        if (!gemLvEnough(gem)) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Inscription_GemLvNotEnough));
            gsChn.send(SC_InscriptionEquip_VALUE, resultBuilder);
            return;
        }
        int cfgId = req.getCfgId();
        InscriptionCfgObject cfg = InscriptionCfg.getById(cfgId);
        if (cfg == null || cfg.getType() == GameConst.generalInscriptionType) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_InscriptionEquip_VALUE, resultBuilder);
            return;
        }
        if (!entity.inscriptionItemEnough(cfg.getId(), 1)) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(SC_InscriptionEquip_VALUE, resultBuilder);
            return;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_InscriptionEquip);
        if (!ConsumeManager.getInstance().consumeMaterialByList(
                playerId, ConsumeUtil.parseToConsumeList(cfg.getEquipconsume()), reason)) {

            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(SC_InscriptionEquip_VALUE, resultBuilder);
            return;
        }

        ItemConvert2EntityResult result = inscriptionItemConvert2Entity(entity, cfgId);

        if (RetCodeEnum.RCE_Success == result.getCodeEnum()) {
            equipInscriptionOnGem(petgemEntity, gem, result.getInscription());
        }

        resultBuilder.setResult(GameUtil.buildRetCode(result.getCodeEnum()));
        gsChn.send(SC_InscriptionEquip_VALUE, resultBuilder);
        petCache.settlePetUpdate(playerId, gem.getGemPet(), reason);
    }

    /**
     * 装备铭文到宝石
     * @param petgemEntity
     * @param gem
     * @param inscription
     */
    private void equipInscriptionOnGem(petgemEntity petgemEntity, Gem gem, PetMessage.Inscription inscription) {
        SyncExecuteFunction.executeConsumer(petgemEntity, cx -> {
            Gem newGem = gem.toBuilder().addInscriptionId(inscription.getId()).build();
            petgemEntity.putGem(newGem);
            petgemEntity.sendGemUpdate(newGem);
        });
    }

    /**
     * 将铭文背包的道具态铭文转为实例
     * @param entity
     * @param cfgId
     * @return
     */
    private ItemConvert2EntityResult inscriptionItemConvert2Entity(petinscriptionEntity entity, int cfgId) {
        return SyncExecuteFunction.executeFunction(entity, cx -> {
            ItemConvert2EntityResult result = new ItemConvert2EntityResult();
            PetMessage.Inscription inscription = entity.InscriptionItemConvert2Entity(cfgId);
            if (inscription == null) {
                result.setCodeEnum(RetCodeEnum.RCE_MatieralNotEnough);
                return result;
            }
            entity.sendAddInscription(Collections.singletonList(inscription), null);
            entity.sendRemoveInscription(null, Collections.singletonMap(cfgId, 1));
            result.setCodeEnum(RetCodeEnum.RCE_Success);
            result.setInscription(inscription);
            return result;

        });
    }


    private boolean gemLvEnough(Gem gem) {
        int[] inscriptionOpenLv = GameConfig.getById(GameConst.CONFIG_ID).getInscriptionopenlv();
        int equipCount = gem.getInscriptionIdList().size();
        if (equipCount >= inscriptionOpenLv.length) {
            return false;
        }
        return PetGemConfig.queryEnhanceLv(gem.getGemConfigId()) >= inscriptionOpenLv[equipCount];
    }


    @Override
    public EnumFunction belongFunction() {
        return EF_Inscription;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_InscriptionEquip_VALUE, SC_InscriptionEquip.newBuilder().setResult(retCode));
    }


    @Data
    private class ItemConvert2EntityResult {
        private RetCodeEnum codeEnum;
        PetMessage.Inscription inscription;
    }
}
