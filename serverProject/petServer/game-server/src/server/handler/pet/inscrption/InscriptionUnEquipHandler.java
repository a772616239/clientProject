package server.handler.pet.inscrption;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.inscription.dbCache.petinscriptionCache;
import model.inscription.petinscriptionEntity;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_InscriptionUnEquip;
import protocol.PetMessage.Gem;
import protocol.PetMessage.SC_PetGemUnEquip;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_InscriptionUnEquip_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_InscriptionUnEquip_VALUE;


@MsgId(msgId = CS_InscriptionUnEquip_VALUE)
public class InscriptionUnEquipHandler extends AbstractBaseHandler<CS_InscriptionUnEquip> {

    @Override
    protected CS_InscriptionUnEquip parse(byte[] bytes) throws Exception {
        return CS_InscriptionUnEquip.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_InscriptionUnEquip req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        PetMessage.SC_InscriptionUnEquip.Builder resultBuilder = PetMessage.SC_InscriptionUnEquip.newBuilder();
        petgemEntity gemEntity = petgemCache.getInstance().getEntityByPlayer(playerId);
        petinscriptionEntity entity = petinscriptionCache.getInstance().getEntityByPlayer(playerId);
        if (gemEntity == null || entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_InscriptionUnEquip_VALUE, resultBuilder);
            return;
        }
        Gem gem = gemEntity.getGemById(req.getGemId());
        if (gem == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_GemNotExist));
            gsChn.send(SC_InscriptionUnEquip_VALUE, resultBuilder);
            return;
        }
        if (!gem.getInscriptionIdList().contains(req.getInscriptionId())) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_InscriptionUnEquip_VALUE, resultBuilder);
            return;

        }

        unEquipGemInscriptionId(req, gemEntity, gem);

        inscriptionEntity2Item(req, entity);

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_InscriptionUnEquip_VALUE, resultBuilder);
        petCache.settlePetUpdate(playerId, gem.getGemPet(), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_InscriptionUnEquip));
    }

    private void inscriptionEntity2Item(CS_InscriptionUnEquip req, petinscriptionEntity entity) {
        SyncExecuteFunction.executeConsumer(entity, cx->{
            entity.entity2Item(req.getInscriptionId());
        });
    }

    private void unEquipGemInscriptionId(CS_InscriptionUnEquip req, petgemEntity gemEntity, Gem gem) {
        SyncExecuteFunction.executeConsumer(gemEntity, cx -> {
            Gem.Builder builder = gem.toBuilder().clearInscriptionId();
            gem.getInscriptionIdList().stream().filter(e -> !e.equals(req.getInscriptionId())).forEach(builder::addInscriptionId);
            Gem newGem = builder.build();
            gemEntity.putGem(newGem);
            gemEntity.sendGemUpdate(newGem);
        });
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_Inscription;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetGemUnEquip_VALUE, SC_PetGemUnEquip.newBuilder().setResult(retCode));
    }


}
