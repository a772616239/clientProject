/*
package server.handler.pet.voidStone;

import cfg.VoidStoneConfig;
import cfg.VoidStoneConfigObject;
import common.AbstractBaseHandler;
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
import static protocol.MessageId.MsgIdEnum.CS_VoidStoneConvert_VALUE;
import protocol.PetMessage.CS_VoidStoneConvert;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_VoidStoneConvert;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.RandomUtil;

import java.util.List;
import java.util.Map;

*/
/**
 * 虚空宝石转换
 *//*

@MsgId(msgId = CS_VoidStoneConvert_VALUE)
public class VoidStoneLvlConvertHandler extends AbstractBaseHandler<CS_VoidStoneConvert> {

    @Override
    protected CS_VoidStoneConvert parse(byte[] bytes) throws Exception {
        return CS_VoidStoneConvert.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_VoidStoneConvert req, int i) {
        SC_VoidStoneConvert.Builder result = SC_VoidStoneConvert.newBuilder();
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        Pet pet;
        if (entity == null || (pet = entity.getPetById(req.getPetId())) == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_VoidStoneConvert_VALUE, result);
            return;
        }
        VoidStoneConfigObject config = VoidStoneConfig.getById(pet.getVoidStoneId());
        if (config == null) {
            //符文宝石不存在
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_VoidStoneNotExist));
            gsChn.send(MsgIdEnum.SC_VoidStoneConvert_VALUE, result);
            return;
        }
        int newVoidStoneId = RandomUtil.randomVoidStone(config.getRarity(), config.getLv(), null, pet.getVoidStoneId());
        if (newVoidStoneId == -1) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_VoidStoneConvert_VALUE, result);
            return;
        }
        List<Consume> consumes = ConsumeUtil.parseToConsumeList(config.getChangeconsume());
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_VoidStoneConvert);
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes,
                reason)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_VoidStoneConvert_VALUE, result);
            return;
        }
        Map<String, Integer> tempVoidStone = petCache.getInstance().getTempVoidStone();
        tempVoidStone.put(pet.getId(),newVoidStoneId);
        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        result.setNewStoneId(newVoidStoneId);
        gsChn.send(MsgIdEnum.SC_VoidStoneConvert_VALUE, result);

    }

}
*/
