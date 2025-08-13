package server.handler.pet.rune;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import protocol.Common.EnumFunction;
import protocol.PetMessage.CS_PetRuneUnEquip;
import protocol.PetMessage.SC_PetRuneUnEquip;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetRuneUnEquip_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetRuneUnEquip_VALUE;

/**
 * @author xiao_FL
 * @date 2019/6/4
 */
@MsgId(msgId = CS_PetRuneUnEquip_VALUE)
public class PetRuneUnEquipHandler extends AbstractBaseHandler<CS_PetRuneUnEquip> {

    @Override
    protected CS_PetRuneUnEquip parse(byte[] bytes) throws Exception {
        return CS_PetRuneUnEquip.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetRuneUnEquip req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PetRuneUnEquip.Builder resultBuilder = SC_PetRuneUnEquip.newBuilder();
        petruneEntity entity = petruneCache.getInstance().getEntityByPlayer(playerId);
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetRuneUnEquip_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.unEquipAllRuneById(req.getRuneIdList(), true);
        });

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetRuneEquip;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetRuneUnEquip_VALUE, SC_PetRuneUnEquip.newBuilder().setResult(retCode));
    }


}
