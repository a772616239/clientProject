package server.handler.pet.gem;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetGemLockStatus;
import protocol.PetMessage.Gem;
import protocol.PetMessage.SC_PetGemLockStatus;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 宝石上锁/解锁
 */
@MsgId(msgId = MsgIdEnum.CS_PetGemLockStatus_VALUE)
public class PetGemLockStatusHandler extends AbstractBaseHandler<CS_PetGemLockStatus> {

    @Override
    protected CS_PetGemLockStatus parse(byte[] bytes) throws Exception {
        return CS_PetGemLockStatus.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetGemLockStatus req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_PetGemLockStatus.Builder resultBuilder = SC_PetGemLockStatus.newBuilder();
        petgemEntity cache = petgemCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetGemLockStatus_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(cache, c -> {
            Gem gemById = cache.getGemById(req.getGemId());
            if (gemById == null) {
                gsChn.send(MsgIdEnum.SC_PetGemLockStatus_VALUE, resultBuilder);
                return;
            }

            Gem build = gemById.toBuilder().setGemLockStatus(req.getLockStatus()).build();
            cache.putGem(build);
        });

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_PetGemLockStatus_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetGemLock;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetGemLockStatus_VALUE, SC_PetGemLockStatus.newBuilder().setResult(retCode));
    }


}
