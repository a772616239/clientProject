package server.handler.pet.rune;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_PetRuneLockStatus;
import protocol.PetMessage.Rune;
import protocol.PetMessage.SC_PetRuneLockStatus;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 处理客户端修改符文上锁状态请求
 *
 * @author xiao_FL
 * @date 2019/10/8
 */
@MsgId(msgId = MsgIdEnum.CS_PetRuneLockStatus_VALUE)
public class PetRuneLockStatusHandler extends AbstractBaseHandler<CS_PetRuneLockStatus> {

    @Override
    protected CS_PetRuneLockStatus parse(byte[] bytes) throws Exception {
        return CS_PetRuneLockStatus.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetRuneLockStatus req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_PetRuneLockStatus.Builder resultBuilder = SC_PetRuneLockStatus.newBuilder();
        petruneEntity cache = petruneCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetRuneLockStatus_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(cache, c -> {
            Rune runeById = cache.getRuneById(req.getRuneId());
            if (runeById == null) {
                resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_RuneNotExist));
                gsChn.send(MsgIdEnum.SC_PetRuneLockStatus_VALUE, resultBuilder);
                return;
            }

            Rune build = runeById.toBuilder().setRuneLockStatus(req.getLockStatus()).build();
            cache.putRune(build);
        });

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_PetRuneLockStatus_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetRuneLock;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetRuneLockStatus_VALUE, PetMessage.SC_PetRuneLockStatus.newBuilder().setResult(retCode));
    }


}
