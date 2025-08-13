package server.handler.crazyDuel;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import java.util.Map;

import model.crazyDuel.CrazyDuelManager;
import model.crazyDuel.CrazyDuelOpenManager;
import model.crazyDuel.dto.CrazyDuelPlayerPageDB;
import model.crazyDuel.entity.CrazyDuelPlayerDB;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import protocol.Common;
import protocol.CrayzeDuel;
import protocol.CrayzeDuel.CS_ClaimLobbyTeams;
import protocol.CrayzeDuel.SC_ClaimLobbyTeams;
import protocol.MessageId;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimLobbyTeams_VALUE)
public class ClaimLobbyTeamHandler extends AbstractBaseHandler<CS_ClaimLobbyTeams> {

    @Override
    protected CS_ClaimLobbyTeams parse(byte[] bytes) throws Exception {
        return CS_ClaimLobbyTeams.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimLobbyTeams req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        SC_ClaimLobbyTeams.Builder msg = SC_ClaimLobbyTeams.newBuilder();
        if (!CrazyDuelOpenManager.getInstance().isOpen()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimLobbyTeams_VALUE, msg);
            return;
        }
        List<CrazyDuelPlayerPageDB> crazyDuelPlayerPageDBS = CrazyDuelManager.getInstance().claimLobbyTeam(playerIdx);

        CrazyDuelPlayerDB playerDb = CrazyDuelManager.getInstance().findPlayerDb(playerIdx);
        if (playerDb == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimLobbyTeams_VALUE, msg);
            return;
        }
        Map<String, Integer> scoreAddition = playerDb.getScoreAddition();
        for (CrazyDuelPlayerPageDB db : crazyDuelPlayerPageDBS) {
            msg.addTeams(toVo(db, playerDb.getDefeatPlayer(),scoreAddition));
        }
        msg.addAllChooseOpponent(playerDb.getChoosePlayers());
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimLobbyTeams_VALUE, msg);
    }

    public static CrayzeDuel.CrazyLobbyTeam.Builder toVo(CrazyDuelPlayerPageDB db, List<String> defeatPlayer, Map<String, Integer> scoreAddition) {
        CrayzeDuel.CrazyLobbyTeam.Builder vo = CrayzeDuel.CrazyLobbyTeam.newBuilder();
        vo.setPlayerIdx(db.getPlayerId());
        vo.setPlayerName(db.getName());
        vo.setDuelCount(db.getDuelCount());
        vo.setSuccessRate(db.getSuccessRate());
        vo.setHeader(db.getHeadId());
        vo.setAvatarBorderId(db.getHeadBorderId());
        vo.setAbility(db.getAbility());
        Integer addition = scoreAddition.get(db.getPlayerId());
        if (addition != null) {
            vo.setScoreAddition(addition);
        }
        if (!CollectionUtils.isEmpty(defeatPlayer) && defeatPlayer.contains(db.getPlayerId())) {
            vo.setFinishChallenge(true);
        }
        vo.setHonorLv(db.getHonLv());
        vo.setScore(CrazyDuelManager.getInstance().findPlayerScore(db.getPlayerId()));
        return vo;
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimLobbyTeams_VALUE, SC_ClaimLobbyTeams.newBuilder().setRetCode(retCode));
    }
}
