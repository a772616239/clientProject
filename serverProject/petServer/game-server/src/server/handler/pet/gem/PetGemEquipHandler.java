package server.handler.pet.gem;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GameConst.WarPetUpdate;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import org.apache.commons.lang.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.petGem.PetGemEquipLog;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.PetMessage.CS_PetGemEquip;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import protocol.PetMessage.SC_PetGemEquip;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetGemEquip_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetGemEquip_VALUE;

@MsgId(msgId = CS_PetGemEquip_VALUE)
public class PetGemEquipHandler extends AbstractBaseHandler<CS_PetGemEquip> {

    @Override
    protected CS_PetGemEquip parse(byte[] bytes) throws Exception {
        return CS_PetGemEquip.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetGemEquip req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petgemEntity petgemEntity = petgemCache.getInstance().getEntityByPlayer(playerId);

        SC_PetGemEquip.Builder resultBuilder = SC_PetGemEquip.newBuilder();
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerId);
        Pet pet;
        if (null == petgemEntity || StringUtils.isBlank(req.getPetId()) || StringUtils.isBlank(req.getGemId())
                || null == (pet = petCache.getInstance().getPetById(playerId, req.getPetId()))
                || null == petEntity) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_PetGemEquip_VALUE, resultBuilder);
            return;
        }
        if (pet.getPetLvl() < GameConfig.getById(GameConst.CONFIG_ID).getVoidstoneunlocklvl()) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_LevelLimit));
            gsChn.send(SC_PetGemEquip_VALUE, resultBuilder);
            return;
        }
        Gem needEquipGem = petgemEntity.getGemById(req.getGemId());
        //宝石不存在
        if (needEquipGem == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_GemNotExist));
            gsChn.send(SC_PetGemEquip_VALUE, resultBuilder);
            return;
        }
        //已装备当前宠物
        if (req.getGemId().equals(needEquipGem.getGemPet())) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(SC_PetGemEquip_VALUE, resultBuilder);
            return;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_EquipGem);
        SyncExecuteFunction.executeConsumer(petEntity, cx -> {
            //脱掉上一个装备当前宝石的宠物
            if (StringUtils.isNotBlank(needEquipGem.getGemPet())) {
                Pet lastEquipPet = petEntity.getPetById(needEquipGem.getGemPet());
                lastEquipPet = lastEquipPet.toBuilder().clearGemId().build();
                petEntity.putPet(lastEquipPet);
                petEntity.refreshPetPropertyAndPut(lastEquipPet, reason, false);
                //通知战戈宠物更新
                EventUtil.triggerWarPetUpdate(playerId, lastEquipPet.getId(), WarPetUpdate.MODIFY);
            }
            //装备到当前宠物
            Builder petBuilder = pet.toBuilder().setGemId(needEquipGem.getId());
            petEntity.refreshPetPropertyAndPut(petBuilder, reason, true);
            //通知战戈宠物更新
            EventUtil.triggerWarPetUpdate(playerId, pet.getId(), WarPetUpdate.MODIFY);
        });
        SyncExecuteFunction.executeConsumer(petgemEntity, e -> {
            //脱掉宠物宝石
            Gem lastEquipGem = null;
            if (StringUtils.isNotBlank(pet.getGemId())) {
                //脱掉宝石
                lastEquipGem = petgemEntity.getGemById(pet.getGemId());
                if (lastEquipGem != null) {
                    Gem gemInPetUnEquip = lastEquipGem.toBuilder().clearGemPet().build();
                    petgemEntity.putGem(gemInPetUnEquip);
                    petgemEntity.sendGemUpdate(gemInPetUnEquip);
                }
            }
            //装备当前宝石到宠物上
            Gem equipGem = needEquipGem.toBuilder().setGemPet(pet.getId()).build();
            petgemEntity.putGem(equipGem);
            petgemEntity.sendGemUpdate(equipGem);
            // 埋点日志
            LogService.getInstance().submit(new PetGemEquipLog(playerId, pet.getId(), lastEquipGem, equipGem));
            if (StringUtils.isNotBlank(needEquipGem.getGemPet())) {
                LogService.getInstance().submit(new PetGemEquipLog(playerId, needEquipGem.getGemPet(), needEquipGem, null));
            }
            petCache.getInstance().equipGemStatistic(playerId, lastEquipGem, equipGem);

        });
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setPetId(req.getPetId());
        gsChn.send(SC_PetGemEquip_VALUE, resultBuilder);
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetGemEquip;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetGemEquip_VALUE, SC_PetGemEquip.newBuilder().setResult(retCode));
    }


}
