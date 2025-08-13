package server.handler.thewar;

import cfg.TheWarMapConfig;
import cfg.TheWarMapConfigObject;
import cfg.TheWarSeasonConfigObject;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import common.GlobalTick;
import common.IdGenerator;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.thewar.TheWarManager;
import model.thewar.WarConst.RoomState;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warmap.config.TotalWarMapCfgData;
import model.thewar.warmap.grid.WarMapGrid;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import model.warpServer.WarpServerConst;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_PlayerEnterTheWarRet;
import protocol.ServerTransfer.CS_GS_TheWarRoomInfo;
import protocol.ServerTransfer.GS_CS_PlayerEnterTheWar;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TheWar.SC_UpdatePursuitRewards;
import protocol.TheWar.WarReward;
import protocol.TheWar.WarSeasonMission;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarRetCode;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import static util.JedisUtil.jedis;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_PlayerEnterTheWar_VALUE)
public class EnterTheWarHandler extends AbstractHandler<GS_CS_PlayerEnterTheWar> {
    @Override
    protected GS_CS_PlayerEnterTheWar parse(byte[] bytes) throws Exception {
        return GS_CS_PlayerEnterTheWar.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_PlayerEnterTheWar req, int i) {
        try {
            String ip = WarpServerConst.parseIp(gsChn.channel.remoteAddress().toString().substring(1));
            CS_GS_PlayerEnterTheWarRet.Builder builder = CS_GS_PlayerEnterTheWarRet.newBuilder();
            builder.setPlayerIdx(req.getPlayerInfo().getPlayerId());
            builder.setIsResume(req.getIsResume());
            if (StringHelper.isNull(ip)) {
                builder.setRetCode(TheWarRetCode.TWRC_UnknownError);
                gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                return;
            }
            WarRoom warRoom = WarRoomCache.getInstance().queryObject(req.getRoomIdx());
            WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerInfo().getPlayerId());
            if (warRoom == null) {
                TheWarSeasonConfigObject warSeasonCfg = TheWarManager.getInstance().getWarSeasonConfig();
                warRoom = createNewWarRoom(warSeasonCfg);
                LogUtil.info("Create a new warRoom,id=" + warRoom.getIdx() + ",reqRoomIdx=" + req.getRoomIdx());
                if (warRoom == null) {
                    builder.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 未找到房间或内存申请失败
                    gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                    return;
                }

                WarMapData mapData = new WarMapData(warRoom.getIdx());
                mapData.init(warRoom);

                WarMapGrid bornGrid = mapData.getCurrentBornPosGrid(warRoom.getAllMembers().size());
                if (bornGrid == null) {
                    builder.setRetCode(TheWarRetCode.TWRC_NotFountBornGrid); // 未找到出生点
                    gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                    return;
                }
                int camp = (int) bornGrid.getPropValue(TheWarCellPropertyEnum.TWCP_SpawnGroupId_VALUE);
                if (camp <= 0) {
                    builder.setRetCode(TheWarRetCode.TWRC_BornGridCampError); // 出生点阵营错误
                    gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                    return;
                }

                warPlayer = WarPlayerCache.getInstance().createObject(req.getPlayerInfo().getPlayerId());
                warPlayer.init(warRoom.getIdx(), req);
                bornGrid.setPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE,
                        GameUtil.stringToLong(warPlayer.getIdx(), 0));

                int gridLevel = (int) bornGrid.getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE);
                warRoom.setRoomLevel(gridLevel);
                warRoom.setInitFinish(true);
                warRoom.setRoomController(true);

                List<WarReward> pursuitRewards = new ArrayList<>();
                int pursuitStamina = initPlayer(warPlayer, warRoom, bornGrid, pursuitRewards);

                warRoom.addNewPlayer(warPlayer);
                warRoom.addGridCache(bornGrid.getPos(), bornGrid.buildGridCacheBuilder().build());

                WarMapManager.getInstance().addMapData(mapData);
                WarPlayerCache.getInstance().manageObject(warPlayer);
                WarRoomCache.getInstance().manageObject(warRoom);

                cacheWarRoom(warRoom.getIdx());

