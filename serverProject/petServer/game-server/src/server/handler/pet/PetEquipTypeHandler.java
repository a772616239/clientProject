package server.handler.pet;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetEquipType;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetDisCharge;
import protocol.PetMessage.SC_PetEquipType;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author xiao_FL
 * @date 2019/12/27
 */
@MsgId(msgId = MsgIdEnum.CS_PetEquipType_VALUE)
public class PetEquipTypeHandler extends AbstractBaseHandler<CS_PetEquipType> {

    @Override
    protected CS_PetEquipType parse(byte[] bytes) throws Exception {
        return CS_PetEquipType.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetEquipType req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);

        SC_PetDisCharge.Builder resultBuilder = SC_PetDisCharge.newBuilder();
        if (cache == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetEquipType_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            Pet pet = cache.getPetById(req.getPetId());
            if (pet == null) {
                resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_PetEquipType_VALUE, resultBuilder);
                return;
            }

            Pet build = pet.toBuilder().setOneKeyEquipType(req.getEquipType()).build();
            cache.putPet(build);

            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_PetEquipType_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetSystem;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetEquipType_VALUE, SC_PetEquipType.newBuilder().setResult(retCode));
    }
}
