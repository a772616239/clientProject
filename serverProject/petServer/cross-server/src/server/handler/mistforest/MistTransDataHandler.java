package server.handler.mistforest;

import com.google.protobuf.ByteString;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistNpc;
import model.mistforest.room.entity.MistGhostBusterRoom.MistGhostBusterRoom;
import model.mistforest.room.entity.MistRoom;
import model.mistforest.team.MistTeam;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_AcceptNpcTask;
import protocol.MistForest.CS_ApplyToMistTeam;
import protocol.MistForest.CS_ChangeAcceptTeamInvite;
import protocol.MistForest.CS_ChooseAlchemyReward;
import protocol.MistForest.CS_ClaimNpcTaskReward;
import protocol.MistForest.CS_InviteJoinMistTeam;
import protocol.MistForest.CS_KickOutFromTeam;
import protocol.MistForest.CS_MistForestPlayerInfo;
import protocol.MistForest.CS_ReplyApplyToMistTeam;
import protocol.MistForest.CS_ReplyInviteJoinTeam;
import protocol.MistForest.CS_StartAlchemy;
import protocol.MistForest.CS_UseMistVipSkill;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_AcceptNpcTask;
import protocol.MistForest.SC_ApplyToMistTeamRet;
import protocol.MistForest.SC_ClaimNpcTaskReward;
import protocol.MistForest.SC_KickOutFromTeam;
import protocol.MistForest.SC_MistForestPlayerInfo;
import protocol.MistForest.SC_ReplyApplyToMistTeamRet;
import protocol.MistForest.SC_ReplyInviteJoinTeamRet;
import protocol.MistForest.SC_UpdateAcceptTeamInvite;
import protocol.MistForest.SC_UseMistVipSkill;
import protocol.ServerTransfer.GS_CS_MistForestRoomInfo;
import protocol.TargetSystem.TargetTypeEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_MistForestRoomInfo_VALUE)
public class MistTransDataHandler extends AbstractHandler<GS_CS_MistForestRoomInfo> {
    @Override
    protected GS_CS_MistForestRoomInfo parse(byte[] bytes) throws Exception {
        return GS_CS_MistForestRoomInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_MistForestRoomInfo req, int codeNum) {
        String idx = req.getPlayerId();
        MistPlayer player = MistPlayerCache.getInstance().queryObject(idx);
        if (player == null || player.getMistRoom() == null) {
            // TODO 返回player is null
            return;
        }
        handlerServerMsg(player, req.getMsgId(), req.getMsgData());
    }

