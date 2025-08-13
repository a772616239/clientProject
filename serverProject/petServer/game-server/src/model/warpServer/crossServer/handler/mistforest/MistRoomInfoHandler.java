package model.warpServer.crossServer.handler.mistforest;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3.Builder;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.SC_RetCode;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_AcceptNpcTask;
import protocol.MistForest.SC_ApplyToMistTeamRet;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.SC_BusinessManResult;
import protocol.MistForest.SC_BusinessManReward;
import protocol.MistForest.SC_ChooseAlchemyReward;
import protocol.MistForest.SC_ClaimNpcTaskReward;
import protocol.MistForest.SC_ExchangeMistForest;
import protocol.MistForest.SC_GhostBusterGhostCount;
import protocol.MistForest.SC_GhostBusterRankData;
import protocol.MistForest.SC_GhostBusterRoomStateTime;
import protocol.MistForest.SC_InviteJoinMistTeamRet;
import protocol.MistForest.SC_KickOutFromTeam;
import protocol.MistForest.SC_LavaBadgeCombine;
import protocol.MistForest.SC_MistEnterPlayerInfo;
import protocol.MistForest.SC_MistExitPlayerInfo;
import protocol.MistForest.SC_MistForestPlayerInfo;
import protocol.MistForest.SC_MistForestTeamInfo;
import protocol.MistForest.SC_ReplyApplyToMistTeamRet;
import protocol.MistForest.SC_ReplyInviteJoinTeamRet;
import protocol.MistForest.SC_StartAlchemy;
import protocol.MistForest.SC_UpdateAcceptTeamInvite;
import protocol.MistForest.SC_UpdateBoxRemainCount;
import protocol.MistForest.SC_UpdateMistActivityBossState;
import protocol.MistForest.SC_UpdateMistBossDmgRankData;
import protocol.MistForest.SC_UpdateMistShowData;
import protocol.MistForest.SC_UpdateMistTeamList;
import protocol.MistForest.SC_UpdateNpcTask;
import protocol.MistForest.SC_UpdateScheduleInfo;
import protocol.MistForest.SC_UseMistVipSkill;
import protocol.ServerTransfer.CS_GS_MistForestRoomInfo;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_MistForestRoomInfo_VALUE)
public class MistRoomInfoHandler extends AbstractHandler<CS_GS_MistForestRoomInfo> {
    @Override
    protected CS_GS_MistForestRoomInfo parse(byte[] bytes) throws Exception {
        return CS_GS_MistForestRoomInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_MistForestRoomInfo req, int i) {
        try {
            int msgId = req.getMsgId();
            LogUtil.debug("recv MistForest transfer msg id = " + msgId);
            ByteString msgData = req.getMsgData();
            List<String> playerIds = req.getPlayerIdList();
            Builder builder = null;
            switch (msgId) {
                case MsgIdEnum.SC_BattleCmd_VALUE:
                    builder = SC_BattleCmd.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_MistForestTeamInfo_VALUE:
                    builder = SC_MistForestTeamInfo.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_MistEnterPlayerInfo_VALUE:
                    builder = SC_MistEnterPlayerInfo.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_MistExitPlayerInfo_VALUE:
                    builder = SC_MistExitPlayerInfo.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_MistForestPlayerInfo_VALUE:
                    builder = SC_MistForestPlayerInfo.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ExchangeMistForest_VALUE:
                    builder = SC_ExchangeMistForest.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateMistTeamList_VALUE:
                    builder = SC_UpdateMistTeamList.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_InviteJoinMistTeamRet_VALUE:
                    builder = SC_InviteJoinMistTeamRet.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ReplyInviteJoinTeamRet_VALUE:
                    builder = SC_ReplyInviteJoinTeamRet.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ApplyToMistTeamRet_VALUE:
                    builder = SC_ApplyToMistTeamRet.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ReplyApplyToMistTeamRet_VALUE:
                    builder = SC_ReplyApplyToMistTeamRet.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_KickOutFromTeam_VALUE:
                    builder = SC_KickOutFromTeam.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateAcceptTeamInvite_VALUE:
                    builder = SC_UpdateAcceptTeamInvite.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateBoxRemainCount_VALUE:
                    builder = SC_UpdateBoxRemainCount.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_GhostBusterRoomStateTime_VALUE:
                    builder = SC_GhostBusterRoomStateTime.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_GhostBusterRankData_VALUE:
                    builder = SC_GhostBusterRankData.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_GhostBusterGhostCount_VALUE:
                    builder = SC_GhostBusterGhostCount.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_RetCode_VALUE:
                    builder = SC_RetCode.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateMistActivityBossState_VALUE:
                    builder = SC_UpdateMistActivityBossState.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateMistBossDmgRankData_VALUE:
                    builder = SC_UpdateMistBossDmgRankData.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_AcceptNpcTask_VALUE:
                    builder = SC_AcceptNpcTask.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ClaimNpcTaskReward_VALUE:
                    builder = SC_ClaimNpcTaskReward.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateNpcTask_VALUE:
                    builder = SC_UpdateNpcTask.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_BusinessManResult_VALUE:
                    builder = SC_BusinessManResult.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_BusinessManReward_VALUE:
                    builder = SC_BusinessManReward.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_StartAlchemy_VALUE:
                    builder = SC_StartAlchemy.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ChooseAlchemyReward_VALUE:
                    builder = SC_ChooseAlchemyReward.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateScheduleInfo_VALUE:
                    builder = SC_UpdateScheduleInfo.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_LavaBadgeCombine_VALUE:
                    builder = SC_LavaBadgeCombine.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UseMistVipSkill_VALUE:
                    builder = SC_UseMistVipSkill.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateMistShowData_VALUE:
                    builder = SC_UpdateMistShowData.parseFrom(msgData).toBuilder();
                    break;
                default:
                    break;
            }
            if (builder == null) {
                return;
            }
            playerEntity player;
            for (String idx : playerIds) {
                player = playerCache.getByIdx(idx);
                if (player == null) {
                    continue;
                }
                GlobalData.getInstance().sendMsg(idx, msgId, builder);
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
