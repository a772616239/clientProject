package server.handler.selectedPet;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.DrawCard.CS_CancelSelectedPet;
import protocol.DrawCard.SC_CancelSelectedPet;
import protocol.DrawCard.SC_CancelSelectedPet.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/07/01
 */
@MsgId(msgId = MsgIdEnum.CS_CancelSelectedPet_VALUE)
public class CancelSelectedPetHandler extends AbstractBaseHandler<CS_CancelSelectedPet> {
    @Override
    protected CS_CancelSelectedPet parse(byte[] bytes) throws Exception {
        return CS_CancelSelectedPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CancelSelectedPet req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity entity = playerCache.getByIdx(playerIdx);
        Builder resultBuilder = SC_CancelSelectedPet.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_CancelSelectedPet_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.cancelSelectedPet(req.getPetCfgId());
        });
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_CancelSelectedPet_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_SelectedPet;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CancelSelectedPet_VALUE, SC_CancelSelectedPet.newBuilder().setRetCode(retCode));
    }
}
