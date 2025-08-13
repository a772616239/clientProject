/*
package server.handler.pet.voidStone;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import platform.logs.ReasonManager;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.CS_VoidStoneConvertSave_VALUE;
import protocol.PetMessage.CS_VoidStoneConvertSave;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import protocol.PetMessage.SC_VoidStoneConvertSave;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import java.util.Map;

*/
/**
 * @author xiao_FL
 * @date 2019/6/3
 *//*

@MsgId(msgId = CS_VoidStoneConvertSave_VALUE)
public class VoidStoneLvlConvertSaveHandler extends AbstractBaseHandler<CS_VoidStoneConvertSave> {

    @Override
    protected CS_VoidStoneConvertSave parse(byte[] bytes) throws Exception {
        return CS_VoidStoneConvertSave.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_VoidStoneConvertSave req, int i) {
        SC_VoidStoneConvertSave.Builder result = SC_VoidStoneConvertSave.newBuilder();
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        Pet pet;
        if (entity == null || (pet = entity.getPetById(req.getPetId())) == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_VoidStoneConvertSave_VALUE, result);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, cacheTemp -> {
            Map<String, Integer> tempVoidStoneMap = petCache.getInstance().getTempVoidStone();
            Integer tempVoidStone = tempVoidStoneMap.get(pet.getId());
            if (tempVoidStone==null){
                result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_VoidStoneNotExist));
                gsChn.send(MsgIdEnum.SC_VoidStoneConvertSave_VALUE, result);
                return;
            }
            Builder builder = pet.toBuilder();
            builder.setVoidStoneId(tempVoidStone);
            entity.refreshPetPropertyAndPut(builder, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_VoidStoneConvert),true);
            tempVoidStoneMap.remove(pet.getId());
        });

        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_VoidStoneConvertSave_VALUE, result);

    }

}
*/
