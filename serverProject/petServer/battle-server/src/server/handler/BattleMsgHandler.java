package server.handler;

import common.GameConst.EventType;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.cache.PlayerCache;
import model.player.entity.Player;
import model.room.RoomConst.RoomStateEnum;
import model.room.cache.RoomCache;
import model.room.entity.Room;
import model.warpServer.WarpServerConst;
import model.warpServer.WarpServerManager;
import protocol.Battle;
import protocol.Battle.CS_BattleResult;
import protocol.BattleMono.CS_FrameData;
import protocol.Chat.CS_BattleChat;
import protocol.Chat.SC_BattleChatData;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_TransferBattleMsg;
import server.event.Event;
import server.event.EventManager;
import util.LogUtil;

import java.util.Objects;

@MsgId(msgId = MsgIdEnum.GS_BS_TransferBattleMsg_VALUE)
public class BattleMsgHandler extends AbstractHandler<GS_BS_TransferBattleMsg> {
    @Override
    protected GS_BS_TransferBattleMsg parse(byte[] bytes) throws Exception {
        return GS_BS_TransferBattleMsg.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_TransferBattleMsg req, int i) {
        if (req.getMsgId() == MsgIdEnum.CS_BattleWatchQuit_VALUE) {
            battleWatchQuit(req);
            return;
        }
        Player player = PlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        Room room = RoomCache.getInstance().queryObject(player.getRoomId());
        if (room == null) {
            return;
        }
        try {
            switch (req.getMsgId()) {
                case MsgIdEnum.CS_FrameData_VALUE: {
                    CS_FrameData frameData = CS_FrameData.parseFrom(req.getMsgData());
                    SyncExecuteFunction.executeConsumer(room, room1 -> room1.addBattleOperation(frameData));
                }
                break;
                case MsgIdEnum.CS_LoadFinished_VALUE: {
                    SyncExecuteFunction.executeConsumer(player, player1 -> player1.setReadyBattle(true));
                }
                break;
                case MsgIdEnum.CS_BattleResult_VALUE: {
                    CS_BattleResult battleResult = CS_BattleResult.parseFrom(req.getMsgData());
                    if (room.getBattleResult().getBattleId() != battleResult.getBattleId()) {
                        LogUtil.error("Recv BattleResult battleId error");
                        break;
                    }
                    if (battleResult.getIsGMEnd()) {
                        if (room.getRoomState() == RoomStateEnum.battling) {
                            if (ServerConfig.getInstance().canGmEndBattle()) {
                                SyncExecuteFunction.executeConsumer(room, room1 -> {
                                    room1.mergeBattleResult(battleResult);
                                    room1.setRoomState(RoomStateEnum.verifyResult);
                                });
                            } else {
                                SyncExecuteFunction.executeConsumer(player, ply -> {
                                    ply.mergeBattleResult(battleResult);
                                    ply.getBattleResult().setWinnerCamp(3);
                                }); // 作弊视为投降
                            }
                        }
                    } else {
                        SyncExecuteFunction.executeConsumer(
                                player, player1 -> player1.mergeBattleResult(battleResult));
                        if (room.getRoomState() == RoomStateEnum.battling) {
                            Event event = Event.valueOf(EventType.ET_AddSubmitResultCount, player, room);
                            EventManager.getInstance().dispatchEvent(event);
                        }
                    }
                }
                break;
                case MsgIdEnum.CS_BattleChat_VALUE: {
                    CS_BattleChat battleChatReq = CS_BattleChat.parseFrom(req.getMsgData());
                    SC_BattleChatData.Builder builder = SC_BattleChatData.newBuilder();
                    builder.setQuickChatId(battleChatReq.getQuickChatId());
                    room.broadcastBattleChatInfo(player.getIdx(), builder);
                }
                break;
                default:
                    break;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }

    }

    private void battleWatchQuit(GS_BS_TransferBattleMsg req) {
        try {
            Battle.CS_BattleWatchQuit watchQuit = Battle.CS_BattleWatchQuit.parseFrom(req.getMsgData());
            String oldRoomId = RoomCache.getInstance().getWatchPlayerRoomMap().getOrDefault(req.getPlayerIdx(), "");
            Room roomOld = RoomCache.getInstance().queryObject(oldRoomId);
            if (roomOld != null) {
                roomOld.quitBattleWatch(req.getPlayerIdx());
            }
            if (!Objects.equals(oldRoomId, watchQuit.getBattleId())) {
                Room room = RoomCache.getInstance().queryObject(String.valueOf(watchQuit.getBattleId()));
                if (room != null) {
                    room.quitBattleWatch(req.getPlayerIdx());
                }
            }
            RoomCache.getInstance().getWatchPlayerIpMap().remove(req.getPlayerIdx());
            RoomCache.getInstance().getWatchPlayerRoomMap().remove(req.getPlayerIdx());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

}
