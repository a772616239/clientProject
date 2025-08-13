package server.handler.ancientCall;

import cfg.PetBaseProperties;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.AncientCall.CS_EnsureTransfer;
import protocol.AncientCall.SC_EnsureTransfer;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.PlayerInfo.PetTransferInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_EnsureTransfer_VALUE)
public class EnsureTransferHandler extends AbstractBaseHandler<CS_EnsureTransfer> {
    @Override
    protected CS_EnsureTransfer parse(byte[] bytes) throws Exception {
        return CS_EnsureTransfer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_EnsureTransfer req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        boolean ensure = req.getEnsure();

        SC_EnsureTransfer.Builder resultBuilder = SC_EnsureTransfer.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null || player.getDb_data() == null) {
            LogUtil.error("EnsureTransferHandler, playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_EnsureTransfer_VALUE, resultBuilder);
            return;
        }

        String srcPetIdx = null;
        int targetBookId = 0;
        //清除转化数据
        try {
            player.lockObj();

            Builder db_data = player.getDb_data();
            if (db_data != null) {
                PetTransferInfo petTransfer = db_data.getAncientAltarBuilder().getPetTransfer();
                srcPetIdx = petTransfer.getSrcPetIdx();
                targetBookId = petTransfer.getTargetPetCfgId();

                db_data.getAncientAltarBuilder().clearPetTransfer();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        } finally {
            player.unlockObj();
        }

        if (model.pet.dbCache.petCache.getInstance().getPetById(playerIdx, srcPetIdx) == null || PetBaseProperties.getByPetid(targetBookId) == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_PetTramsfer_HavaNoPetInTrsnsfer));
            gsChn.send(MsgIdEnum.SC_EnsureTransfer_VALUE, resultBuilder);
            return;
        }

        if (ensure) {
            if (petCache.getInstance().petTransfer(playerIdx, srcPetIdx, targetBookId)) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            } else {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            }
        } else {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        }
        gsChn.send(MsgIdEnum.SC_EnsureTransfer_VALUE, resultBuilder);

        //宠物状态重置
        petCache.getInstance().petChange(playerIdx, srcPetIdx, false);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.AncientCall;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_EnsureTransfer_VALUE, SC_EnsureTransfer.newBuilder().setRetCode(retCode));
    }
}
