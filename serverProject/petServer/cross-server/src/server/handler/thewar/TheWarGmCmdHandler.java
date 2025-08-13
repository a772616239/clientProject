package server.handler.thewar;

import cfg.TheWarItemConfig;
import cfg.TheWarItemConfigObject;
import cfg.TheWarJobTileConfig;
import cfg.TheWarJobTileConfigObject;
import common.GlobalData;
import common.IdGenerator;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.WarConst.RoomState;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warmap.grid.FootHoldGrid;
import model.thewar.warmap.grid.WarMapGrid;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.Common.MissionStatusEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TheWarCurrencyLog;
import protocol.ServerTransfer.CS_GS_TheWarCurrencyLog.CurrencyLogData;
import protocol.ServerTransfer.GS_CS_TransGMCommand;
import protocol.ServerTransfer.TheWarGmType;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TheWar.SC_UpdatePlayerJobtile;
import protocol.TheWar.WarSeasonMission;
import protocol.TheWarDefine.SC_JobTileTaskData;
import protocol.TheWarDefine.SC_UpdateNewItem;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarItemData;
import protocol.TheWarDefine.TheWarResourceType;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TransServerCommon.GS_CS_AddPetEnergyGmCmd;
import protocol.TransServerCommon.GS_CS_AddWarDpGmCmd;
import protocol.TransServerCommon.GS_CS_AddWarGoldGmCmd;
import protocol.TransServerCommon.GS_CS_AddWarItemGmCmd;
import protocol.TransServerCommon.GS_CS_OccupyGirdGmCmd;
import protocol.TransServerCommon.GS_CS_SetGridPropGmCmd;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_TransGMCommand_VALUE)
public class TheWarGmCmdHandler extends AbstractHandler<GS_CS_TransGMCommand> {
    @Override
    protected GS_CS_TransGMCommand parse(byte[] bytes) throws Exception {
        return GS_CS_TransGMCommand.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_TransGMCommand cmd, int i) {
        try {
            switch (cmd.getGmCmdTypeValue()) {
//                case TheWarGmType.TWGT_CreateWarRoom_VALUE: {
//
//                    break;
//                }
                case TheWarGmType.TWGT_SettleWarRoom_VALUE: {
                    WarPlayer player = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (player == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(player.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    SyncExecuteFunction.executeConsumer(room, entity -> {
                        entity.settleTheWar();
                        entity.setRoomState(RoomState.EndState);
                        entity.setModified(true);
                    });
                    break;
                }
                case TheWarGmType.TWGT_SettGridProperty_VALUE: {
                    WarPlayer player = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (player == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(player.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        break;
                    }
                    GS_CS_SetGridPropGmCmd gmParam = GS_CS_SetGridPropGmCmd.parseFrom(cmd.getGmParams());
                    WarMapGrid grid = mapData.getMapGridByPos(gmParam.getPos());
                    if (grid == null) {
                        break;
                    }
                    SyncExecuteFunction.executeConsumer(grid, entity -> {
                        entity.setPropValue(gmParam.getPropTypeValue(), gmParam.getPropValue());
                        entity.broadcastPropData();
                    });
                    break;
                }
                case TheWarGmType.TWGT_AddWarGold_VALUE: {

                    WarPlayer player = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (player == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(player.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    GS_CS_AddWarGoldGmCmd gmParam = GS_CS_AddWarGoldGmCmd.parseFrom(cmd.getGmParams());
                    int addVal = gmParam.getAddVal();
                    if (addVal <= 0) {
                        break;
                    }
                    SyncExecuteFunction.executeConsumer(player, entity -> {
                        int oldCount = entity.getPlayerData().getWarGold();
                        int newCount = oldCount + addVal;
                        entity.getPlayerData().setWarGold(newCount);
                        entity.updatePlayerWarCurrency();

                        long accGold = entity.getPlayerData().getAccumulativeWarGold() + addVal;
                        entity.getPlayerData().setAccumulativeWarGold(accGold);
                        entity.addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuCollectTheWarGold, 0, addVal);

                        CS_GS_TheWarCurrencyLog.Builder logBuilder = CS_GS_TheWarCurrencyLog.newBuilder();
                        CurrencyLogData.Builder goldLogBuilder = CurrencyLogData.newBuilder();
                        goldLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarGold).setBeforeAmount(oldCount).setAmount(newCount).setReason("gm");
                        logBuilder.addLogData(goldLogBuilder);
                        logBuilder.setPlayerIdx(entity.getIdx());
                        GlobalData.getInstance().sendMsgToServer(entity.getServerIndex(), MsgIdEnum.CS_GS_TheWarCurrencyLog_VALUE, logBuilder);
                    });
                    break;
                }
                case TheWarGmType.TWGT_AddWarDp_VALUE: {
                    WarPlayer player = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (player == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(player.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    GS_CS_AddWarDpGmCmd gmParam = GS_CS_AddWarDpGmCmd.parseFrom(cmd.getGmParams());
                    int addVal = gmParam.getAddVal();
                    if (addVal <= 0) {
                        break;
                    }
                    SyncExecuteFunction.executeConsumer(player, entity -> {
                        int oldCount = entity.getPlayerData().getWarDP();
                        int newCount = oldCount + addVal;
                        entity.getPlayerData().setWarDP(newCount);
                        entity.updatePlayerWarCurrency();

                        long accDp = entity.getPlayerData().getAccumulativeWarDp() + addVal;
                        entity.getPlayerData().setAccumulativeWarDp(accDp);
                        entity.addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuCollectDP, 0, addVal);

                        CS_GS_TheWarCurrencyLog.Builder logBuilder = CS_GS_TheWarCurrencyLog.newBuilder();
                        CurrencyLogData.Builder goldLogBuilder = CurrencyLogData.newBuilder();
                        goldLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarDoorPoint).setBeforeAmount(oldCount).setAmount(newCount).setReason("gm");
                        logBuilder.addLogData(goldLogBuilder);
                        logBuilder.setPlayerIdx(entity.getIdx());
                        GlobalData.getInstance().sendMsgToServer(entity.getServerIndex(), MsgIdEnum.CS_GS_TheWarCurrencyLog_VALUE, logBuilder);
                    });
                    break;
                }
                case TheWarGmType.TWGT_AddWarItem_VALUE: {
                    WarPlayer player = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (player == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(player.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    GS_CS_AddWarItemGmCmd gmParam = GS_CS_AddWarItemGmCmd.parseFrom(cmd.getGmParams());
                    if (gmParam.getItemNum() <= 0) {
                        return;
                    }
                    TheWarItemConfigObject cfg = TheWarItemConfig.getByItemid(gmParam.getItemCfgId());
                    if (cfg == null) {
                        break;
                    }
                    SyncExecuteFunction.executeConsumer(player, entity -> {
                        SC_UpdateNewItem.Builder builder = SC_UpdateNewItem.newBuilder();
                        for (int j = 0; j < gmParam.getItemNum(); j++) {
                            TheWarItemData.Builder itemBuilder = TheWarItemData.newBuilder();
                            itemBuilder.setIdx(IdGenerator.getInstance().generateId());
                            itemBuilder.setItemCfgId(cfg.getItemid());
                            entity.getPlayerData().getTechDbDataBuilder().putOwedItems(itemBuilder.getIdx(), itemBuilder.build());
                            builder.addItemData(itemBuilder);
                        }
//                        entity.addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuComposeTech, cfg.getQuality(), gmParam.getItemNum());
                        entity.addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuCollectTech, cfg.getQuality(), gmParam.getItemNum());
                        entity.addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuGainTech, cfg.getQuality(), gmParam.getItemNum());
                        entity.sendTransMsgToServer(MsgIdEnum.SC_UpdateNewItem_VALUE, builder);
                    });
                    break;
                }
                case TheWarGmType.TWGT_PromoteJobTile_VALUE: {
                    WarPlayer player = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (player == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(player.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    TheWarRetCode retCode = SyncExecuteFunction.executeFunction(player, entity -> entity.promoteJobTile(true));
                    if (retCode == TheWarRetCode.TWRC_Success) {
                        SC_JobTileTaskData.Builder builder = SC_JobTileTaskData.newBuilder();
                        builder.setJobTileTask(player.builderJobTileTaskData());
                        player.sendTransMsgToServer(MsgIdEnum.SC_JobTileTaskData_VALUE, builder);

                        SC_UpdatePlayerJobtile.Builder newJobTileData = SC_UpdatePlayerJobtile.newBuilder();
                        newJobTileData.setPlayerId(player.getIdx());
                        newJobTileData.setNewJobtileLevel(player.getJobTileLevel());
                        room.broadcastMsg(MsgIdEnum.SC_UpdatePlayerJobtile_VALUE, newJobTileData, true);
                    }
                    break;
                }
                case TheWarGmType.TWGT_AddPetEnergy_VALUE: {
                    WarPlayer player = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (player == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(player.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    GS_CS_AddPetEnergyGmCmd gmCmd = GS_CS_AddPetEnergyGmCmd.parseFrom(cmd.getGmParams());
                    SyncExecuteFunction.executeConsumer(player, entity -> {
                        entity.getPlayerData().setStamina(entity.getPlayerData().getStamina() + gmCmd.getAddVal());
                        entity.sendPlayerStamina();
                    });
                    break;
                }
                case TheWarGmType.TWGT_OccupyGrid_VALUE: {
                    WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (warPlayer == null) {
                        break;
                    }
                    TheWarJobTileConfigObject cfg = TheWarJobTileConfig.getById(warPlayer.getJobTileLevel());
                    if (cfg == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(warPlayer.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    GS_CS_OccupyGirdGmCmd gmCmd = GS_CS_OccupyGirdGmCmd.parseFrom(cmd.getGmParams());
                    WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
                    if (mapData == null) {
                        break;
                    }
                    WarMapGrid grid = mapData.getMapGridByPos(gmCmd.getOccupyGird());
                    if (grid instanceof FootHoldGrid) {
                        FootHoldGrid fhGrid = (FootHoldGrid) grid;
                        long ownerId = fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
                        if (ownerId == 0) {
                            SyncExecuteFunction.executeConsumer(fhGrid, entity -> {
                                entity.setPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE, GameUtil.stringToLong(warPlayer.getIdx(), 0));
                                entity.settleBattle(warPlayer, true, null, 3);
                            });
                        }
                    }
                    break;
                }
                case TheWarGmType.TWGT_FinishCurTask_VALUE: {
                    WarPlayer player = WarPlayerCache.getInstance().queryObject(cmd.getPlayerIdx());
                    if (player == null) {
                        break;
                    }
                    WarRoom room = WarRoomCache.getInstance().queryObject(player.getRoomIdx());
                    if (room == null) {
                        break;
                    }
                    SyncExecuteFunction.executeConsumer(player, entity -> {
                        int curIndex = entity.getPlayerData().getCurMissionIndex();
                        if(curIndex < 0 || curIndex >= entity.getPlayerData().getWarMissionCount()) {
                            return;
                        }
                        WarSeasonMission.Builder mission = entity.getPlayerData().getWarMissionBuilder(curIndex);
                        if (mission == null) {
                            return;
                        }
                        mission.setStatus(MissionStatusEnum.MSE_Finished);
                        mission.getWarTaskBuilder().setFinished(true);
                        entity.updateCurSeasonMission();
                    });
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
