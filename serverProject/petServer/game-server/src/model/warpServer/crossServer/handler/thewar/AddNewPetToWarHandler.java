package model.warpServer.crossServer.handler.thewar;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_AddNewWarPetDataRet;
import protocol.TheWar.SC_AddNewPetToWar;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TheWarDefine.WarPetData;

@MsgId(msgId = MsgIdEnum.CS_GS_AddNewWarPetDataRet_VALUE)
public class AddNewPetToWarHandler extends AbstractHandler<CS_GS_AddNewWarPetDataRet> {
    @Override
    protected CS_GS_AddNewWarPetDataRet parse(byte[] bytes) throws Exception {
        return CS_GS_AddNewWarPetDataRet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_AddNewWarPetDataRet ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SC_AddNewPetToWar.Builder builder = SC_AddNewPetToWar.newBuilder();
        builder.setRetCode(ret.getRetCode());
        builder.addAllNewPets(ret.getWarPetDataList());
        GlobalData.getInstance().sendMsg(ret.getPlayerIdx(), MsgIdEnum.SC_AddNewPetToWar_VALUE, builder);

        if (ret.getRetCode() == TheWarRetCode.TWRC_Success) {
            SyncExecuteFunction.executeConsumer(player, entity -> {
                for (WarPetData petData : ret.getWarPetDataList()) {
                    entity.getDb_data().getTheWarDataBuilder().putInWarPets(petData.getPetId(), 0);
                }
            });
        }
    }
}
