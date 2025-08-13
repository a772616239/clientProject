package model.mistforest.team;

import model.mistforest.MistConst;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_AddObj;
import protocol.MistForest.BattleCMD_RemoveObj;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistBriefTeamInfo;
import protocol.MistForest.MistTeamInfo;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.SC_MistForestTeamInfo;
import protocol.MistForest.SC_UpdateMistTeamList;
import protocol.MistForest.UnitMetadata;
import util.GameUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MistTeam {
    private int teamId;
    private MistRoom room;
    private Map<Long, MistFighter> members;
    private long leaderId;
    private long teamFightPower;

    public MistTeam(MistRoom room) {
        this.room = room;
        this.members = new HashMap<>();
    }

    public void clear() {
        members.clear();
        leaderId = 0;
        teamFightPower = 0;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(long leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderName() {
        MistFighter fighter = room.getObjManager().getMistObj(getLeaderId());
        if (fighter == null) {
            return "";
        }
        MistPlayer leader = fighter.getOwnerPlayerInSameRoom();
        if (leader == null) {
            return "";
        }
        return leader.getName();
    }

    public long getTeamFightPower() {
        return teamFightPower;
    }

    public void setTeamFightPower(long teamFightPower) {
        this.teamFightPower = teamFightPower;
    }

    public Map<Long, MistFighter> getAllMembers() {
        return members;
    }

    public List<UnitMetadata> getTeamMetaData(long fighterId) {
        List<UnitMetadata> metadataList = null;
        for (MistFighter fighter : members.values()) {
            if (fighter.getId() == fighterId) {
                continue;
            }
            if (metadataList == null) {
                metadataList = new ArrayList<>();
            }
            metadataList.add(fighter.getMetaData(fighter));
        }
        return metadataList;
    }

    public boolean addMembers(MistFighter newMember, long fightPower) {
        if (members.size() >= MistConst.MistRoomMaxTeamMemberSize) {
            return false;
        }
        members.put(newMember.getId(), newMember);
        if (!members.containsKey(leaderId)) {
            leaderId = newMember.getId();
        }
        setTeamFightPower(Math.max(0, getTeamFightPower() + fightPower));
        updateTeamInfo(null);
        updateTeamBriefInfo();
        addPlayerTeamInfo(newMember);
        return true;
    }

    public void removeMembers(MistFighter fighter, long fightPower) {
        members.remove(fighter.getId());
        if (leaderId == fighter.getId()) {
            if (members.isEmpty()) {
                leaderId = 0;
            } else {
                for (MistFighter fighter1 : members.values()) {
                    if (fighter1.getId() != fighter.getId()) {
                        leaderId = fighter1.getId();
                        break;
                    }
                }
            }
        }
        setTeamFightPower(Math.max(0, getTeamFightPower() - fightPower));
        updateTeamInfo(null);
        updateTeamBriefInfo();
        removePlayerTeamInfo(fighter);
    }

    public int getTeamMemberCount() {
        return members.size();
    }

    public boolean isTeamFull() {
        return members.size() >= MistConst.MistRoomMaxTeamMemberSize;
    }

    public boolean isTeamEmpty() {
        return leaderId == 0 && members.isEmpty();
    }

    public boolean isTeammate(long fighter1, long fighter2) {
        return members.get(fighter1) != null && members.get(fighter2) != null;
    }

    public void updateTeamInfo(MistPlayer player) {
        if (members.isEmpty()) {
            return;
        }
        List<MistPlayer> oldMemberList = null;
        if (player == null) {
            oldMemberList = new ArrayList<>();
        }
        SC_MistForestTeamInfo.Builder teamBuilder = SC_MistForestTeamInfo.newBuilder();
        teamBuilder.setTeamInfo(buildTeamInfo(oldMemberList));
        if (player != null) {
            player.sendMsgToServer(MsgIdEnum.SC_MistForestTeamInfo_VALUE, teamBuilder);
        } else if (oldMemberList != null) {
            for (MistPlayer memberPly : oldMemberList) {
                memberPly.sendMsgToServer(MsgIdEnum.SC_MistForestTeamInfo_VALUE, teamBuilder);
            }
        }
    }

    public void updateTeamBriefInfo() {
        SC_UpdateMistTeamList.Builder builder = SC_UpdateMistTeamList.newBuilder();
        builder.setNeedClear(false);
        MistBriefTeamInfo.Builder teamBuilder = MistBriefTeamInfo.newBuilder();
        teamBuilder.setTeamId(getTeamId());
        MistPlayer player;
        for (MistFighter fighter : members.values()) {
            player = fighter.getOwnerPlayerInSameRoom();
            if (player == null) {
                continue;
            }
            teamBuilder.addMemberIds(player.getIdx());
        }
        teamBuilder.setTeamFightPower(getTeamFightPower());
        teamBuilder.setLeaderName(getLeaderName());
        builder.addTeamList(teamBuilder);
        room.broadcastMsg(MsgIdEnum.SC_UpdateMistTeamList_VALUE, builder, true);
    }

    public MistTeamInfo.Builder buildTeamInfo(List<MistPlayer> oldMemberList) {
        MistTeamInfo.Builder teamInfo = MistTeamInfo.newBuilder();
        if (members.isEmpty()) {
            return teamInfo;
        }
        teamInfo.setTeamId(getTeamId());
        String plyId;
        MistPlayer player;
        for (MistFighter member : members.values()) {
            plyId = GameUtil.longToString(member.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), "");
            player = MistPlayerCache.getInstance().queryObject(plyId);
            if (player == null) {
                continue;
            }
            if (member.getId() == getLeaderId()) {
                teamInfo.setLeaderIdx(player.getIdx());
            }
            teamInfo.addTeamMember(player.buildMistPlayerInfo());

            if (oldMemberList != null) {
                oldMemberList.add(player);
            }
        }
        return teamInfo;
    }

    public void addPlayerTeamInfo(MistFighter fighter) {
        if (members.isEmpty()) {
            return;
        }
        String plyId = GameUtil.longToString(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), "");
        MistPlayer newPlayer = MistPlayerCache.getInstance().queryObject(plyId);
        if (newPlayer == null) {
            return;
        }
        // 新成员对象信息
        SC_BattleCmd.Builder newObjBuilder = SC_BattleCmd.newBuilder();
        newObjBuilder.addCMDList(fighter.buildCreateObjCmd());

        MistPlayer tmpPlayer;
        SC_BattleCmd.Builder oldObjBuilder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder oldObjCmdBuilder = BattleCmdData.newBuilder();
        oldObjCmdBuilder.setCMDType(MistBattleCmdEnum.MBC_AddObj);
        BattleCMD_AddObj.Builder addOldObjBuilder = BattleCMD_AddObj.newBuilder();
        for (MistFighter member : members.values()) {
            plyId = GameUtil.longToString(member.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), "");
            tmpPlayer = MistPlayerCache.getInstance().queryObject(plyId);
            if (tmpPlayer == null || fighter.getId() == member.getId()) {
                continue;
            }
            if (fighter.isNearPlayer(member)) {
                continue;
            }
            tmpPlayer.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, newObjBuilder);
            addOldObjBuilder.addObjsMetaData(member.getMetaData(fighter));
        }
        if (addOldObjBuilder.getObjsMetaDataCount() <= 0) {
            return;
        }
        oldObjCmdBuilder.setCMDContent(addOldObjBuilder.build().toByteString());
        oldObjBuilder.addCMDList(oldObjCmdBuilder);
        newPlayer.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, oldObjBuilder);
    }

    public void removePlayerTeamInfo(MistFighter fighter) {
        if (members.isEmpty()) {
            return;
        }
        long exitPlyId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        MistPlayer exitPlayer = MistPlayerCache.getInstance().queryObject(GameUtil.longToString(exitPlyId, ""));
        if (exitPlayer == null) {
            return;
        }
        // 退队对象信息
        SC_BattleCmd.Builder exitObjBuilder = SC_BattleCmd.newBuilder();
        exitObjBuilder.addCMDList(fighter.buildRemoveObjCmd());

        long tmpPlayerId;
        MistPlayer tmpPlayer;
        SC_BattleCmd.Builder memberObjBuilder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder memberObjCmdBuilder = BattleCmdData.newBuilder();
        memberObjCmdBuilder.setCMDType(MistBattleCmdEnum.MBC_RemoveObj);
        BattleCMD_RemoveObj.Builder removeMemberObjBuilder = BattleCMD_RemoveObj.newBuilder();
        for (MistFighter member : members.values()) {
            tmpPlayerId = member.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
            tmpPlayer = MistPlayerCache.getInstance().queryObject(GameUtil.longToString(tmpPlayerId, ""));
            if (tmpPlayer == null || fighter.getId() == member.getId()) {
                continue;
            }
            if (fighter.isNearPlayer(member)) {
                continue;
            }
            tmpPlayer.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, exitObjBuilder);
            removeMemberObjBuilder.addObjIds(member.getId());
        }
        if (removeMemberObjBuilder.getObjIdsCount() <= 0) {
            return;
        }
        memberObjCmdBuilder.setCMDContent(removeMemberObjBuilder.build().toByteString());
        memberObjBuilder.addCMDList(memberObjCmdBuilder);
        exitPlayer.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, memberObjBuilder);
    }

    public void updateMemberCmd(SC_BattleCmd.Builder command, long fighterId) {
        // fighterId为0时转发给所有人, 不为0时转发给其他所有人
        long playerId;
        MistPlayer player;
        for (MistFighter member : members.values()) {
            if (member.getId() == fighterId) {
                continue;
            }
            if (member.isBattling()) {
                continue;
            }
            playerId = member.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
            player = MistPlayerCache.getInstance().queryObject(GameUtil.longToString(playerId, ""));
            if (player != null) {
                player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, command);
            }
        }
    }

    public Map<Integer, Set<String>> getTeamIpPlayerIds(MistFighter fighter) {
        Map<Integer, Set<String>> ipPlayerMap = null;
        if (members.isEmpty()) {
            return null;
        }
        MistPlayer player;
        for (MistFighter member : members.values()) {
            if (member.getId() == fighter.getId()) {
                continue;
            }
            player = member.getOwnerPlayerInSameRoom();
            if (player == null) {
                continue;
            }
            if (ipPlayerMap == null) {
                ipPlayerMap = new HashMap<>();
            }
            Set<String> playerSet = ipPlayerMap.get(player.getServerIndex());
            if (playerSet == null) {
                playerSet = new HashSet<>();
                ipPlayerMap.put(player.getServerIndex(), playerSet);
            }
            playerSet.add(player.getIdx());
        }
        return ipPlayerMap;
    }
}
