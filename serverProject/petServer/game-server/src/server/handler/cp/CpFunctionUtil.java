package server.handler.cp;

import cfg.CpTeamRobotCfg;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import model.cp.CpTeamCache;
import model.cp.CpTeamManger;
import model.cp.CpTeamMatchManger;
import model.cp.entity.CpCopyMap;
import model.cp.entity.CpTeamCopyPlayerProgress;
import model.cp.entity.CpTeamMember;
import model.cp.entity.CpTeamPublish;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.CpFunction;
import protocol.PetMessage;
import util.LogUtil;

public class CpFunctionUtil {

    @Getter
    private static final String robotIdStart = "cpRobot";

    public static CpFunction.CpFriendPlayer.Builder toClientFriend(String friendId) {
        playerEntity player = playerCache.getByIdx(friendId);
        if (player == null) {
            return null;
        }
        CpTeamMember playerInfo = CpTeamManger.getInstance().findPlayerInfo(friendId);
        CpFunction.CpFriendPlayer.Builder clientPlayer = CpFunction.CpFriendPlayer.newBuilder();
        clientPlayer.setPlayerIdx(player.getIdx());
        clientPlayer.setPlayerName(player.getName());
        clientPlayer.setPlayerLevel(player.getLevel());
        clientPlayer.setOfflineTime(player.queryOfflineTime());
        clientPlayer.setHeadId(player.getAvatar());
        clientPlayer.setBorderId(player.getDb_data().getCurAvatarBorder());
        if (playerInfo != null) {
            clientPlayer.setAbility(playerInfo.getAbility());
        }
        return clientPlayer;

    }

    public static CpFunction.CPTeamPlayer.Builder queryCPTeamPlayer(String playerIdx) {
        if (isRobot(playerIdx)) {
            return CpTeamRobotCfg.getInstance().getCpTeamByRobotId(parseRobotId(playerIdx));
        }

        CpTeamMember cpTeamMember = CpTeamCache.getInstance().loadPlayerInfo(playerIdx);
        if (cpTeamMember == null) {
            return null;
        }
        CpFunction.CPTeamPlayer.Builder builder = CpFunction.CPTeamPlayer.newBuilder();
        builder.setHeader(cpTeamMember.getHeader());
        builder.setPlayerIdx(cpTeamMember.getPlayerIdx());
        builder.setPlayerName(cpTeamMember.getPlayerName());
        builder.setAbility(cpTeamMember.getAbility());
        return builder;
    }

    public static CpFunction.InviteCpPlayer.Builder queryInviteCpPlayer(String playerIdx) {
        CpTeamMember cpTeamMember = CpTeamCache.getInstance().loadPlayerInfo(playerIdx);
        if (cpTeamMember == null) {
            return null;
        }
        CpFunction.InviteCpPlayer.Builder builder = CpFunction.InviteCpPlayer.newBuilder();
        builder.setPlayerIdx(cpTeamMember.getPlayerIdx());
        builder.setPlayerName(cpTeamMember.getPlayerName());
        builder.setHeader(cpTeamMember.getHeader());
        builder.setPlayerLevel(cpTeamMember.getPlayerLv());
        builder.setAbility(cpTeamMember.getAbility());
        return builder;
    }

    public static int queryPointFloor(int pointId) {
        return pointId / 100;
    }

    public static PetMessage.PetVo.Builder toPetVo(PetMessage.Pet monster) {
        PetMessage.PetVo.Builder petVo = PetMessage.PetVo.newBuilder();
        petVo.setPetLv(monster.getPetLvl());
        petVo.setPetId(monster.getPetBookId());
        petVo.setRarity(monster.getPetRarity());
        return petVo;
    }

    public static PetMessage.PetVo.Builder toPetVo(Battle.BattlePetData petDatum) {
        PetMessage.PetVo.Builder petVo = PetMessage.PetVo.newBuilder();
        petVo.setPetId(petDatum.getPetCfgId());
        petVo.setRarity(petDatum.getPetRarity());
        petVo.setPetLv(petDatum.getPetLevel());
        return petVo;

    }

    public static List<String> findPlayerIds(List<String> members) {
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        return members.stream().filter(e -> !isRobot(e)).collect(Collectors.toList());
    }

    public static boolean isRobot(String playerIdx) {
        return playerIdx != null && playerIdx.startsWith(robotIdStart);
    }

    public static String getRobotId(int id) {
        return getRobotIdStart() + id;
    }

    public static List<String> findRobotIds(List<String> members) {
        return members.stream().filter(CpFunctionUtil::isRobot).collect(Collectors.toList());
    }

    public static int generatePointId(int floor, int pointIndex) {
        return floor * 100 + pointIndex;
    }

    public static int parseRobotId(String robotIdx) {
        int robotId = 0;
        try {
            robotId = Integer.parseInt(robotIdx.replace(robotIdStart, ""));
        } catch (Exception ex) {
            LogUtil.printStackTrace(ex);
        }
        return robotId;
    }

    public static boolean isBattleFloor(int floor) {
        return floor % 2 == 1;
    }

    public static int getNextFloorPoint(int curPoint) {
        return curPoint + 100;
    }

    public static int randomRobotLv(int playerLv) {
        int result = playerLv + RandomUtils.nextInt(10) - 5;
        return Math.max(1, result);
    }

    public static long queryRobotAbility(String leaderId) {
        CpTeamMember member = CpTeamManger.getInstance().findPlayerInfo(leaderId);
        return (long) (member.getAbility() * 1.1);
    }

    public static CpFunction.CpPlayerTeam.Builder buildCpPlayerTeam(int teamId) {
        CpTeamPublish team = CpTeamCache.getInstance().loadTeamInfo(teamId);
        if (team == null) {
            return null;
        }
        CpFunction.CpPlayerTeam.Builder builder = CpFunction.CpPlayerTeam.newBuilder();
        builder.setTeamId(team.getTeamId());
        builder.setTeamName(team.getTeamName());
        for (String member : team.getMembers()) {
            CpFunction.CPTeamPlayer.Builder builder1 = CpFunctionUtil.queryCPTeamPlayer(member);
            if (builder1==null){
                continue;
            }
            if (builder1.getAbility() == 0) {
                builder1.setAbility(team.getPlayerAbility(builder1.getPlayerIdx()));
            }
            builder.addPlayers(builder1);
        }
        builder.setLeaderIdx(team.getLeaderIdx());
        builder.setActiveCopy(team.isActiveCopy());
        builder.setAutoJoin(team.isAutoJoin());
        builder.setAutoDisbandTime(CpTeamManger.getInstance().findTeamAutoDisbandTime(teamId));
        return builder;
    }

}
