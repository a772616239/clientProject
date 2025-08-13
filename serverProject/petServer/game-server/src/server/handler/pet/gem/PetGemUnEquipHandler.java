package server.handler.pet.gem;

import common.AbstractBaseHandler;
import common.GameConst.WarPetUpdate;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import org.springframework.util.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.petGem.PetGemEquipLog;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.PetMessage.CS_PetGemUnEquip;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import protocol.PetMessage.SC_PetGemUnEquip;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetGemUnEquip_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetGemUnEquip_VALUE;

/**
 * @author xiao_FL
 * @date 2019/6/4
 */
@MsgId(msgId = CS_PetGemUnEquip_VALUE)
public class PetGemUnEquipHandler extends AbstractBaseHandler<CS_PetGemUnEquip> {

    @Override
    protected CS_PetGemUnEquip parse(byte[] bytes) throws Exception {
        return CS_PetGemUnEquip.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetGemUnEquip req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PetGemUnEquip.Builder resultBuilder = SC_PetGemUnEquip.newBuilder();
        petgemEntity entity = petgemCache.getInstance().getEntityByPlayer(playerId);
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerId);
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetGemUnEquip_VALUE, resultBuilder);
            return;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_UnEquipGem);
        Gem petGem = entity.getGemById(req.getGemId());
        if (petGem == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_GemNotExist));
            gsChn.send(SC_PetGemUnEquip_VALUE, resultBuilder);
            return;
        }
        String gemPet = petGem.getGemPet();
        if (StringUtils.isEmpty(gemPet)) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_PetGemUnEquip_VALUE, resultBuilder);
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> {
                    Gem newGem = petGem.toBuilder().clearGemPet().build();
                    entity.putGem(newGem);
                    entity.sendGemUpdate(newGem);
                }
        );
        Pet pet = petEntity.getPetById(gemPet);
        if (pet == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(SC_PetGemUnEquip_VALUE, resultBuilder);
            return;
        }
        SyncExecuteFunction.executeConsumer(petEntity, cx -> {
            Builder petBuilder = pet.toBuilder().clearGemId();
            petEntity.refreshPetPropertyAndPut(petBuilder, reason, true);
        });
        petCache.getInstance().equipGemStatistic(playerId, petGem, null);
        // 埋点日志
        LogService.getInstance().submit(new PetGemEquipLog(playerId, pet.getId(), petGem, null));
        //通知战戈宠物更新
        EventUtil.triggerWarPetUpdate(playerId, gemPet, WarPetUpdate.MODIFY);
        resultBuilder.setGemId(req.getGemId());
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_PetGemUnEquip_VALUE, resultBuilder);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetGemEquip;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetGemUnEquip_VALUE, SC_PetGemUnEquip.newBuilder().setResult(retCode));
    }


}
