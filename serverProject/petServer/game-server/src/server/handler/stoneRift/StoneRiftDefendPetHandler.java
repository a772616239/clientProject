package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.RetCodeId;
import protocol.StoneRift.CS_StoneRiftDefendPet;
import protocol.StoneRift.SC_StoneRiftDefendPet;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_StoneRiftDefendPet_VALUE;

/**
 * 驻防宠物
 */
@MsgId(msgId = MsgIdEnum.CS_StoneRiftDefendPet_VALUE)
public class StoneRiftDefendPetHandler extends AbstractBaseHandler<CS_StoneRiftDefendPet> {

    @Override
    protected CS_StoneRiftDefendPet parse(byte[] bytes) throws Exception {
        return CS_StoneRiftDefendPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_StoneRiftDefendPet req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_StoneRiftDefendPet.Builder msg = doDefendPet(playerId, req);

        GlobalData.getInstance().sendMsg(playerId, SC_StoneRiftDefendPet_VALUE, msg);

    }

    private SC_StoneRiftDefendPet.Builder doDefendPet(String playerId, CS_StoneRiftDefendPet req) {
        SC_StoneRiftDefendPet.Builder msg = SC_StoneRiftDefendPet.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }
        DbStoneRiftFactory factory = entity.getDB_Builder().getFactoryMap().get(req.getFactoryId());
        if (factory == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_FactoryNotUnlock));
            return msg;
        }
        PetMessage.Pet pet = petCache.getInstance().getPetById(playerId, req.getPetId());
        if (pet == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Pet_PetNotExist));
            return msg;
        }
        if (factory.getLevel() < StoneRiftCfgManager.getInstance().getDefendNeedRiftLv()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_RiftLvNotEnough));
            return msg;
        }
        if (entity.getDB_Builder().getDefendPet().containsValue(req.getPetId())) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_PetAlreadyInDefend));
            return msg;
        }
        if (GameUtil.inScope(pet.getPetRarity(), entity.getCanMinDefendPetRarity(), entity.getCanMaxDefendPetRarity())) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_PetNotMatch));
            return msg;
        }
        SyncExecuteFunction.executeConsumer(entity, et -> {
            entity.deFendPet(pet, factory);
        });
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        return msg;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_StoneRiftDefendPet_VALUE, SC_StoneRiftDefendPet.newBuilder().setRetCode(retCode));

    }
}