                builder.setRoomIdx(warRoom.getIdx());
                builder.setServerIndex(ServerConfig.getInstance().getServer());
                builder.setLevel(warPlayer.getJobTileLevel());
                builder.setRetCode(TheWarRetCode.TWRC_Success);
                builder.setLastSettleTime(warPlayer.getPlayerData().getLastSettleAfkTime());
                gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);

                CS_GS_TheWarRoomInfo.Builder roomInfo = CS_GS_TheWarRoomInfo.newBuilder();
                roomInfo.setRoomIdx(warRoom.getIdx());
                roomInfo.setMapName(warRoom.getMapName());
                roomInfo.setIsFirstTimeEnter(true);
                roomInfo.addAllRoomMembers(warRoom.buildPlayerListInfo());
                roomInfo.setPlayerData(warPlayer.buildPlayerDetailInfo());
                roomInfo.addAllOwnedGrids(warPlayer.buildPlayerOwnedGrids());
//                roomInfo.addCollectionPos(warPlayer.getPlayerData().getBornPos());
//                roomInfo.addAllCollectionPos(mapData.getAllBossGrids());
                roomInfo.addAllCollectionPos(warPlayer.getPlayerData().getCollectionPosList());
                roomInfo.addAllCampInfo(warRoom.getAllCampInfo());
                WarSeasonMission mission = warPlayer.getCurMission();
                if (mission != null) {
                    roomInfo.setCurMission(mission);
                }
                gsChn.send(MsgIdEnum.CS_GS_TheWarRoomInfo_VALUE, roomInfo);

                // 需在roomInfo之后处理
                if (!pursuitRewards.isEmpty() || pursuitStamina > 0) {
                    SC_UpdatePursuitRewards.Builder pursuitRewardBuilder = SC_UpdatePursuitRewards.newBuilder();
                    pursuitRewardBuilder.addAllWarRewards(pursuitRewards);
                    pursuitRewardBuilder.setGainStamina(pursuitStamina);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdatePursuitRewards_VALUE, pursuitRewardBuilder);
                }

                Event event = Event.valueOf(EventType.ET_TheWar_AddPosGroupGrid, warPlayer, warRoom);
                event.pushParam(bornGrid);
                EventManager.getInstance().dispatchEvent(event);


            } else if (warPlayer == null && warRoom.isRoomFull()) {
                builder.setRetCode(TheWarRetCode.TWRC_WarRoomFull); // 房间已满
                gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
            } else  {
                if (warRoom.needClear()) {
                    builder.setRetCode(TheWarRetCode.TWRC_RoomIsClearing);
                    gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                    return;
                }
                if (warRoom.getRoomState() != RoomState.FightingState || warRoom.isPreSettleFlag()) {
                    builder.setRetCode(TheWarRetCode.TWRC_RoomEnded);
                    gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                    return;
                }
                WarMapData mapData = WarMapManager.getInstance().getRoomMapData(warRoom.getIdx());
                if (mapData == null) {
                    builder.setRetCode(TheWarRetCode.TWRC_NotFoundWarMap); // 地图未初始化
                    gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                    return;
                }
                WarMapGrid bornGrid = null;
                List<WarReward> pursuitRewards = null;
                int pursuitStamina = 0;
                boolean isFirstTimeEnter = false;
                if (warPlayer == null) {
                    bornGrid = mapData.getCurrentBornPosGrid(warRoom.getAllMembers().size());
                    if (bornGrid == null) {
                        builder.setRetCode(TheWarRetCode.TWRC_NotFountBornGrid); // 未找到出生点
                        gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                        return;
                    }
                    int camp = (int) bornGrid.getPropValue(TheWarCellPropertyEnum.TWCP_SpawnGroupId_VALUE);
                    if (camp <= 0) {
                        builder.setRetCode(TheWarRetCode.TWRC_BornGridCampError); // 出生点阵营错误
                        gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);
                        return;
                    }
                    warPlayer = WarPlayerCache.getInstance().createObject(req.getPlayerInfo().getPlayerId());
                    warPlayer.init(warRoom.getIdx(), req);

                    long playerId = GameUtil.stringToLong(warPlayer.getIdx(), 0);
                    SyncExecuteFunction.executeConsumer(bornGrid, grid -> grid.setPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE, playerId));

                    pursuitRewards = new ArrayList<>();
                    pursuitStamina = initPlayer(warPlayer, warRoom, bornGrid, pursuitRewards);

                    if (!warRoom.containPlayer(warPlayer.getIdx())) {
                        WarPlayer tmpPlayer = warPlayer;
                        SyncExecuteFunction.executeConsumer(warRoom, room -> room.addNewPlayer(tmpPlayer));
                    }

                    WarPlayerCache.getInstance().manageObject(warPlayer);
                    isFirstTimeEnter = true;
                } else {
                    warPlayer.onPlayerLogin(req);
                }
                builder.setRoomIdx(warRoom.getIdx());
                builder.setServerIndex(ServerConfig.getInstance().getServer());
                builder.setLevel(warPlayer.getJobTileLevel());
                builder.setLastSettleTime(warPlayer.getPlayerData().getLastSettleAfkTime());
                builder.setRetCode(TheWarRetCode.TWRC_Success);
                gsChn.send(MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE, builder);

                CS_GS_TheWarRoomInfo.Builder roomInfo = CS_GS_TheWarRoomInfo.newBuilder();
                roomInfo.setRoomIdx(warRoom.getIdx());
                roomInfo.setMapName(warRoom.getMapName());
                roomInfo.addAllRoomMembers(warRoom.buildPlayerListInfo());
                roomInfo.setPlayerData(warPlayer.buildPlayerDetailInfo());
                roomInfo.addAllOwnedGrids(warPlayer.buildPlayerOwnedGrids());
