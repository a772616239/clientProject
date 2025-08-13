package server.handler.thewar;

import cfg.TheWarConstConfig;
import cfg.TheWarMapConfig;
import cfg.TheWarMapConfigObject;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.WarConst.RoomState;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_BuyStamina;
import protocol.TheWarDefine.SC_UpdatePlayerStamia;

@MsgId(msgId = MsgIdEnum.GS_CS_BuyStamina_VALUE)
public class BuyStaminaHandler extends AbstractHandler<GS_CS_BuyStamina> {
    @Override
    protected GS_CS_BuyStamina parse(byte[] bytes) throws Exception {
        return GS_CS_BuyStamina.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_BuyStamina req, int i) {
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(req.getRoomIdx());
        if (warRoom == null || warRoom.getRoomState() != RoomState.FightingState) {
            return;
        }
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (warPlayer == null) {
            return;
        }
        TheWarMapConfigObject mapCfg = TheWarMapConfig.getByMapname(warRoom.getMapName());
        if (mapCfg == null) {
            return;
        }
        int maxStamina = mapCfg.getMaxenergy();
        int plyStamina = warPlayer.getPlayerData().getStamina();
        if (plyStamina >= maxStamina) {
            return;
        }
        int newStamina = Math.min(maxStamina, plyStamina + TheWarConstConfig.getById(GameConst.ConfigId).getBustamiavalue());
        SyncExecuteFunction.executeConsumer(warPlayer, entity -> entity.getPlayerData().setStamina(newStamina));

        SC_UpdatePlayerStamia.Builder builder = SC_UpdatePlayerStamia.newBuilder();
        builder.setNewValue(newStamina);
        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdatePlayerStamia_VALUE, builder);
    }
}
