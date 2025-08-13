package server.handler.pet.inscrption;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Map;
import model.inscription.dbCache.petinscriptionCache;
import model.inscription.petinscriptionEntity;
import protocol.Common.EnumFunction;
import protocol.PetMessage;

import static protocol.Common.EnumFunction.EF_Inscription;
import static protocol.MessageId.MsgIdEnum.CS_InscriptionBagInit_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_InscriptionBagInit_VALUE;

/**
 * 客户端打开铭文背包
 */
@MsgId(msgId = CS_InscriptionBagInit_VALUE)
public class InscriptionBagInitHandler extends AbstractBaseHandler<PetMessage.CS_InscriptionBagInit> {

    @Override
    protected PetMessage.CS_InscriptionBagInit parse(byte[] bytes) throws Exception {
        return PetMessage.CS_InscriptionBagInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PetMessage.CS_InscriptionBagInit csInscriptionBagInit, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        PetMessage.SC_InscriptionBagInit.Builder msg = PetMessage.SC_InscriptionBagInit.newBuilder();
        petinscriptionEntity inscriptionEntity = petinscriptionCache.getInstance().getEntityByPlayer(playerId);
        if (inscriptionEntity == null) {
            gsChn.send(SC_InscriptionBagInit_VALUE, msg);
            return;
        }

        msg.addAllInscriptions(inscriptionEntity.getDb_data().getInscriptionEntityMap().values());
        Map<Integer, Integer> inscriptionItemMap = inscriptionEntity.getItemNum();
        msg.addAllCfgId(inscriptionItemMap.keySet()).addAllNum(inscriptionItemMap.values());
        gsChn.send(SC_InscriptionBagInit_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EF_Inscription;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(SC_InscriptionBagInit_VALUE, PetMessage.SC_InscriptionBagInit.newBuilder());
    }
}
