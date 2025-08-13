package server.handler.crazyDuel;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.CrazyDuelOpenManager;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.team.util.UpdateTeamUtil;
import org.springframework.util.CollectionUtils;
import protocol.*;
import protocol.CrayzeDuel.CS_SettingCrazyDuelTeam;
import protocol.CrayzeDuel.SC_SettingCrazyDuelTeam;
import util.GameUtil;

import java.util.ArrayList;
import java.util.List;

@MsgId(msgId = MessageId.MsgIdEnum.CS_SettingCrazyDuelTeam_VALUE)
public class SettingCrazyDuelTeamHandler extends AbstractBaseHandler<CS_SettingCrazyDuelTeam> {
    @Override
    protected CS_SettingCrazyDuelTeam parse(byte[] bytes) throws Exception {
        return CS_SettingCrazyDuelTeam.parseFrom(bytes);
    }


    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SettingCrazyDuelTeam req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_SettingCrazyDuelTeam.Builder msg = SC_SettingCrazyDuelTeam.newBuilder();

        if (!CrazyDuelOpenManager.getInstance().isOpen()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_SettingCrazyDuelTeam_VALUE, msg);
            return;
        }
        RetCodeId.RetCodeEnum retCodeEnum = checkPet(playerIdx, req.getMapsList());

        if (retCodeEnum == RetCodeId.RetCodeEnum.RCE_Success) {
            CrazyDuelManager.getInstance().saveSetting(playerIdx,req.getMapsList());
        }
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));

        gsChn.send(MessageId.MsgIdEnum.SC_SettingCrazyDuelTeam_VALUE, msg);
    }




    private RetCodeId.RetCodeEnum checkPet(String playerIdx, List<PrepareWar.PositionPetMap> mapsList) {
        if (CollectionUtils.isEmpty(mapsList)) {
            //没有宠物
            return RetCodeId.RetCodeEnum.RCE_ErrorParam;
        }
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (petEntity == null) {
            return RetCodeId.RetCodeEnum.RCE_UnknownError;
        }

        List<Integer> temPet = new ArrayList<>();
        for (PrepareWar.PositionPetMap positionPetMap :mapsList) {
            PetMessage.Pet pet = petEntity.getPetById(positionPetMap.getPetIdx());
            if (pet == null) {
                return RetCodeId.RetCodeEnum.RCE_PrepareWar_PetNoExist;
            }
            if (temPet.contains(pet.getPetBookId())) {
                return RetCodeId.RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
            }
            //如果当前宠物处于宠物转化中,不允许编队
            if (UpdateTeamUtil.isInPetTransfer(playerIdx, positionPetMap.getPetIdx())) {
                return RetCodeId.RetCodeEnum.RCE_PrepareWar_PetIsInPetTransfer;
            }
            temPet.add(pet.getPetBookId());
        }
        return RetCodeId.RetCodeEnum.RCE_Success;

    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_SettingCrazyDuelTeam_VALUE, SC_SettingCrazyDuelTeam.newBuilder().setRetCode(retCode));
    }
}
