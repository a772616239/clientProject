package server.handler.cp.team;

import cfg.CpTeamRobotCfg;
import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import model.cp.entity.CpTeamPublish;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.CpFunction;
import protocol.CpFunction.CS_ClaimCanInviteCpPlayer;
import protocol.CpFunction.SC_ClaimCanInviteCpPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.handler.cp.CpFunctionUtil;
import util.GameUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 拉取可邀请的玩家列表
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimCanInviteCpPlayer_VALUE)
public class ClaimCanInviteCpPlayerHandler extends AbstractBaseHandler<CS_ClaimCanInviteCpPlayer> {


    @Override
    protected CS_ClaimCanInviteCpPlayer parse(byte[] bytes) throws Exception {
        return CS_ClaimCanInviteCpPlayer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimCanInviteCpPlayer req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_ClaimCanInviteCpPlayer.Builder msg = SC_ClaimCanInviteCpPlayer.newBuilder();
        addCanInviteFriend(msg, playerIdx);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimCanInviteCpPlayer_VALUE, msg);
    }

    private void addCanInviteFriend(SC_ClaimCanInviteCpPlayer.Builder msg, String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        putRobotData(msg, playerIdx);

        putFriendsData(msg, player);
    }

    private void putFriendsData(SC_ClaimCanInviteCpPlayer.Builder msg, playerEntity player) {
        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
        for (String friendId : player.getFriendIds()) {
            if (!allOnlinePlayerIdx.contains(friendId)) {
                continue;
            }
            if (!CpTeamManger.getInstance().canInvite(friendId)) {
                continue;
            }
            CpFunction.CpFriendPlayer.Builder friendInfo = CpFunctionUtil.toClientFriend(friendId);
            if (friendInfo == null) {
                continue;
            }
            msg.addPlayers(friendInfo);

        }
    }

    private void putRobotData(SC_ClaimCanInviteCpPlayer.Builder msg, String playerIdx) {
        CpTeamPublish team = CpTeamManger.getInstance().findPlayerJoinTeam(playerIdx);
        if (team == null) {
            return;
        }
        int playerLv = PlayerUtil.queryPlayerLv(playerIdx);
        List<CpFunction.CpFriendPlayer> allRobot = CpTeamRobotCfg.getInstance().getAllRobot();
        Collections.shuffle(allRobot);
        if (CollectionUtils.isEmpty(allRobot)) {
            return;
        }
        for (CpFunction.CpFriendPlayer robot : allRobot) {
            if (!team.getMembers().contains(robot.getPlayerIdx())) {
                Long temp = team.getPlayerAbility().get(robot.getPlayerIdx());
                long robotAbility = temp != null ? temp : 0;
                msg.addPlayers(robot.toBuilder().setAbility(robotAbility).setPlayerLevel(CpFunctionUtil.randomRobotLv(playerLv)).build());
                if (msg.getPlayersCount() >= 2) {
                    return;
                }
            }
        }
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimCanInviteCpPlayer_VALUE, SC_ClaimCanInviteCpPlayer.newBuilder().setRetCode(retCode));
    }
}
