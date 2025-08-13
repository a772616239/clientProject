package server.handler.selectedPet;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.DrawCard.CS_ClaimSelectedPet;
import protocol.DrawCard.SC_ClaimSelectedPet;
import protocol.DrawCard.SC_ClaimSelectedPet.Builder;
import protocol.DrawCard.SelectedPet;
import protocol.DrawCard.SelectedPetIndex;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_SelectedPet;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/07/01
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimSelectedPet_VALUE)
public class ClaimSelectedPetHandler extends AbstractBaseHandler<CS_ClaimSelectedPet> {
    @Override
    protected CS_ClaimSelectedPet parse(byte[] bytes) throws Exception {
        return CS_ClaimSelectedPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimSelectedPet req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity entity = playerCache.getByIdx(playerIdx);
        Builder resultBuilder = SC_ClaimSelectedPet.newBuilder();
        if (entity == null){
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimSelectedPet_VALUE, resultBuilder);
            return;
        }

        for (int index = 0; index < entity.getDb_data().getSelectedPetBuilder().getPetTypeCount(); index++) {
            SelectedPet.Builder builder = SelectedPet.newBuilder().setPetType(entity.getDb_data().getSelectedPetBuilder().getPetType(index));
            DB_SelectedPet selectedPet = entity.getDb_data().getSelectedPetBuilder().getPetData(index);
            for (SelectedPetIndex petData : selectedPet.getSelectPetDataList()) {
                builder.addPetIndex(SelectedPetIndex.newBuilder().setIndex(petData.getIndex()).setPetId(petData.getPetId()));
            }
            resultBuilder.addSelectedPet(builder);
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimSelectedPet_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_SelectedPet;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimSelectedPet_VALUE, SC_ClaimSelectedPet.newBuilder().setRetCode(retCode));
    }
}
