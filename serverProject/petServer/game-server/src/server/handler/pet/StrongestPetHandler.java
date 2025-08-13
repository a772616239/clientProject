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

@MsgId(msgId = MessageId.MsgIdEnum.CS_StrongestPetDetail_VALUE)
public class StrongestPetHandler extends AbstractBaseHandler<PetMessage.CS_StrongestPetDetail> {


    @Override
    protected PetMessage.CS_StrongestPetDetail parse(byte[] bytes) throws Exception {
        return PetMessage.CS_StrongestPetDetail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PetMessage.CS_StrongestPetDetail req, int i) {
        PetMessage.SC_StrongestPetDetail.Builder msg = StrongestPetManager.getInstance().getClientStrongestPetDetailMsg(req.getPetBookId());
        gsChn.send(MessageId.MsgIdEnum.SC_StrongestPetDetail_VALUE, msg);

    }

    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.PetBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_PetUnLock_VALUE, PetMessage.SC_PetLock.newBuilder().setResult(retCode));
    }
}