package server.handler.thewar;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_AddNewWarPetDataRet;
import protocol.ServerTransfer.GS_CS_AddNewWarPetData;
import protocol.ServerTransfer.WarBattlePet;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TheWarDefine.WarPetData.Builder;

@MsgId(msgId = MsgIdEnum.GS_CS_AddNewWarPetData_VALUE)
public class AddWarPetHandler extends AbstractHandler<GS_CS_AddNewWarPetData> {
    @Override
    protected GS_CS_AddNewWarPetData parse(byte[] bytes) throws Exception {
        return GS_CS_AddNewWarPetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_AddNewWarPetData req, int i) {
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        CS_GS_AddNewWarPetDataRet.Builder retBuilder = CS_GS_AddNewWarPetDataRet.newBuilder();
        retBuilder.setPlayerIdx(req.getPlayerIdx());
        if (warPlayer == null) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_NotFoundPlayer);
            gsChn.send(MsgIdEnum.CS_GS_AddNewWarPetDataRet_VALUE, retBuilder);
            return;
        }
        Builder petData;
        for (WarBattlePet warBattlePet : req.getWarBattlePetsList()) {
            petData = warPlayer.addNewPetData(warBattlePet.getPos(), warBattlePet.getPetData());
            if (petData != null) {
                retBuilder.addWarPetData(petData);
            }
        }
        retBuilder.setRetCode(TheWarRetCode.TWRC_Success);
        gsChn.send(MsgIdEnum.CS_GS_AddNewWarPetDataRet_VALUE, retBuilder);
    }
}