    public void handlerServerMsg(MistPlayer player, int msgId, ByteString msgData) {
        if (player == null) {
            return;
        }
        MistRoom room = player.getMistRoom();
        if (room == null) {
            return;
        }
        try {
            switch (msgId) {
                case MsgIdEnum.CS_MistForestJoinTeam_VALUE:
//                    SyncExecuteFunction.executeBitFunction(room, player, (room1, player1) -> room1.joinTeam(player1));
                    break;
                case MsgIdEnum.CS_MistForestExitTeam_VALUE:
                    SyncExecuteFunction.executeBitFunction(room, player, (room1, player1) -> room1.exitTeam(player1));
                    break;
                case MsgIdEnum.CS_ClientEventInvoke_VALUE:
                    SyncExecuteFunction.executeConsumer(room, room1 -> room1.handleCommandMsg(player.getFighterId(), msgData));
                    break;
                case MsgIdEnum.CS_MistForestPlayerInfo_VALUE:
                    MistPlayer tmpMistPlayer;
                    CS_MistForestPlayerInfo queryInfo = CS_MistForestPlayerInfo.parseFrom(msgData);
                    SC_MistForestPlayerInfo.Builder builder = SC_MistForestPlayerInfo.newBuilder();
                    for (String idx : queryInfo.getPlayerIdList()) {
                        tmpMistPlayer = MistPlayerCache.getInstance().queryObject(idx);
                        if (tmpMistPlayer != null) {
                            builder.addPlayerInfo(tmpMistPlayer.buildMistPlayerInfo());
                        }
                    }
                    player.sendMsgToServer(MsgIdEnum.SC_MistForestPlayerInfo_VALUE, builder);
                    break;
                case MsgIdEnum.CS_ChangeAcceptTeamInvite_VALUE:
                    CS_ChangeAcceptTeamInvite changeInfo = CS_ChangeAcceptTeamInvite.parseFrom(msgData);
                    SyncExecuteFunction.executeConsumer(player, player1 -> {
                        player1.setAcceptTeamInvite(changeInfo.getAcceptFlag());
                        SC_UpdateAcceptTeamInvite.Builder updateBuilder = SC_UpdateAcceptTeamInvite.newBuilder();
                        updateBuilder.setAcceptFlag(player1.isAcceptTeamInvite());
                        player1.sendMsgToServer(MsgIdEnum.CS_ChangeAcceptTeamInvite_VALUE, updateBuilder);
                    });
                    break;
                case MsgIdEnum.CS_CreateMistTeam_VALUE:
                    SyncExecuteFunction.executeBitFunction(room, player, (room1, player1) -> room1.createTeam(player1));
                    break;
                case MsgIdEnum.CS_InviteJoinMistTeam_VALUE: {
                    CS_InviteJoinMistTeam inviteInfo = CS_InviteJoinMistTeam.parseFrom(msgData);
                    MistPlayer targetMistPlayer = MistPlayerCache.getInstance().queryObject(inviteInfo.getTargetId());
                    MistRetCode retCode = room.inviteJoinTeam(player, targetMistPlayer);
                    if (retCode != MistRetCode.MRC_Success) {
                        SC_ReplyInviteJoinTeamRet.Builder replyBuilder = SC_ReplyInviteJoinTeamRet.newBuilder();
                        replyBuilder.setRetCode(retCode);
                        replyBuilder.setTargetId(inviteInfo.getTargetId());
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyInviteJoinTeamRet_VALUE, replyBuilder);
                    }
                    break;
                }
                case MsgIdEnum.CS_ReplyInviteJoinTeam_VALUE:
                    CS_ReplyInviteJoinTeam replyInviteJoinTeam = CS_ReplyInviteJoinTeam.parseFrom(msgData);
                    SC_ReplyInviteJoinTeamRet.Builder replyBuilder = SC_ReplyInviteJoinTeamRet.newBuilder();
                    MistTeam team = room.getTeamById(replyInviteJoinTeam.getTeamId());
                    if (team == null) {
                        replyBuilder.setTargetId(player.getIdx());
                        replyBuilder.setInviterId(replyInviteJoinTeam.getInviterId());
                        replyBuilder.setRetCode(MistRetCode.MRC_TeamNotFound); // 队伍已解散
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyInviteJoinTeamRet_VALUE, replyBuilder);
                        break;
                    }
                    if (replyInviteJoinTeam.getIsAgree()) {
                        if (team.isTeamFull()) {
                            replyBuilder.setTargetId(player.getIdx());
                            replyBuilder.setInviterId(replyInviteJoinTeam.getInviterId());
                            replyBuilder.setRetCode(MistRetCode.MRC_TeamFull); // 队伍已满
                            player.sendMsgToServer(MsgIdEnum.SC_ReplyInviteJoinTeamRet_VALUE, replyBuilder);
                            break;
                        }
                        MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                        if (fighter == null) {
                            replyBuilder.setTargetId(player.getIdx());
                            replyBuilder.setInviterId(replyInviteJoinTeam.getInviterId());
                            replyBuilder.setRetCode(MistRetCode.MRC_NotInMistForest); // 未进入迷雾森林
                            player.sendMsgToServer(MsgIdEnum.SC_ReplyInviteJoinTeamRet_VALUE, replyBuilder);
                            break;
                        }
                        MistRetCode ret = SyncExecuteFunction.executeFunction(
                                room, room1 -> room1.joinTeam(player, team.getTeamId()));
                        if (ret != MistRetCode.MRC_Success) {
                            replyBuilder.setTargetId(player.getIdx());
                            replyBuilder.setInviterId(replyInviteJoinTeam.getInviterId());
                            replyBuilder.setRetCode(ret);
                            player.sendMsgToServer(MsgIdEnum.SC_ReplyInviteJoinTeamRet_VALUE, replyBuilder);
                        }
                    } else {
                        MistPlayer inviterMistPlayer = MistPlayerCache.getInstance().queryObject(replyInviteJoinTeam.getInviterId());
                        if (inviterMistPlayer == null) {
                            break;
                        }
                        replyBuilder.setTargetId(player.getIdx());
                        replyBuilder.setInviterId(replyInviteJoinTeam.getInviterId());
                        replyBuilder.setRetCode(MistRetCode.MRC_TargetRefuseInvite); // 拒绝邀请
                        inviterMistPlayer.sendMsgToServer(MsgIdEnum.SC_ReplyInviteJoinTeamRet_VALUE, replyBuilder);
                    }
                    break;
                case MsgIdEnum.CS_ApplyToMistTeam_VALUE: {
                    CS_ApplyToMistTeam applyInfo = CS_ApplyToMistTeam.parseFrom(msgData);
                    MistTeam applyTeam = room.getTeamById(applyInfo.getTeamId());
                    SC_ReplyApplyToMistTeamRet.Builder replyApplyBuilder = SC_ReplyApplyToMistTeamRet.newBuilder();
                    if (applyTeam == null) {
                        replyApplyBuilder.setTeamId(applyInfo.getTeamId());
                        replyApplyBuilder.setRetCode(MistRetCode.MRC_TeamNotFound); // 队伍已经解散
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyBuilder);
                        break;
                    }
                    if (applyTeam.isTeamFull()) {
                        replyApplyBuilder.setTeamId(applyInfo.getTeamId());
                        replyApplyBuilder.setRetCode(MistRetCode.MRC_TeamFull); // 队伍已满
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyBuilder);
                        break;
                    }
                    MistRetCode result = MistRetCode.MRC_Success;
                    MistFighter leader = room.getObjManager().getMistObj(applyTeam.getLeaderId());
                    if (leader != null) {
                        MistPlayer leaderMistPlayer = leader.getOwnerPlayerInSameRoom();
                        if (leaderMistPlayer != null) {
                            SC_ApplyToMistTeamRet.Builder applyBuilder = SC_ApplyToMistTeamRet.newBuilder();
                            applyBuilder.setApplicantId(player.getIdx());
                            leaderMistPlayer.sendMsgToServer(MsgIdEnum.SC_ApplyToMistTeamRet_VALUE, applyBuilder);
                        } else {
                            result = MistRetCode.MRC_TeamLeaderLeft; // 队长已退队
                        }
                    } else {
                        result = SyncExecuteFunction.executeFunction(
                                room, room1 -> room1.joinTeam(player, applyTeam.getTeamId()));
                    }
                    if (result != MistRetCode.MRC_Success) {
                        replyApplyBuilder.setTeamId(applyInfo.getTeamId());
                        replyApplyBuilder.setRetCode(result);
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyBuilder);
                    }
                    break;
                }
                case MsgIdEnum.CS_ReplyApplyToMistTeam_VALUE: {
                    CS_ReplyApplyToMistTeam replyApplyInfo = CS_ReplyApplyToMistTeam.parseFrom(msgData);
                    SC_ReplyApplyToMistTeamRet.Builder replyApplyToTeamBuilder = SC_ReplyApplyToMistTeamRet.newBuilder();
                    replyApplyToTeamBuilder.setTeamId(replyApplyInfo.getTeamId());

                    MistTeam mistTeam = room.getTeamById(replyApplyInfo.getTeamId());
                    if (mistTeam == null) {
                        replyApplyToTeamBuilder.setRetCode(MistRetCode.MRC_TeamNotFound); // 队伍已解散
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyToTeamBuilder);
                        break;
                    }
                    MistPlayer applicant = MistPlayerCache.getInstance().queryObject(replyApplyInfo.getApplicantId());
                    if (applicant == null) {
                        replyApplyToTeamBuilder.setRetCode(MistRetCode.MRC_TargetNotFound); // 申请目标未找到
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyToTeamBuilder);
                        break;
                    }
                    MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                    if (fighter == null) {
                        replyApplyToTeamBuilder.setRetCode(MistRetCode.MRC_NotInMistForest); // 当前玩家未进入迷雾森林
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyToTeamBuilder);
                        break;
                    }
                    if (mistTeam.getLeaderId() != fighter.getId()) {
                        replyApplyToTeamBuilder.setRetCode(MistRetCode.MRC_NotTeamLeader); // 你不是队长
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyToTeamBuilder);
                        break;
                    }
                    if (mistTeam.isTeamFull()) {
                        replyApplyToTeamBuilder.setRetCode(MistRetCode.MRC_TeamFull); // 队伍已满
                        applicant.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyToTeamBuilder);
                        break;
                    }
                    if (!replyApplyInfo.getIsAgree()) {
                        replyApplyToTeamBuilder.setRetCode(MistRetCode.MRC_TeamLeaderRefuseApply); // 队长已拒绝申请
                        applicant.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyToTeamBuilder);
                        break;
                    }
                    MistRetCode ret = SyncExecuteFunction.executeFunction(
                            room, room1 -> room1.joinTeam(applicant, mistTeam.getTeamId()));
                    if (ret != MistRetCode.MRC_Success) {
                        replyApplyToTeamBuilder.setRetCode(ret);
                        player.sendMsgToServer(MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE, replyApplyToTeamBuilder);
                    }
                    break;
                }
                case MsgIdEnum.CS_KickOutFromTeam_VALUE:
                    CS_KickOutFromTeam kickOutInfo = CS_KickOutFromTeam.parseFrom(msgData);
                    MistFighter kickFighter = room.getObjManager().getMistObj(player.getFighterId());
                    if (kickFighter == null) {
                        break;
                    }
                    MistTeam kickFromTeam = room.getTeamById(kickFighter.getTeamId());
                    if (kickFromTeam == null) {
                        break;
                    }
                    if (kickFromTeam.getLeaderId() != kickFighter.getId()) {
                        break;
                    }
                    MistPlayer kickOutMistPlayer = MistPlayerCache.getInstance().queryObject(kickOutInfo.getTargetId());
                    if (kickOutMistPlayer == null) {
                        break;
                    }
                    boolean kickRet = SyncExecuteFunction.executeBitFunction(room, kickOutMistPlayer, (room1, player1) -> room1.exitTeam(player1));
                    if (kickRet) {
                        SC_KickOutFromTeam.Builder kickOutBuilder = SC_KickOutFromTeam.newBuilder();
                        kickOutBuilder.setRetCode(MistRetCode.MRC_OtherError); // 目标已被移除队伍
                        kickOutMistPlayer.sendMsgToServer(MsgIdEnum.SC_KickOutFromTeam_VALUE, kickOutBuilder);

                        kickOutBuilder.setRetCode(MistRetCode.MRC_OtherError); // 你被队长踢出队伍
                        player.sendMsgToServer(MsgIdEnum.SC_KickOutFromTeam_VALUE, kickOutBuilder);
                    }
                    break;
                case MsgIdEnum.CS_GhostBusterRankData_VALUE: {
                    if (room.getMistRule() != EnumMistRuleKind.EMRK_GhostBuster_VALUE) {
                        break;
                    }
                    ((MistGhostBusterRoom) room).updateRankData();
                    break;
                }
                case MsgIdEnum.CS_GhostBusterGhostCount_VALUE: {
                    if (room.getMistRule() != EnumMistRuleKind.EMRK_GhostBuster_VALUE) {
                        break;
                    }
                    player.sendMsgToServer(MsgIdEnum.SC_GhostBusterGhostCount_VALUE, room.getObjManager().getGhostTypeCount());
                    break;
                }
                case MsgIdEnum.CS_AcceptNpcTask_VALUE: {
                    if (room.getMistRule() != EnumMistRuleKind.EMRK_Common_VALUE) {
                        break;
                    }
                    CS_AcceptNpcTask acceptNpcTaskReq = CS_AcceptNpcTask.parseFrom(msgData);
                    MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                    MistRetCode retCode = SyncExecuteFunction.executeFunction(room, mistRoom-> {
                       MistNpc npc = mistRoom.getObjManager().getMistObj(acceptNpcTaskReq.getNpcId());
                       return npc.acceptNpcTask(fighter);
                    });
                    player.sendMsgToServer(MsgIdEnum.SC_AcceptNpcTask_VALUE, SC_AcceptNpcTask.newBuilder().setRetCode(retCode));
                    break;
                }
                case MsgIdEnum.CS_ClaimNpcTaskReward_VALUE: {
                    if (room.getMistRule() != EnumMistRuleKind.EMRK_Common_VALUE) {
                        break;
                    }
                    CS_ClaimNpcTaskReward claimNpcTaskRewardReq = CS_ClaimNpcTaskReward.parseFrom(msgData);
                    MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                    MistRetCode retCode = SyncExecuteFunction.executeFunction(room, mistRoom->
                            fighter.getNpcTask().claimNpcTaskReward(claimNpcTaskRewardReq.getTaskId()));
                    player.sendMsgToServer(MsgIdEnum.SC_ClaimNpcTaskReward_VALUE, SC_ClaimNpcTaskReward.newBuilder().setRetCode(retCode));
                    break;
                }
                case MsgIdEnum.CS_StartAlchemy_VALUE: {
                    if (room.getMistRule() != EnumMistRuleKind.EMRK_Common_VALUE) {
                        break;
                    }
                    MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                    if (fighter == null) {
                        break;
                    }
                    CS_StartAlchemy req = CS_StartAlchemy.parseFrom(msgData);
                    SyncExecuteFunction.executeConsumer(player, entity -> entity.startAlchemyReward(req.getExchangeRewardId()));
                    break;
                }
                case MsgIdEnum.CS_ChooseAlchemyReward_VALUE: {
                    if (room.getMistRule() != EnumMistRuleKind.EMRK_Common_VALUE) {
                        break;
                    }
                    MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                    if (fighter == null) {
                        break;
                    }
                    CS_ChooseAlchemyReward req = CS_ChooseAlchemyReward.parseFrom(msgData);
                    boolean result = SyncExecuteFunction.executePredicate(player, entity -> entity.chooseAlchemyReward(req.getAlchemyRewardId(), req.getChooseRewardId()));
                    if (result) {
                        fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_JoinAlchemyReward, 0, 1);
                    }
                    break;
                }
                case MsgIdEnum.CS_UseMistVipSkill_VALUE: {
                    if (room.getMistRule() != EnumMistRuleKind.EMRK_Common_VALUE) {
                        break;
                    }
                    MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                    if (fighter == null) {
                        break;
                    }
                    CS_UseMistVipSkill req = CS_UseMistVipSkill.parseFrom(msgData);
                    MistRetCode retCode = fighter.getSkillMachine().useVipSkill(req.getSkillId(), req.getSkillParam());
                    if (retCode != MistRetCode.MRC_Success) {
                        SC_UseMistVipSkill.Builder builder1 = SC_UseMistVipSkill.newBuilder();
                        builder1.setRetCode(retCode);
                        player.sendMsgToServer(MsgIdEnum.SC_UseMistVipSkill_VALUE, builder1);
                    }
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