//                roomInfo.addCollectionPos(warPlayer.getPlayerData().getBornPos());
//                roomInfo.addAllCollectionPos(mapData.getAllBossGrids());
                roomInfo.addAllCollectionPos(warPlayer.getPlayerData().getCollectionPosList());
                roomInfo.addAllCampInfo(warRoom.getAllCampInfo());
                roomInfo.setIsFirstTimeEnter(isFirstTimeEnter);
                WarSeasonMission mission = warPlayer.getCurMission();
                if (mission != null) {
                    roomInfo.setCurMission(mission);
                }
                gsChn.send(MsgIdEnum.CS_GS_TheWarRoomInfo_VALUE, roomInfo);

                if (pursuitRewards != null || pursuitStamina > 0) {
                    SC_UpdatePursuitRewards.Builder pursuitRewardBuilder = SC_UpdatePursuitRewards.newBuilder();
                    pursuitRewardBuilder.addAllWarRewards(pursuitRewards);
                    pursuitRewardBuilder.setGainStamina(pursuitStamina);
                    warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdatePursuitRewards_VALUE, pursuitRewardBuilder);
                }

                if (bornGrid != null) {
                    // 需在roomInfo之后处理
                    bornGrid.broadcastPropData();

                    Event event = Event.valueOf(EventType.ET_TheWar_AddPosGroupGrid, warPlayer, warRoom);
                    event.pushParam(bornGrid);
                    EventManager.getInstance().dispatchEvent(event);
                }
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    protected WarRoom createNewWarRoom(TheWarSeasonConfigObject warSeasonCfg) {
        if (warSeasonCfg == null || TotalWarMapCfgData.getInitMapData(warSeasonCfg.getOpenmapname()) == null) {
            return null;
        }
        String roomIdx = IdGenerator.getInstance().generateId();
        WarRoom warRoom = WarRoomCache.getInstance().createObject(roomIdx);
        warRoom.init(warSeasonCfg);
        return warRoom;
    }

    protected void cacheWarRoom(String roomIdx) {
        String serverIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "");
        jedis.hset(RedisKey.TheWarRoomServerIndex, roomIdx, serverIndex);
    }

    protected int initPlayer(WarPlayer warPlayer, WarRoom warRoom, WarMapGrid bornGrid, List<WarReward> pursuitRewards) {
        TheWarMapConfigObject mapCfg = TheWarMapConfig.getByMapname(warRoom.getMapName());
        if (mapCfg != null) {
            warPlayer.getPlayerData().setStamina(mapCfg.getInitstamina());
        }
        int camp = (int) bornGrid.getPropValue(TheWarCellPropertyEnum.TWCP_SpawnGroupId_VALUE);
        warPlayer.setCamp(camp);
        warPlayer.addOwnedGridPos(bornGrid);
        warPlayer.getPlayerData().setLatestPos(bornGrid.getPos());
        warPlayer.getPlayerData().setLastSettleAfkTime(GlobalTick.getInstance().getCurrentTime());
        warPlayer.addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid, (int) bornGrid.getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE), 1);
        return warPlayer.firstEnterReward(warRoom.getRoomLevel(), pursuitRewards);
    }
}
