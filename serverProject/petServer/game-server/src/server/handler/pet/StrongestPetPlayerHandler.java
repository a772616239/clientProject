package server.handler.pet;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.StrongestPetManager;
import protocol.Common;
import protocol.MessageId;
import protocol.PetMessage;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_StrongestPetPlayer_VALUE)
public class StrongestPetPlayerHandler extends AbstractBaseHandler<PetMessage.CS_StrongestPetPlayer> {


    @Override
    protected PetMessage.CS_StrongestPetPlayer parse(byte[] bytes) throws Exception {
        return PetMessage.CS_StrongestPetPlayer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PetMessage.CS_StrongestPetPlayer req, int i) {
        PetMessage.SC_StrongestPetPlayer.Builder msg = StrongestPetManager.getInstance().getClientStrongestPetPlayerMsg(req.getPetBookId());
        gsChn.send(MessageId.MsgIdEnum.SC_StrongestPetPlayer_VALUE, msg);

    }

    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.PetBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetUnLock_VALUE, PetMessage.SC_PetLock.newBuilder().setResult(retCode));
    }
}