package model.room.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import common.GameConst;
import common.GameConst.EventType;
import common.IdGenerator;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import model.obj.ObjCache;
import model.obj.ObjPool;
import model.player.cache.PlayerCache;
import model.player.entity.Player;
import model.room.RoomConst.RoomStateEnum;
import model.room.entity.Room;
import model.warpServer.WarpServerManager;
import protocol.Battle;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.SC_EnterFight;
import protocol.ServerTransfer.ApplyPvpBattleData;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import protocol.ServerTransfer.ReplyPvpBattleData;
import protocol.ServerTransfer.ServerTypeEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

public class RoomCache extends ObjCache<Room> {
    private static RoomCache instance = null;

    public static RoomCache getInstance() {
        if (instance == null) {
            instance = new RoomCache();
            instance.setObjPool(new ObjPool<>(() -> new Room()));
        }
        return instance;
    }

    private Map<String, Integer> watchPlayerIpMap = new ConcurrentHashMap<>();
    private Map<String, String> watchPlayerRoomMap = new ConcurrentHashMap<>();

    public boolean checkBattlePlayerInfo(PvpBattlePlayerInfo playerInfo) {
        if (StringHelper.isNull(playerInfo.getPlayerInfo().getPlayerId())) {
            return false;
        }
        if (playerInfo.getPetListCount() <= 0) {
            return false;
        }
        if (GameConst.ROOBOTID.equals(playerInfo.getPlayerInfo().getPlayerId())) {
            return true;
        }
        if (WarpServerManager.getInstance().getGameServerChannel(playerInfo.getFromSvrIndex()) == null) {
            return false;
        }
        Player player = PlayerCache.getInstance().queryObject(playerInfo.getPlayerInfo().getPlayerId());
        if (player != null && !StringHelper.isNull(player.getRoomId())) {
            return false;
        }
        return true;
    }

    public ReplyPvpBattleData.Builder createRoom(ApplyPvpBattleData applyData, ServerTypeEnum serverType, int serverIndex) {
        return createRoom(applyData, serverType, serverIndex, 0);
    }

