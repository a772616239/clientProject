package server.handler.pet.helpPet;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.entity.petEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_HelpPetInit;
import protocol.PetMessage.SC_HelpPetInit;


/**
 * 处理客户端打开助阵宠物背包打开
 */
@MsgId(msgId = MsgIdEnum.CS_HelpPetInit_VALUE)
public class HelpPetInitHandler extends AbstractBaseHandler<CS_HelpPetInit> {

    @Override
    protected CS_HelpPetInit parse(byte[] bytes) throws Exception {
        return CS_HelpPetInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_HelpPetInit req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity petCacheTemp = model.pet.dbCache.petCache.getInstance().getEntityByPlayer(playerId);

        SC_HelpPetInit.Builder msg = SC_HelpPetInit.newBuilder();
        if (petCacheTemp == null) {
            gsChn.send(MsgIdEnum.SC_HelpPetInit_VALUE, msg);
            return;
        }
        for (PetMessage.HelpPetBagItem helpPetBagItem : petCacheTemp.getDbPetsBuilder().getHelpPetMap().values()) {
            msg.addHelpPet(helpPetBagItem);
        }
        gsChn.send(MsgIdEnum.SC_HelpPetInit_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_HelpPetInit_VALUE, SC_HelpPetInit.newBuilder());
    }


}
