package server.handler.selectedPet;

import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.DrawCard.CS_SetSelectedPet;
import protocol.DrawCard.SC_SetSelectedPet;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/07/01
 */
@MsgId(msgId = MsgIdEnum.CS_SetSelectedPet_VALUE)
public class SelectedPetHandler extends AbstractBaseHandler<CS_SetSelectedPet> {
    @Override
    protected CS_SetSelectedPet parse(byte[] bytes) throws Exception {
        return CS_SetSelectedPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SetSelectedPet req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        playerEntity entity = playerCache.getByIdx(playerIdx);
        SC_SetSelectedPet.Builder resultBuilder = SC_SetSelectedPet.newBuilder();

        PetBasePropertiesObject petCfg = PetBaseProperties.getByPetid(req.getPetId());
        if (entity == null || petCfg == null || !petCfg.getIsoptional()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_SetSelectedPet_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            RetCodeEnum setResult = entity.setDrawCardSelectedPet(req.getPetId(), req.getIndex());
            resultBuilder.setRetCode(GameUtil.buildRetCode(setResult));
            gsChn.send(MsgIdEnum.SC_SetSelectedPet_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_SelectedPet;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_SetSelectedPet_VALUE, SC_SetSelectedPet.newBuilder().setRetCode(retCode));
    }
}
