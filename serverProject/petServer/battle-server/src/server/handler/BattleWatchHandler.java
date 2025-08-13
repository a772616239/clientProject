package server.handler;

import common.load.ServerConfig;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.room.cache.RoomCache;
import model.room.entity.Room;
import protocol.Battle;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.ServerTransfer;
import protocol.ServerTransfer.GS_BS_BattleWatch;

import java.util.Objects;

@MsgId(msgId = MsgIdEnum.GS_BS_BattleWatch_VALUE)
public class BattleWatchHandler extends AbstractHandler<GS_BS_BattleWatch> {
    @Override
    protected GS_BS_BattleWatch parse(byte[] bytes) throws Exception {
        return GS_BS_BattleWatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_BattleWatch req, int i) {
        // 请求观战的玩家不需要登录本服务器
        ServerTransfer.BS_GS_BattleWatch.Builder msgBack = ServerTransfer.BS_GS_BattleWatch.newBuilder();
        msgBack.setPlayerId(req.getPlayerId());
        msgBack.setBsid(ServerConfig.getInstance().getServer());
        String oldRoomId = RoomCache.getInstance().getWatchPlayerRoomMap().getOrDefault(req.getPlayerId(), "");
        RoomCache.getInstance().getWatchPlayerIpMap().put(req.getPlayerId(), req.getSvrIndex());
        if (Objects.equals(oldRoomId, req.getRoomId())) {
            Room room = RoomCache.getInstance().queryObject(req.getRoomId());
            if (room != null) {
                Battle.SC_BattleWatch.Builder msgWatch = room.jionBattleWatch(req.getPlayerId(), req.getSvrIndex());
                if (null == msgWatch) {
                    msgBack.setRetCode(RetCodeId.RetCodeEnum.RCE_Battle_NotInBattle);
                } else {
                    msgBack.setRevertData(msgWatch);
                }
                gsChn.send(MsgIdEnum.BS_GS_BattleWatch_VALUE, msgBack);
            }
        } else {
            Room roomOld = RoomCache.getInstance().queryObject(oldRoomId);
            if (roomOld != null) {
                roomOld.quitBattleWatch(req.getPlayerId());
            }
            RoomCache.getInstance().getWatchPlayerRoomMap().remove(req.getPlayerId());
            Room room = RoomCache.getInstance().queryObject(req.getRoomId());
            if (room != null) {
                RoomCache.getInstance().getWatchPlayerRoomMap().put(req.getPlayerId(), req.getRoomId());
                Battle.SC_BattleWatch.Builder msgWatch = room.jionBattleWatch(req.getPlayerId(), req.getSvrIndex());
                if (null == msgWatch) {
                    msgBack.setRetCode(RetCodeId.RetCodeEnum.RCE_Battle_NotInBattle);
                } else {
                    msgBack.setRevertData(msgWatch);
                }
                gsChn.send(MsgIdEnum.BS_GS_BattleWatch_VALUE, msgBack);
            }
        }
    }

}
