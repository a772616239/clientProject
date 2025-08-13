/*
package server.handler.pet.voidStone;

import cfg.VoidStoneConfig;
import cfg.VoidStoneConfigObject;
import common.AbstractBaseHandler;
import common.GameConst.WarPetUpdate;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Consume;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.CS_VoidStoneLvUp_VALUE;
import protocol.PetMessage.CS_VoidStoneLvUp;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import protocol.PetMessage.SC_VoidStoneLvUp;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;
import util.RandomUtil;

import java.util.List;

*/
/**
 * @author xiao_FL
 * @date 2019/6/3
 *//*

@MsgId(msgId = CS_VoidStoneLvUp_VALUE)
public class VoidStoneLvlUpHandler extends AbstractBaseHandler<CS_VoidStoneLvUp> {

    @Override
    protected CS_VoidStoneLvUp parse(byte[] bytes) throws Exception {
        return CS_VoidStoneLvUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_VoidStoneLvUp req, int i) {
        SC_VoidStoneLvUp.Builder result = SC_VoidStoneLvUp.newBuilder();
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        Pet pet;
        if (entity == null || (pet = entity.getPetById(req.getPetId())) == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_VoidStoneLvUp_VALUE, result);
            return;
        }
        VoidStoneConfigObject config = VoidStoneConfig.getById(pet.getVoidStoneId());
        if (config == null) {
            //符文宝石不存在
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_VoidStoneNotExist));
            gsChn.send(MsgIdEnum.SC_VoidStoneLvUp_VALUE, result);
            return;
        }
        if (pet.getPetLvl() < config.getNeedpetlv()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_LevelLimit));
            gsChn.send(MsgIdEnum.SC_VoidStoneLvUp_VALUE, result);
            return;
        }

        //到达最大等级
        int newVoidStoneId = RandomUtil.randomNextLvVoidStone(config.getRarity(), config.getLv(), req.getLockProperty() ? config.getPropertytype() : null);
        if (newVoidStoneId == -1) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_VoidStoneMaxLvLimit));
            gsChn.send(MsgIdEnum.SC_VoidStoneLvUp_VALUE, result);
            return;
        }
        List<Consume> consumes = ConsumeUtil.parseToConsumeList(config.getUpconsume());
        //升级
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_VoidStoneLvUp);
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes,
                reason)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_VoidStoneLvUp_VALUE, result);
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            Builder builder = pet.toBuilder();
            builder.setVoidStoneId(newVoidStoneId);
            entity.refreshPetPropertyAndPut(builder, reason,true);
        });
        result.setNewStoneId(newVoidStoneId);
        //通知战戈宠物更新
        EventUtil.triggerWarPetUpdate(entity.getPlayeridx(),pet.getId(), WarPetUpdate.MODIFY);
        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_VoidStoneLvUp_VALUE, result);
    }

}
*/
