package server.handler.cp.team;

import cfg.CpTeamRobotCfg;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.cp.CpTeamManger;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.CpFunction;
import protocol.CpFunction.CS_ClaimCpPlayers;
import protocol.CpFunction.SC_ClaimCpPlayers;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

/**
 * 拉取组队玩法中玩家
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimCpPlayers_VALUE)
public class ClaimCpPlayersHandler extends AbstractBaseHandler<CS_ClaimCpPlayers> {
    @Override
    protected CS_ClaimCpPlayers parse(byte[] bytes) throws Exception {
        return CS_ClaimCpPlayers.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimCpPlayers req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_ClaimCpPlayers.Builder msg = SC_ClaimCpPlayers.newBuilder();

        List<String> teamPlayers = CpTeamManger.getInstance().findTeamPlayers(playerIdx);
        for (String teamPlayer : teamPlayers) {
            CpFunction.CPTeamPlayer.Builder builder = CpFunctionUtil.queryCPTeamPlayer(teamPlayer);
            if (builder == null) {
                continue;
            }
            msg.addPlayers(builder);
        }

        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimCpPlayers_VALUE, msg);
    }


    private void putRobotData(SC_ClaimCpPlayers.Builder msg, String playerIdx) {
        int playerLv = PlayerUtil.queryPlayerLv(playerIdx);
        //添加两个机器人
        for (CpFunction.CpFriendPlayer robot : CpTeamRobotCfg.getInstance().randomRobot(2)) {
            //    msg.addPlayers(robot.toBuilder().setPlayerLevel(CpFunctionUtil.randomRobotLv(playerLv)).build());
        }
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimCpPlayers_VALUE, SC_ClaimCpPlayers.newBuilder().setRetCode(retCode));
    }
}
