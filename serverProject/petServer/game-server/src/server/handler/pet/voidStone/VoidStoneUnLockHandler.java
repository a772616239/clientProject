/*
package server.handler.pet.voidStone;

import cfg.GameConfig;
import cfg.VoidStoneConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GameConst.WarPetUpdate;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_VoidStoneUnLock;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import protocol.PetMessage.SC_VoidStoneUnLock;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;
import util.RandomUtil;

*/
/**
 * @author xiao_FL
 * @date 2019/6/13
 *//*

@MsgId(msgId = MsgIdEnum.CS_VoidStoneUnLock_VALUE)
public class VoidStoneUnLockHandler extends AbstractBaseHandler<CS_VoidStoneUnLock> {

    @Override
    protected CS_VoidStoneUnLock parse(byte[] bytes) throws Exception {
        return CS_VoidStoneUnLock.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_VoidStoneUnLock req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        SC_VoidStoneUnLock.Builder resultBuilder = SC_VoidStoneUnLock.newBuilder();
        Pet pet = petCache.getInstance().getPetById(playerId, req.getPetId());
        if (pet == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_PetNotExist));
            gsChn.send(MsgIdEnum.SC_VoidStoneUnLock_VALUE, resultBuilder);
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> {
            if (pet.getPetLvl() < GameConfig.getById(GameConst.CONFIG_ID).getVoidstoneunlocklvl()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_LevelLimit));
                gsChn.send(MsgIdEnum.SC_VoidStoneUnLock_VALUE, resultBuilder);
                return;
            }

            if (pet.getVoidStoneId() != 0) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_VoidStoneIsUnLock));
                gsChn.send(MsgIdEnum.SC_VoidStoneUnLock_VALUE, resultBuilder);
                return;
            }
            Builder builder = pet.toBuilder();
            int voidStoneId = RandomUtil.randomVoidStone(VoidStoneConfig.getInitRarity(), VoidStoneConfig.getInitLv(), null, null);
            if (pet.getVoidStoneId() == -1) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_VoidStoneUnLock_VALUE, resultBuilder);
                return;
            }
            builder.setVoidStoneId(voidStoneId);
            //通知战戈宠物更新
            EventUtil.triggerWarPetUpdate(entity.getPlayeridx(),pet.getId(), WarPetUpdate.MODIFY);
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_VoidStoneActive);
            entity.refreshPetPropertyAndPut(builder, reason,true);
            resultBuilder.setNewStoneId(voidStoneId);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_VoidStoneUnLock_VALUE, resultBuilder);
        });
    }
}
*/
