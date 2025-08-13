package server.handler.crazyDuel;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.CrazyDuelOpenManager;
import model.crazyDuel.dto.CrazyDuelPlayerPageDB;
import org.springframework.util.CollectionUtils;
import protocol.*;
import protocol.CrayzeDuel.CS_QueryCrazyDuelPlayerDetail;
import protocol.CrayzeDuel.SC_QueryCrazyDuelPlayerDetail;
import util.GameUtil;

import java.util.ArrayList;
import java.util.List;

@MsgId(msgId = MessageId.MsgIdEnum.CS_QueryCrazyDuelPlayerDetail_VALUE)
public class QueryCrazyDuelPlayerDetailHandler extends AbstractBaseHandler<CS_QueryCrazyDuelPlayerDetail> {

    @Override
    protected CS_QueryCrazyDuelPlayerDetail parse(byte[] bytes) throws Exception {
        return CS_QueryCrazyDuelPlayerDetail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryCrazyDuelPlayerDetail req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        SC_QueryCrazyDuelPlayerDetail.Builder msg = SC_QueryCrazyDuelPlayerDetail.newBuilder();
        if (!CrazyDuelOpenManager.getInstance().isOpen()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_QueryCrazyDuelPlayerDetail_VALUE, msg);
            return;
        }

        CrazyDuelDB.CrazyDuelSettingDB playerSetting = CrazyDuelManager.getInstance().findPlayerSetting(req.getPlayerIdx());

        setPlayerTeams(playerIdx, req.getPlayerIdx(), msg, playerSetting);

        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_QueryCrazyDuelPlayerDetail_VALUE, msg);
    }

    private void setPlayerTeams(String playerIdx, String queryPlayer, SC_QueryCrazyDuelPlayerDetail.Builder msg, CrazyDuelDB.CrazyDuelSettingDB playerSetting) {
        int curFloor = CrazyDuelManager.getInstance().findPlayerCurBattleFloor(playerIdx, queryPlayer);
        for (CrayzeDuel.CrazyDuelBuffSetting buffSet : playerSetting.getBuffSettingMap().values()) {
            msg.addTeams(buffSet);
        }
        List<PetMessage.PetVo> favoritePets = teamsToPetsVo(playerSetting.getBattleDataList());
        if (!CollectionUtils.isEmpty(favoritePets)) {
            msg.addAllShowPet(favoritePets);
        }

        CrazyDuelPlayerPageDB playerInfo = CrazyDuelManager.getInstance().findPagePlayerById(queryPlayer);
        msg.setDuelCount(playerInfo.getDuelCount());
        msg.setSuccessRate(playerInfo.getSuccessRate());
        msg.setAvatarBorderId(playerInfo.getHeadBorderId());
        msg.setPlayerIdx(playerInfo.getPlayerId());

        msg.setHonorLv(playerInfo.getHonLv());
        msg.setHeader(playerInfo.getHeadId());
        msg.setPlayerName(playerInfo.getName());
        msg.setAbility(playerInfo.getAbility());
        msg.setCurBattleFloor(curFloor);
        msg.setScore(CrazyDuelManager.getInstance().findPlayerScore(queryPlayer));
    }


    private List<PetMessage.PetVo> teamsToPetsVo(List<Battle.BattlePetData> playerTeams) {

        List<PetMessage.PetVo> petVos = new ArrayList<>();
        for (Battle.BattlePetData battlePetData : playerTeams) {
            petVos.add(PetMessage.PetVo.newBuilder().setPetId(battlePetData.getPetCfgId())
                    .setPetLv(battlePetData.getPetLevel()).setRarity(battlePetData.getPetRarity()).build());

        }
        return petVos;

    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_QueryCrazyDuelPlayerDetail_VALUE, SC_QueryCrazyDuelPlayerDetail.newBuilder().setRetCode(retCode));
    }


}