    public ReplyPvpBattleData.Builder createRoom(ApplyPvpBattleData applyData, ServerTypeEnum serverType, int serverIndex, int buffId) {
        GameServerTcpChannel channel;
        if (serverType == ServerTypeEnum.STE_GameServer) {
            channel = WarpServerManager.getInstance().getGameServerChannel(serverIndex);
        } else {
            channel = WarpServerManager.getInstance().getCrossServerChannel(serverIndex);
        }
        ReplyPvpBattleData.Builder replyBuilder = ReplyPvpBattleData.newBuilder();
        replyBuilder.setSubBattleType(applyData.getSubBattleType());
        replyBuilder.addAllParams(applyData.getParamList());
        if (PlayerCache.getInstance().isServerFull()) {
            replyBuilder.setResult(false);
            LogUtil.error("Create room failed for server full, subBattleType:" + applyData.getSubBattleType());
            return replyBuilder;
        }
        if (channel == null || applyData.getFightMakeId() <= 0) {
            replyBuilder.setResult(false);
            return replyBuilder;
        }
        boolean playerInfoInvalid = false;
        for (PvpBattlePlayerInfo playerInfo : applyData.getPlayerInfoList()) {
            if (!checkBattlePlayerInfo(playerInfo)) {
                playerInfoInvalid = true;
                break;
            }
        }
        if (playerInfoInvalid) {
            replyBuilder.setResult(false);
            return replyBuilder;
        }
        long id = IdGenerator.getInstance().generateIdNum();
        Room room = createObject(GameUtil.longToString(id, ""));
        room.initRoom(serverType, serverIndex, applyData);
        room.setRobotNum(0);
        Player player;
        SC_EnterFight.Builder enterFightData = room.getEnterFightData();
        for (PvpBattlePlayerInfo playerInfo : applyData.getPlayerInfoList()) {
        	// 机器人
        	if (playerInfo.getIsAI() > 0) {
        		player = new Player();
                player.setIdx(playerInfo.getPlayerInfo().getPlayerId());
                player.onPlayerLogin(playerInfo, room.getIdx(), false);
                player.setReadyBattle(true);
                player.setRobot(true);
                room.addMember(player);
                room.setRobotNum(room.getRobotNum()+1);
        	} else {
                player = PlayerCache.getInstance().queryObject(playerInfo.getPlayerInfo().getPlayerId());
                if (player == null) {
                    player = PlayerCache.getInstance().createObject(playerInfo.getPlayerInfo().getPlayerId());
                    player.onPlayerLogin(playerInfo, room.getIdx(), false);
                    PlayerCache.getInstance().manageObject(player);
                } else if (StringHelper.isNull(player.getRoomId())) {
                    player.clear();
                    Event event = Event.valueOf(EventType.ET_Login, room, player);
                    event.pushParam(playerInfo, room.getIdx(), false);
                    EventManager.getInstance().dispatchEvent(event);
                }
                room.addMember(player);
            }
            replyBuilder.addPlayerList(playerInfo);

            BattlePlayerInfo.Builder battlePlyInfo = BattlePlayerInfo.newBuilder();
            battlePlyInfo.setPlayerInfo(playerInfo.getPlayerInfo());
            battlePlyInfo.setCamp(playerInfo.getCamp());
            battlePlyInfo.addAllPetList(playerInfo.getPetListList());
            battlePlyInfo.addAllFriendHelpPets(playerInfo.getFriendPetsList());
            battlePlyInfo.addAllPlayerSkillIdList(playerInfo.getPlayerSkillIdListList());
            battlePlyInfo.setIsAuto(playerInfo.getIsAuto());
            battlePlyInfo.setPlayerExtData(playerInfo.getPlayerExtData());
            enterFightData.addPlayerInfo(battlePlyInfo);
            enterFightData.addAllExtendProp(playerInfo.getExtendPropList());
        }
        if (buffId > 0) {
            Battle.PetBuffData.Builder dataBuilder = Battle.PetBuffData.newBuilder();
            dataBuilder.setBuffCount(1);
            dataBuilder.setBuffCfgId(buffId);
            Battle.ExtendProperty.Builder buff = Battle.ExtendProperty.newBuilder();
            buff.setCamp(1);
            buff.addBuffData(dataBuilder.build());
            enterFightData.addExtendProp(buff);
        }
        replyBuilder.setResult(true);
        replyBuilder.setBattleId(id);
        replyBuilder.setFightMakeId(applyData.getFightMakeId());
        replyBuilder.setRandSeed(room.getRandSeed());

        enterFightData.setCamp(1);
        enterFightData.setBattleId(id);
        enterFightData.setBattleType(BattleTypeEnum.BTE_PVP);
        enterFightData.setFightMakeId(applyData.getFightMakeId());
        enterFightData.setSubType(applyData.getSubBattleType());
        enterFightData.setRandSeed(room.getRandSeed());
        manageObject(room);

        return replyBuilder;
    }

    public void onTick(long curTime) {
        for (Room room : objMap.values()) {
            if (room.getRoomState()== RoomStateEnum.closed) {
                removeObject(room);
            } else {
                SyncExecuteFunction.executeConsumer(room, room1 -> room1.onTick(curTime));
            }
        }
    }

    public Map<String, Integer> getWatchPlayerIpMap() {
        return watchPlayerIpMap;
    }

    public void setWatchPlayerIpMap(Map<String, Integer> watchPlayerIpMap) {
        this.watchPlayerIpMap = watchPlayerIpMap;
    }

    public Map<String, String> getWatchPlayerRoomMap() {
        return watchPlayerRoomMap;
    }

    public void setWatchPlayerRoomMap(Map<String, String> watchPlayerRoomMap) {
        this.watchPlayerRoomMap = watchPlayerRoomMap;
    }
}
