package model.warpServer.crossServer.handler.thewar;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3.Builder;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TheWarTransInfo;
import protocol.TheWar.SC_CancelClearOwnedGrid;
import protocol.TheWar.SC_ClaimAfkReward;
import protocol.TheWar.SC_ClaimTheWarMissionReward;
import protocol.TheWar.SC_ClearOwnedGrid;
import protocol.TheWar.SC_ClearStationTroops;
import protocol.TheWar.SC_ComposeNewItem;
import protocol.TheWar.SC_KickOutFromTheWar;
import protocol.TheWar.SC_PromoteJobTile;
import protocol.TheWar.SC_PromoteTechnology;
import protocol.TheWar.SC_QueryGridBattleData;
import protocol.TheWar.SC_QueryWarGridRecord;
import protocol.TheWar.SC_QueryWarTeam;
import protocol.TheWar.SC_StationTroops;
import protocol.TheWar.SC_SubmitDpResource;
import protocol.TheWar.SC_TheWarBattleReward;
import protocol.TheWar.SC_TheWarBroadCast;
import protocol.TheWar.SC_UpdateAfkRewardData;
import protocol.TheWar.SC_UpdateNewMemberJoin;
import protocol.TheWar.SC_UpdatePlayerJobtile;
import protocol.TheWar.SC_UpdatePursuitRewards;
import protocol.TheWar.SC_UpdateStationTroops;
import protocol.TheWar.SC_UpdateTheWarMission;
import protocol.TheWar.SC_UpdateWarFightTeam;
import protocol.TheWar.SC_UpdateWarGridRecord;
import protocol.TheWar.SC_UpdateWarTeamPet;
import protocol.TheWar.SC_UpdateWarTeamSkill;
import protocol.TheWarDefine.SC_EquipOffItem;
import protocol.TheWarDefine.SC_EquipOnItem;
import protocol.TheWarDefine.SC_JobTileTaskData;
import protocol.TheWarDefine.SC_OperateCollectionPos;
import protocol.TheWarDefine.SC_QueryWarGridList;
import protocol.TheWarDefine.SC_QueryWarPetData;
import protocol.TheWarDefine.SC_UpdateCampInfo;
import protocol.TheWarDefine.SC_UpdateNewItem;
import protocol.TheWarDefine.SC_UpdatePetProp;
import protocol.TheWarDefine.SC_UpdatePlayerStamia;
import protocol.TheWarDefine.SC_UpdateWarCurrency;
import protocol.TheWarDefine.SC_UpdateWarGridProp;
import protocol.TheWarDefine.SC_UpdateWarPetData;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_TheWarTransInfo_VALUE)
public class TheWarTransInfoHandler extends AbstractHandler<CS_GS_TheWarTransInfo> {
    @Override
    protected CS_GS_TheWarTransInfo parse(byte[] bytes) throws Exception {
        return CS_GS_TheWarTransInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_TheWarTransInfo ret, int i) {
        try {
            int msgId = ret.getMsgId();
            LogUtil.debug("recv TheWar transfer msg id = " + msgId);
            ByteString msgData = ret.getMsgData();
            List<String> playerIds = ret.getPlayerIdsList();
            Builder builder = null;
            switch (msgId) {
                case MsgIdEnum.SC_EquipOnItem_VALUE:
                    builder = SC_EquipOnItem.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_EquipOffItem_VALUE:
                    builder = SC_EquipOffItem.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ComposeNewItem_VALUE:
                    builder = SC_ComposeNewItem.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_PromoteTechnology_VALUE:
                    builder = SC_PromoteTechnology.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_PromoteJobTile_VALUE:
                    builder = SC_PromoteJobTile.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_JobTileTaskData_VALUE:
                    builder = SC_JobTileTaskData.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateWarTeamPet_VALUE:
                    builder = SC_UpdateWarTeamPet.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateWarTeamSkill_VALUE:
                    builder = SC_UpdateWarTeamSkill.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ClearOwnedGrid_VALUE:
                    builder = SC_ClearOwnedGrid.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_QueryWarGridList_VALUE:
                    builder = SC_QueryWarGridList.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateWarGridProp_VALUE:
                    builder = SC_UpdateWarGridProp.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_QueryWarPetData_VALUE:
                    builder = SC_QueryWarPetData.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_QueryWarTeam_VALUE:
                    builder = SC_QueryWarTeam.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateAfkRewardData_VALUE:
                    builder = SC_UpdateAfkRewardData.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ClaimAfkReward_VALUE:
                    builder = SC_ClaimAfkReward.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_OperateCollectionPos_VALUE:
                    builder = SC_OperateCollectionPos.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_SubmitDpResource_VALUE:
                    builder = SC_SubmitDpResource.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateWarCurrency_VALUE:
                    builder = SC_UpdateWarCurrency.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateNewItem_VALUE:
                    builder = SC_UpdateNewItem.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateWarPetData_VALUE:
                    builder = SC_UpdateWarPetData.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_KickOutFromTheWar_VALUE:
                    builder = SC_KickOutFromTheWar.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_CancelClearOwnedGrid_VALUE:
                    builder = SC_CancelClearOwnedGrid.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateNewMemberJoin_VALUE:
                    builder = SC_UpdateNewMemberJoin.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdatePlayerStamia_VALUE:
                    builder = SC_UpdatePlayerStamia.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateCampInfo_VALUE:
                    builder = SC_UpdateCampInfo.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdatePursuitRewards_VALUE:
                    builder = SC_UpdatePursuitRewards.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_TheWarBattleReward_VALUE:
                    builder = SC_TheWarBattleReward.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_QueryGridBattleData_VALUE:
                    builder = SC_QueryGridBattleData.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_StationTroops_VALUE:
                    builder = SC_StationTroops.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateWarFightTeam_VALUE:
                    builder = SC_UpdateWarFightTeam.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateStationTroops_VALUE:
                    builder = SC_UpdateStationTroops.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdatePetProp_VALUE:
                    builder = SC_UpdatePetProp.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdatePlayerJobtile_VALUE:
                    builder = SC_UpdatePlayerJobtile.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_TheWarBroadCast_VALUE:
                    builder = SC_TheWarBroadCast.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ClearStationTroops_VALUE:
                    builder = SC_ClearStationTroops.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_QueryWarGridRecord_VALUE:
                    builder = SC_QueryWarGridRecord.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateWarGridRecord_VALUE:
                    builder = SC_UpdateWarGridRecord.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_ClaimTheWarMissionReward_VALUE:
                    builder = SC_ClaimTheWarMissionReward.parseFrom(msgData).toBuilder();
                    break;
                case MsgIdEnum.SC_UpdateTheWarMission_VALUE:
                    builder = SC_UpdateTheWarMission.parseFrom(msgData).toBuilder();
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
                if (player != null) {
                    GlobalData.getInstance().sendMsg(idx, msgId, builder);
                }
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}
