package server.handler.thewar;

import cfg.TheWarConstConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalTick;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.thewar.WarConst;
import model.thewar.WarConst.RoomState;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warmap.grid.FootHoldGrid;
import model.thewar.warmap.grid.WarMapGrid;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_TheWarTransInfo;
import protocol.TheWar.CS_CancelClearOwnedGrid;
import protocol.TheWar.CS_ClearOwnedGrid;
import protocol.TheWar.CS_ClearStationTroops;
import protocol.TheWar.CS_ComposeNewItem;
import protocol.TheWar.CS_PromoteTechnology;
import protocol.TheWar.CS_QueryGridBattleData;
import protocol.TheWar.CS_StationTroops;
import protocol.TheWar.CS_SubmitDpResource;
import protocol.TheWar.CS_UpdateWarTeamPet;
import protocol.TheWar.CS_UpdateWarTeamSkill;
import protocol.TheWar.SC_CancelClearOwnedGrid;
import protocol.TheWar.SC_ClaimAfkReward;
import protocol.TheWar.SC_ClaimTheWarMissionReward;
import protocol.TheWar.SC_ClearOwnedGrid;
import protocol.TheWar.SC_ClearStationTroops;
import protocol.TheWar.SC_ComposeNewItem;
import protocol.TheWar.SC_PromoteJobTile;
import protocol.TheWar.SC_PromoteTechnology;
import protocol.TheWar.SC_QueryGridBattleData;
import protocol.TheWar.SC_QueryWarGridRecord;
import protocol.TheWar.SC_QueryWarTeam;
import protocol.TheWar.SC_StationTroops;
import protocol.TheWar.SC_SubmitDpResource;
import protocol.TheWar.SC_UpdatePlayerJobtile;
import protocol.TheWar.SC_UpdateStationTroops;
import protocol.TheWar.SC_UpdateWarTeamPet;
import protocol.TheWar.SC_UpdateWarTeamSkill;
import protocol.TheWar.StationTroopsInfo;
import protocol.TheWar.WarReward;
import protocol.TheWarDefine.CS_EquipOffItem;
import protocol.TheWarDefine.CS_EquipOnItem;
import protocol.TheWarDefine.CS_OperateCollectionPos;
import protocol.TheWarDefine.CS_QueryWarGridList;
import protocol.TheWarDefine.CS_QueryWarPetData;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.SC_EquipOffItem;
import protocol.TheWarDefine.SC_EquipOnItem;
import protocol.TheWarDefine.SC_OperateCollectionPos;
import protocol.TheWarDefine.SC_QueryWarGridList;
import protocol.TheWarDefine.SC_QueryWarPetData;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarResourceType;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TheWarDefine.WarPetData;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_TheWarTransInfo_VALUE)
public class TheWarTransInfoHandler extends AbstractHandler<GS_CS_TheWarTransInfo> {
    @Override
    protected GS_CS_TheWarTransInfo parse(byte[] bytes) throws Exception {
        return GS_CS_TheWarTransInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_TheWarTransInfo req, int i) {
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (warPlayer == null) {
            return;
        }
        WarRoom room = WarRoomCache.getInstance().queryObject(warPlayer.getRoomIdx());
        if (room == null || room.needClear()) {
            return;
        }
        try {
            switch (req.getMsgId()) {
                case MsgIdEnum.CS_EquipOnItem_VALUE: {
                    CS_EquipOnItem equipOnReq = CS_EquipOnItem.parseFrom(req.getMsgData());
                    TheWarRetCode retCode = SyncExecuteFunction.executeFunction(warPlayer, entity -> entity.equipOnItem(equipOnReq));
                    SC_EquipOnItem.Builder equipOnRet = SC_EquipOnItem.newBuilder();
                    equipOnRet.setRetCode(retCode);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_EquipOnItem_VALUE, equipOnRet);
                    break;
                }
                case MsgIdEnum.CS_EquipOffItem_VALUE: {
                    CS_EquipOffItem equipOffReq = CS_EquipOffItem.parseFrom(req.getMsgData());
                    TheWarRetCode retCode = SyncExecuteFunction.executeFunction(warPlayer, entity -> entity.equipOffItem(equipOffReq));
                    SC_EquipOffItem.Builder equipOffRet = SC_EquipOffItem.newBuilder();
                    equipOffRet.setRetCode(retCode);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_EquipOffItem_VALUE, equipOffRet);
                    break;
                }
                case MsgIdEnum.CS_ComposeNewItem_VALUE: {
                    CS_ComposeNewItem composeItemReq = CS_ComposeNewItem.parseFrom(req.getMsgData());
                    // 查找需要移除的道具idx列表
                    Set<String> removeIdxList = new HashSet<>();
                    TheWarRetCode retCode = SyncExecuteFunction.executeFunction(warPlayer, entity -> entity.composeItem(composeItemReq, removeIdxList));
                    SC_ComposeNewItem.Builder composeItemRet = SC_ComposeNewItem.newBuilder();
                    composeItemRet.setRetCode(retCode);
                    composeItemRet.addAllRemovedItemList(removeIdxList);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ComposeNewItem_VALUE, composeItemRet);
                    break;
                }
                case MsgIdEnum.CS_PromoteTechnology_VALUE: {
                    CS_PromoteTechnology promoteTechReq = CS_PromoteTechnology.parseFrom(req.getMsgData());
                    TheWarRetCode retCode = SyncExecuteFunction.executeFunction(warPlayer, entity ->
                            entity.levelUpTechnology(promoteTechReq.getProfessionType()));
                    SC_PromoteTechnology.Builder promoteTechRet = SC_PromoteTechnology.newBuilder();
                    promoteTechRet.setRetCode(retCode);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_PromoteTechnology_VALUE, promoteTechRet);
                    break;
                }
                case MsgIdEnum.CS_PromoteJobTile_VALUE: {
                    TheWarRetCode retCode = SyncExecuteFunction.executeFunction(warPlayer, entity -> entity.promoteJobTile(false));
                    SC_PromoteJobTile.Builder promoteJobTileRet = SC_PromoteJobTile.newBuilder();
                    promoteJobTileRet.setRetCode(retCode);
                    if (retCode == TheWarRetCode.TWRC_Success) {
                        promoteJobTileRet.setJobTileTask(warPlayer.builderJobTileTaskData());

                        SC_UpdatePlayerJobtile.Builder newJobTileData = SC_UpdatePlayerJobtile.newBuilder();
                        newJobTileData.setPlayerId(warPlayer.getIdx());
                        newJobTileData.setNewJobtileLevel(warPlayer.getJobTileLevel());
                        room.broadcastMsg(MsgIdEnum.SC_UpdatePlayerJobtile_VALUE, newJobTileData, true);
                    }
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_PromoteJobTile_VALUE, promoteJobTileRet);
                    break;
                }
                case MsgIdEnum.CS_ClearOwnedGrid_VALUE: {
                    SC_ClearOwnedGrid.Builder clearGridRet = SC_ClearOwnedGrid.newBuilder();
                    if (room.getRoomState() != RoomState.FightingState || room.isPreSettleFlag()) {
                        clearGridRet.setRetCode(TheWarRetCode.TWRC_RoomEnded); // 战戈已结束
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClearOwnedGrid_VALUE, clearGridRet);
                        break;
                    }
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        clearGridRet.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 地图未找到
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClearOwnedGrid_VALUE, clearGridRet);
                        break;
                    }
                    CS_ClearOwnedGrid clearGridReq = CS_ClearOwnedGrid.parseFrom(req.getMsgData());
                    WarMapGrid grid = mapData.getMapGridByPos(clearGridReq.getPos());
                    if (!(grid instanceof FootHoldGrid)) {
                        clearGridRet.setRetCode(TheWarRetCode.TWRC_InvalidPos); // 该位置不是可占领格子
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClearOwnedGrid_VALUE, clearGridRet);
                        break;
                    }

                    FootHoldGrid ftGrid = (FootHoldGrid) grid;
                    TheWarRetCode retCode = ftGrid.preClearOwnedGrid(warPlayer);
                    if (retCode == TheWarRetCode.TWRC_Success) {
                        long longPos = WarConst.protoPosToLongPos(ftGrid.getPos());
                        long clearTime = GlobalTick.getInstance().getCurrentTime() + TheWarConstConfig.getById(GameConst.ConfigId).getDelaycleargridtime() * TimeUtil.MS_IN_A_MIN;
                        SyncExecuteFunction.executeFunction(warPlayer, entity -> entity.getPlayerData().putClearingGridPos(longPos, clearTime));
                        SyncExecuteFunction.executeConsumer(ftGrid, gridEntity -> {
                            gridEntity.setPropValue(TheWarCellPropertyEnum.TWCP_RealClearTimeStamp_VALUE, clearTime);
                            gridEntity.broadcastPropData();
                        });

                    }
                    clearGridRet.setRetCode(retCode);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClearOwnedGrid_VALUE, clearGridRet);
                    break;
                }
                case MsgIdEnum.CS_CancelClearOwnedGrid_VALUE: {
                    SC_CancelClearOwnedGrid.Builder cancelClearRet = SC_CancelClearOwnedGrid.newBuilder();
                    if (room.getRoomState() != RoomState.FightingState || room.isPreSettleFlag()) {
                        cancelClearRet.setRetCode(TheWarRetCode.TWRC_RoomEnded); // 战戈已结束
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE, cancelClearRet);
                        break;
                    }
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        cancelClearRet.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 地图未找到
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE, cancelClearRet);
                        break;
                    }
                    CS_CancelClearOwnedGrid clearGridReq = CS_CancelClearOwnedGrid.parseFrom(req.getMsgData());
                    WarMapGrid grid = mapData.getMapGridByPos(clearGridReq.getPos());
                    if (!(grid instanceof FootHoldGrid)) {
                        cancelClearRet.setRetCode(TheWarRetCode.TWRC_InvalidPos); // 该位置不是可占领格子
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE, cancelClearRet);
                        break;
                    }
                    FootHoldGrid ftGrid = (FootHoldGrid) grid;
                    long playerId = GameUtil.stringToLong(warPlayer.getIdx(), 0);
                    if (ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) != playerId) {
                        cancelClearRet.setRetCode(TheWarRetCode.TWRC_NotOccupiedGrid); // 未占领该格子，无法取消
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE, cancelClearRet);
                        break;
                    }
                    long curTime = GlobalTick.getInstance().getCurrentTime();
                    if (ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_RealClearTimeStamp_VALUE) <= curTime) {
                        cancelClearRet.setRetCode(TheWarRetCode.TWRC_ClearTimesUp); // 清除时间已过，无法取消
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE, cancelClearRet);
                        break;
                    }
                    long longPos = WarConst.protoPosToLongPos(ftGrid.getPos());
                    SyncExecuteFunction.executeConsumer(warPlayer, entity -> {
                        Long clearTime = entity.getPlayerData().getClearingGridPosMap().get(longPos);
                        if (clearTime != null && clearTime > GlobalTick.getInstance().getCurrentTime()) {
                            entity.getPlayerData().removeClearingGridPos(longPos);
                        }
                    });

                    SyncExecuteFunction.executeConsumer(ftGrid, gridEntity -> {
                        gridEntity.setPropDefaultValue(TheWarCellPropertyEnum.TWCP_RealClearTimeStamp_VALUE);
                        gridEntity.broadcastPropData();
                    });
                    cancelClearRet.setRetCode(TheWarRetCode.TWRC_Success);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_CancelClearOwnedGrid_VALUE, cancelClearRet);
                    break;
                }
                case MsgIdEnum.CS_StationTroops_VALUE: {
                    SC_StationTroops.Builder troopsBuilderRet = SC_StationTroops.newBuilder();
                    if (room.getRoomState() != RoomState.FightingState) {
                        troopsBuilderRet.setRetCode(TheWarRetCode.TWRC_RoomEnded);
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_StationTroops_VALUE, troopsBuilderRet);
                        break;
                    }
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        troopsBuilderRet.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 地图未找到
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_StationTroops_VALUE, troopsBuilderRet);
                        break;
                    }
                    CS_StationTroops stationTroopsReq = CS_StationTroops.parseFrom(req.getMsgData());
                    TheWarRetCode retCode;
                    SC_UpdateStationTroops.Builder updateStationTroops = SC_UpdateStationTroops.newBuilder();
                    for (StationTroopsInfo troopsInfo : stationTroopsReq.getTroopsInfoList()) {
//                        retCode = mapData.playerStationTroopsGrid(warPlayer, troopsInfo);
                        retCode = mapData.playerStationTroopsGrid(warPlayer, troopsInfo);
                        if (retCode == TheWarRetCode.TWRC_Success) {
                            updateStationTroops.addTroopsInfo(troopsInfo);
                        }
                    }
                    troopsBuilderRet.setRetCode(TheWarRetCode.TWRC_Success);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_StationTroops_VALUE, troopsBuilderRet);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdateStationTroops_VALUE, updateStationTroops);
                    break;
                }
                case MsgIdEnum.CS_ClearStationTroops_VALUE: {
                    SC_ClearStationTroops.Builder clearTroopsBuilderRet = SC_ClearStationTroops.newBuilder();
                    if (room.getRoomState() != RoomState.FightingState) {
                        clearTroopsBuilderRet.setRetCode(TheWarRetCode.TWRC_RoomEnded);
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClearStationTroops_VALUE, clearTroopsBuilderRet);
                        break;
                    }
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        clearTroopsBuilderRet.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 地图未找到
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClearStationTroops_VALUE, clearTroopsBuilderRet);
                        break;
                    }
                    CS_ClearStationTroops clearStationTroopsReq = CS_ClearStationTroops.parseFrom(req.getMsgData());
                    TheWarRetCode retCode = mapData.removeAllStationTroopsPets(warPlayer, clearStationTroopsReq.getGridPos());
                    clearTroopsBuilderRet.setRetCode(retCode);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClearStationTroops_VALUE, clearTroopsBuilderRet);
                    break;
                }
                case MsgIdEnum.CS_QueryGridBattleData_VALUE: {
                    CS_QueryGridBattleData queryGridBatPetDataReq = CS_QueryGridBattleData.parseFrom(req.getMsgData());
                    SC_QueryGridBattleData.Builder queryGridBatPetDataRet;
                    if (room.getRoomState() != RoomState.FightingState || room.isPreSettleFlag()) {
                        queryGridBatPetDataRet = SC_QueryGridBattleData.newBuilder();
                        queryGridBatPetDataRet.setRetCode(TheWarRetCode.TWRC_RoomEnded); // 房间已结束
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryGridBattleData_VALUE, queryGridBatPetDataRet);
                        break;
                    }
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        queryGridBatPetDataRet = SC_QueryGridBattleData.newBuilder();
                        queryGridBatPetDataRet.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 未找到地图
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryGridBattleData_VALUE, queryGridBatPetDataRet);
                        break;
                    }

                    WarMapGrid grid = mapData.getMapGridByPos(queryGridBatPetDataReq.getGridPos());
                    if (!(grid instanceof FootHoldGrid) || grid.isBlock()) {
                        queryGridBatPetDataRet = SC_QueryGridBattleData.newBuilder();
                        queryGridBatPetDataRet.setRetCode(TheWarRetCode.TWRC_InvalidPos); // 非法位置
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryGridBattleData_VALUE, queryGridBatPetDataRet);
                        break;
                    }
                    FootHoldGrid fhGrid = (FootHoldGrid) grid;
                    queryGridBatPetDataRet = fhGrid.getGridBattlePetData();
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryGridBattleData_VALUE, queryGridBatPetDataRet);
                    break;
                }
                case MsgIdEnum.CS_ClaimAfkReward_VALUE: {
                    SC_ClaimAfkReward.Builder claimAfkRewardRet = SC_ClaimAfkReward.newBuilder();
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        claimAfkRewardRet.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 地图未找到
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClaimAfkReward_VALUE, claimAfkRewardRet);
                        break;
                    }
                    WarMapGrid grid;
                    FootHoldGrid footHoldGrid;
                    List<WarReward> rewardList = new ArrayList<>();
                    Map<Integer, Long> itemSettleMap = new HashMap<>();
                    long curTime = GlobalTick.getInstance().getCurrentTime();
                    WarReward.Builder goldBuilder = WarReward.newBuilder().setRewardType(TheWarResourceType.TWRT_WarGold);
                    WarReward.Builder dpBuilder = WarReward.newBuilder().setRewardType(TheWarResourceType.TWRT_WarDoorPoint);
                    WarReward.Builder holyWaterBuilder = WarReward.newBuilder().setRewardType(TheWarResourceType.TWRT_WarHolyWater);
                    for (Position ownedGridPos : warPlayer.getPlayerData().getOwnedGridPosList()) {
                        grid = mapData.getMapGridByPos(ownedGridPos);
                        if (grid instanceof FootHoldGrid) {
                            footHoldGrid = (FootHoldGrid) grid;
                            SyncExecuteFunction.executeConsumer(footHoldGrid, fhGrid -> {
                                int itemCfgId = (int) fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_DropItemCfgId_VALUE);
                                long itemRewardTime = fhGrid.playerClaimAfkReward(goldBuilder, dpBuilder, holyWaterBuilder, rewardList, curTime);
                                if (itemCfgId > 0 && itemRewardTime > 0) {
                                    itemSettleMap.merge(itemCfgId, itemRewardTime, (oldVal, newVal) -> oldVal + newVal);
                                }
                            });
                        }
                    }
                    if (goldBuilder.getRewardCount() > 0) {
                        rewardList.add(goldBuilder.build());
                    }
                    if (dpBuilder.getRewardCount() > 0) {
                        rewardList.add(dpBuilder.build());
                    }
                    if (holyWaterBuilder.getRewardCount() > 0) {
                        rewardList.add(holyWaterBuilder.build());
                    }
                    Event event = Event.valueOf(EventType.ET_TheWar_SettleAfkReward, room, warPlayer);
                    event.pushParam(rewardList, itemSettleMap);
                    EventManager.getInstance().dispatchEvent(event);

                    claimAfkRewardRet.setRetCode(TheWarRetCode.TWRC_Success);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClaimAfkReward_VALUE, claimAfkRewardRet);
                    break;
                }
                case MsgIdEnum.CS_OperateCollectionPos_VALUE: {
                    SC_OperateCollectionPos.Builder opCollectPosRet = SC_OperateCollectionPos.newBuilder();
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        opCollectPosRet.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 地图未找到
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_OperateCollectionPos_VALUE, opCollectPosRet);
                        break;
                    }
                    CS_OperateCollectionPos opCollectPosReq = CS_OperateCollectionPos.parseFrom(req.getMsgData());
                    if (mapData.isPosBlock(opCollectPosReq.getPos())) {
                        opCollectPosRet.setRetCode(TheWarRetCode.TWRC_GridIsBlock); // 该位置不可达，无法占领
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_OperateCollectionPos_VALUE, opCollectPosRet);
                        break;
                    }
