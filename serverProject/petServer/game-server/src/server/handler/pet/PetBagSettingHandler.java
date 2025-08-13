package server.handler.pet;


import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.entity.petEntity;
import protocol.Common;
import protocol.MessageId;
import protocol.PetMessage;
import protocol.RetCodeId;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_PetBagSetting_VALUE;

@MsgId(msgId = MessageId.MsgIdEnum.CS_PetBagSetting_VALUE)
public class PetBagSettingHandler extends AbstractBaseHandler<PetMessage.CS_PetBagSetting> {

    @Override
    protected PetMessage.CS_PetBagSetting parse(byte[] bytes) throws Exception {
        return PetMessage.CS_PetBagSetting.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PetMessage.CS_PetBagSetting req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity petDb = model.pet.dbCache.petCache.getInstance().getEntityByPlayer(playerId);


        PetMessage.SC_PetBagSetting.Builder msg = PetMessage.SC_PetBagSetting.newBuilder();
        boolean autoFree = req.getAutoFree();
        if (autoFree == petDb.getDbPetsBuilder().getAutoFree()) {
            msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
            gsChn.send(SC_PetBagSetting_VALUE, msg);
            return;
        }
        SyncExecuteFunction.executeConsumer(petDb, cacheTemp -> {
            cacheTemp.getDbPetsBuilder().setAutoFree(autoFree);
        });

        msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(SC_PetBagSetting_VALUE, msg);

    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.PetBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_PetBagSetting_VALUE, protocol.PetMessage.SC_PetBagSetting.newBuilder().setResult(retCode));
    }


}
