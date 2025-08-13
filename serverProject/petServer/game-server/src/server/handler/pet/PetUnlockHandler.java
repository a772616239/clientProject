package server.handler.pet;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.entity.petEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_PetUnLock;
import protocol.PetMessage.SC_PetLock;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author xiao_FL
 * @date 2019/6/13
 */
@MsgId(msgId = MsgIdEnum.CS_PetUnLock_VALUE)
public class PetUnlockHandler extends AbstractBaseHandler<CS_PetUnLock> {


    @Override
    protected CS_PetUnLock parse(byte[] bytes) throws Exception {
        return CS_PetUnLock.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetUnLock req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        petEntity entity = model.pet.dbCache.petCache.getInstance().getEntityByPlayer(playerId);
        SC_PetLock.Builder resultBuilder = SC_PetLock.newBuilder();
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetUnLock_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.lockPet(req.getId(), false);
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_PetUnLock_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetLock;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetUnLock_VALUE, PetMessage.SC_PetLock.newBuilder().setResult(retCode));
    }


}