//                    if (mapData.getAllBossGrids().contains(opCollectPosReq.getPos())) {
//                        if (opCollectPosReq.getBAdd()) {
//                            opCollectPosRet.setRetCode(TheWarRetCode.TWRC_AlreadyCollectGrid); // boss格子已收藏
//                        } else {
//                            opCollectPosRet.setRetCode(TheWarRetCode.TWRC_CannotClearCollectedBossGrid); // boss格子无法清除
//                        }
//                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_OperateCollectionPos_VALUE, opCollectPosRet);
//                        break;
//                    }
                    if (opCollectPosReq.getBAdd()) {
                        if (warPlayer.getPlayerData().getCollectionPosList().contains(opCollectPosReq.getPos())) {
                            opCollectPosRet.setRetCode(TheWarRetCode.TWRC_AlreadyCollectGrid); // 已经收藏过该格子
                            warPlayer.sendTransMsgToServer(MsgIdEnum.SC_OperateCollectionPos_VALUE, opCollectPosRet);
                            break;
                        }
                        SyncExecuteFunction.executeConsumer(warPlayer, entity -> entity.getPlayerData().addCollectionPos(opCollectPosReq.getPos()));
                    } else {
                        int index = warPlayer.getPlayerData().getCollectionPosList().indexOf(opCollectPosReq.getPos());
                        if (index < 0) {
                            opCollectPosRet.setRetCode(TheWarRetCode.TWRC_GridNotCollected); // 未收藏该格子
                            warPlayer.sendTransMsgToServer(MsgIdEnum.SC_OperateCollectionPos_VALUE, opCollectPosRet);
                            break;
                        }
                        SyncExecuteFunction.executeConsumer(warPlayer, entity -> entity.getPlayerData().removeCollectionPos(index));
                    }
                    opCollectPosRet.setRetCode(TheWarRetCode.TWRC_Success);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_OperateCollectionPos_VALUE, opCollectPosRet);
                    break;
                }
                case MsgIdEnum.CS_SubmitDpResource_VALUE: {
                    CS_SubmitDpResource submitDpReq = CS_SubmitDpResource.parseFrom(req.getMsgData());
                    SC_SubmitDpResource.Builder submitDpRet = SC_SubmitDpResource.newBuilder();
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        submitDpRet.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 地图未找到
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_SubmitDpResource_VALUE, submitDpRet);
                        break;
                    }
                    if (!room.checkOwnedAroundPos(warPlayer.getCamp(), submitDpReq.getSubmitPos())) {
                        submitDpRet.setRetCode(TheWarRetCode.TWRC_NotFoundAroundTeamGrid); // 附加没有友方占领格子
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_SubmitDpResource_VALUE, submitDpRet);
                        break;
                    }
                    TheWarRetCode retCode = mapData.submitCrystalDp(warPlayer, submitDpReq.getSubmitPos(), submitDpReq.getSubmitCount());
                    submitDpRet.setRetCode(retCode);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_SubmitDpResource_VALUE, submitDpRet);
                    break;
                }
                case MsgIdEnum.CS_UpdateWarTeamPet_VALUE: {
                    CS_UpdateWarTeamPet updateTeamPetReq = CS_UpdateWarTeamPet.parseFrom(req.getMsgData());
                    SC_UpdateWarTeamPet.Builder updateTeamPetRet = SC_UpdateWarTeamPet.newBuilder();
                    TheWarRetCode ret = SyncExecuteFunction.executeFunction(warPlayer, entity ->
                            entity.updateTeamPet(updateTeamPetReq.getTeamTypeValue(), updateTeamPetReq.getPetInfo()));
                    updateTeamPetRet.setRetCode(ret);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdateWarTeamPet_VALUE, updateTeamPetRet);
                    break;
                }
                case MsgIdEnum.CS_UpdateWarTeamSkill_VALUE: {
                    CS_UpdateWarTeamSkill updateTeamSkillReq = CS_UpdateWarTeamSkill.parseFrom(req.getMsgData());
                    SC_UpdateWarTeamSkill.Builder updateTeamSkillRet = SC_UpdateWarTeamSkill.newBuilder();
                    TheWarRetCode ret = SyncExecuteFunction.executeFunction(warPlayer, entity ->
                            entity.updateTeamSkill(updateTeamSkillReq.getTeamTypeValue(), updateTeamSkillReq.getSkillInfo()));
                    updateTeamSkillRet.setRetCode(ret);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdateWarTeamSkill_VALUE, updateTeamSkillRet);
                    break;
                }
                case MsgIdEnum.CS_QueryWarPetData_VALUE: {
                    CS_QueryWarPetData queryPetsReq = CS_QueryWarPetData.parseFrom(req.getMsgData());
                    SC_QueryWarPetData.Builder queryPetsRet = SC_QueryWarPetData.newBuilder();

                    Collection<WarPetData> petDBList;
                    if (queryPetsReq.getQueryAllPet()) {
                        petDBList = warPlayer.getWarAllPetData().values();
                        warPlayer.getPlayerData().getBanedPetsMap().forEach((banedPetIdx, expireTime) -> {
                            queryPetsRet.addBanedIdx(banedPetIdx);
                            queryPetsRet.addBanExpireTime(expireTime);
                        });
                    } else {
                        WarPlayer queryPlayer = WarPlayerCache.getInstance().queryObject(queryPetsReq.getPlayerIdx());
                        if (queryPlayer == null) {
                            queryPetsRet.setRetCode(TheWarRetCode.TWRC_NotFoundPlayer);
                            warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryWarPetData_VALUE, queryPetsRet);
                            break;
                        }
                        petDBList = queryPlayer.getWarAllPetData(queryPetsReq.getPetsList());
                    }
                    petDBList.forEach(petDB -> queryPetsRet.addPetList(petDB));
                    queryPetsRet.setRetCode(TheWarRetCode.TWRC_Success);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryWarPetData_VALUE, queryPetsRet);
                    break;
                }
                case MsgIdEnum.CS_QueryWarTeam_VALUE: {
                    SC_QueryWarTeam.Builder queryTeamRet = warPlayer.buildTotalTeamInfo();
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryWarTeam_VALUE, queryTeamRet);
                    break;
                }
                case MsgIdEnum.CS_QueryWarGridList_VALUE: {
                    CS_QueryWarGridList queryWarReq = CS_QueryWarGridList.parseFrom(req.getMsgData());
                    SC_QueryWarGridList.Builder retBuilder = SC_QueryWarGridList.newBuilder();
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryWarGridList_VALUE, retBuilder);
                        break;
                    }
                    WarMapGrid grid;
                    for (Position pos : queryWarReq.getQueryWarGridsList()) {
                        grid = mapData.getMapGridByPos(pos);
                        if (grid == null) {
                            continue;
                        }
                        retBuilder.addWarGridsInfo(grid.getWarGridData());
                    }
                    retBuilder.setRetTime(GlobalTick.getInstance().getCurrentTime());
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryWarGridList_VALUE, retBuilder);
                    break;
                }
                case MsgIdEnum.CS_QueryWarGridRecord_VALUE: {
                    SC_QueryWarGridRecord.Builder queryRecordRet = SC_QueryWarGridRecord.newBuilder();
                    queryRecordRet.setRetCode(TheWarRetCode.TWRC_Success);
                    queryRecordRet.addAllRecordData(warPlayer.getPlayerData().getWarGridRecordsList());
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_QueryWarGridRecord_VALUE, queryRecordRet);
                    break;
                }
                case MsgIdEnum.CS_ClaimTheWarMissionReward_VALUE: {
                    SC_ClaimTheWarMissionReward.Builder claimMissionRet = SC_ClaimTheWarMissionReward.newBuilder();
                    TheWarRetCode retCode = SyncExecuteFunction.executeFunction(warPlayer, entity->entity.claimWarSeasonMission());
                    claimMissionRet.setRetCode(retCode);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_ClaimTheWarMissionReward_VALUE, claimMissionRet);
                    break;
                }
                default:
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            LogUtil.printStackTrace(e);
        }
    }
}
