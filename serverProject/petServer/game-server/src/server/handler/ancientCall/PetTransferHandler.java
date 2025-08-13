package server.handler.ancientCall;

import cfg.GameConfig;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.ancientCall.PetTransferManager;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.ReasonManager;
import protocol.AncientCall.CS_PetTransfer;
import protocol.AncientCall.SC_PetTransfer;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.PlayerInfo.PetTransferInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_PetTransfer_VALUE)
public class PetTransferHandler extends AbstractBaseHandler<CS_PetTransfer> {
    @Override
    protected CS_PetTransfer parse(byte[] bytes) throws Exception {
        return CS_PetTransfer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetTransfer req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        String petIdx = req.getPetIdx();

        SC_PetTransfer.Builder resultBuilder = SC_PetTransfer.newBuilder();
        Pet petById = petCache.getInstance().getPetById(playerIdx, petIdx);
        if (petById == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_PetNotExist));
            gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, resultBuilder);
            return;
        }

        //编队中不可转化
        if (petById.getPetTeamStatus() > 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AncientCall_PetInTeam));
            gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, resultBuilder);
            return;
        }

        //宠物配置
        PetBasePropertiesObject petCfg = PetBaseProperties.getByPetid(petById.getPetBookId());
        if (petCfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, resultBuilder);
            return;
        }

        //消耗资源,如果根据对应的稀有度未拿到消耗证明为不支持转化类型,消耗根据当前品质决定,且可以转化
        Consume petTransferConsume = getPetTransferConsume(petById.getPetRarity());
        if (petTransferConsume == null || !PetTransferManager.getInstance().canTransfer(petById.getPetBookId())) {
            LogUtil.info("PetTransferHandler, pet rarity = " + petCfg.getStartrarity() + "can not transfer, consume is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_PetTransfer_UnsupportedTransferType));
            gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, resultBuilder);
            return;
        }

        //先转化看是否转化失败
        int targetPetCfgId = PetTransferManager.getInstance().doTransfer(petById.getPetBookId(), petCfg.getStartrarity(), petCfg.getPettype());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null || targetPetCfgId == -1) {
            LogUtil.error("PetTransferHandler, playerIdx[" + playerIdx + "], petIdx [" + petIdx + "] transfer failed");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, resultBuilder);
            return;
        }

        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, petTransferConsume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_AncientCall, "转化"))) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, resultBuilder);
            return;
        }

        //设置转化数据,设置宠物状态
        petCache.getInstance().petChange(playerIdx, petIdx, true);
        SyncExecuteFunction.executeConsumer(player, entity -> {
            Builder db_data = player.getDb_data();
            if (db_data == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, resultBuilder);
                return;
            }

            PetTransferInfo.Builder petTransferBuilder = db_data.getAncientAltarBuilder().getPetTransferBuilder();
            petTransferBuilder.setSrcPetIdx(petIdx);
            petTransferBuilder.setTargetPetCfgId(targetPetCfgId);
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setTargetPetId(targetPetCfgId);
        gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, resultBuilder);

        //目标：累积进行x次x品质宠物类型转化
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuPetTransfer, 1, petCfg.getStartrarity());
    }

    private Consume getPetTransferConsume(int rarity) {
        int[][] consumeList = GameConfig.getById(GameConst.CONFIG_ID).getPettransferconsume();
        if (consumeList == null) {
            return null;
        }

        for (int[] ints : consumeList) {
            if (ints.length != 4) {
                LogUtil.error("gameCfg, getPetTransferConsume cfg error, length is not match 4");
            }
            if (ints[0] == rarity) {
                return ConsumeUtil.parseConsume(ints[1], ints[2], ints[3]);
            }
        }
        return null;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.AncientCall;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetTransfer_VALUE, SC_PetTransfer.newBuilder().setRetCode(retCode));
    }
}
