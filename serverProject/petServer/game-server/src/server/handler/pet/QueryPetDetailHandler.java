package server.handler.pet;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import model.petrune.dbCache.petruneCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common;
import protocol.MessageId;
import protocol.PetMessage;
import protocol.RetCodeId;
import util.GameUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@MsgId(msgId = MessageId.MsgIdEnum.CS_QueryPetDetail_VALUE)
public class QueryPetDetailHandler extends AbstractBaseHandler<PetMessage.CS_QueryPetDetail> {


    @Override
    protected PetMessage.CS_QueryPetDetail parse(byte[] bytes) throws Exception {
        return PetMessage.CS_QueryPetDetail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PetMessage.CS_QueryPetDetail req, int i) {

        PetMessage.SC_QueryPetDetail.Builder msg = PetMessage.SC_QueryPetDetail.newBuilder();
        String petId = req.getPetId();
        String playerId = req.getPlayerId();

        PetMessage.Pet pet = petCache.getInstance().getPetById(playerId, petId);

        if (pet == null) {
            msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Pet_RankingPetNotExist));
            gsChn.send(MessageId.MsgIdEnum.SC_QueryPetDetail_VALUE, msg);
            return;
        }

        playerEntity player = playerCache.getByIdx(playerId);
        msg.setPlayerId(playerId);
        if (player != null) {
            Map<Integer, Integer> additionMap = player.getDb_data().getGlobalAddition().getArtifactAdditionMap();
            msg.addAllArtifact(player.getSimpleArtifact());
            msg.addAllArtifactAdditionValues(additionMap.values());
            msg.addAllNewTitleId(player.getPlayerAllTitleIds());
            msg.addAllArtifactAdditionKeys(additionMap.keySet());

        }
        msg.setPet(buildClientShowPet(pet, player));
        List<PetMessage.Rune> runes = petruneCache.getInstance().getPetRune(playerId, petId);
        msg.addAllRunes(runes);

        PetMessage.Gem gem = petgemCache.getInstance().getGemByGemIdx(playerId, pet.getGemId());
        if (gem != null) {
            msg.setGemCfgId(gem.getGemConfigId());
        }
        msg.setResult(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_QueryPetDetail_VALUE, msg);
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.PetLock;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_QueryPetDetail_VALUE, PetMessage.SC_PetLock.newBuilder().setResult(retCode));
    }

    private PetMessage.Pet.Builder buildClientShowPet(PetMessage.Pet pet, playerEntity player) {
        PetMessage.Pet.Builder showPet = pet.toBuilder();
        long addition = player == null ? 0 : player.getDb_data().getPetAbilityAddition();
        Map<Integer, Integer> additionMap = player == null ? Collections.emptyMap() : player.getDb_data().getPetPropertyAdditionMap();
        PetMessage.PetProperties petProperties = petCache.getInstance().refreshProperty(showPet.getPetProperty(), additionMap);
        showPet.setPetProperty(petProperties);
        showPet.setAbility(pet.getAbility() + addition);
        return showPet;
    }
}
