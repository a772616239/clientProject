package server.handler.thewar;

import cfg.TheWarMapConfig;
import cfg.TheWarMapConfigObject;
import common.AbstractBaseHandler;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import model.warpServer.crossServer.CrossServerManager;
import org.springframework.util.CollectionUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.ServerTransfer.GS_CS_AddNewWarPetData;
import protocol.ServerTransfer.WarBattlePet;
import protocol.TheWar.CS_AddNewPetToWar;
import protocol.TheWar.SC_AddNewPetToWar;
import protocol.TheWar.WarPetPosData;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_AddNewPetToWar_VALUE)
public class AddPetToWarHandler extends AbstractBaseHandler<CS_AddNewPetToWar> {
    @Override
    protected CS_AddNewPetToWar parse(byte[] bytes) throws Exception {
        return CS_AddNewPetToWar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_AddNewPetToWar req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_AddNewPetToWar.Builder retBuilder = SC_AddNewPetToWar.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_AddNewPetToWar_VALUE, retBuilder);
            return;
        }
        TheWarMapConfigObject mapCfg = TheWarMapConfig.getByMapname(TheWarManager.getInstance().getMapName());
        if (mapCfg == null) {
            SC_AddNewPetToWar.Builder retBuilder = SC_AddNewPetToWar.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap);
            gsChn.send(MsgIdEnum.SC_AddNewPetToWar_VALUE, retBuilder);
            return;
        }
        Pet pet;
        List<Pet> petList = new ArrayList<>();

        GS_CS_AddNewWarPetData.Builder builder = GS_CS_AddNewWarPetData.newBuilder();
        builder.setPlayerIdx(playerIdx);
        for (WarPetPosData petPosData : req.getPetPosDataList()) {
            pet = petCache.getInstance().buildReviseLevelPet(playerIdx, petPosData.getPetIdx(), mapCfg.getPetverifylevel());
            if (pet == null) {
                SC_AddNewPetToWar.Builder retBuilder = SC_AddNewPetToWar.newBuilder();
                retBuilder.setRetCode(TheWarRetCode.TWRC_InvalidPet); // 非法宠物，存在非法宠物或修正等级错误
                gsChn.send(MsgIdEnum.SC_AddNewPetToWar_VALUE, retBuilder);
                return;
            }
            petList.clear();
            petList.add(pet);
            List<BattlePetData> petDataList = petCache.getInstance().buildPlayerPetBattleData(playerIdx, petList, BattleSubTypeEnum.BSTE_TheWar);
            if (CollectionUtils.isEmpty(petDataList)) {
                SC_AddNewPetToWar.Builder retBuilder = SC_AddNewPetToWar.newBuilder();
                retBuilder.setRetCode(TheWarRetCode.TWRC_ExistInvalidPet); // 更新队伍中含有未携带的宠物
                gsChn.send(MsgIdEnum.SC_AddNewPetToWar_VALUE, retBuilder);
                return;
            }

            WarBattlePet.Builder warPet = WarBattlePet.newBuilder();
            warPet.setPetData(petDataList.get(0));
            warPet.setPos(petPosData.getIndexToAdd());
            builder.addWarBattlePets(warPet);
        }
        if (!CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_AddNewWarPetData_VALUE, builder)) {
            SC_AddNewPetToWar.Builder retBuilder = SC_AddNewPetToWar.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_AddNewPetToWar_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_AddNewPetToWar_VALUE,
                SC_AddNewPetToWar.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
