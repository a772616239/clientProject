package server.handler.thewar;

import cfg.TheWarMapConfig;
import cfg.TheWarMapConfigObject;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.WarConst.RoomState;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_BuyStaminaCheckRet;
import protocol.ServerTransfer.GS_CS_BuyStaminaCheck;
import protocol.TheWarDefine.TheWarRetCode;

@MsgId(msgId = MsgIdEnum.GS_CS_BuyStaminaCheck_VALUE)
public class BuyStaminaCheckHandler extends AbstractHandler<GS_CS_BuyStaminaCheck> {
    @Override
    protected GS_CS_BuyStaminaCheck parse(byte[] bytes) throws Exception {
        return GS_CS_BuyStaminaCheck.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_BuyStaminaCheck req, int i) {
        CS_GS_BuyStaminaCheckRet.Builder retBuilder = CS_GS_BuyStaminaCheckRet.newBuilder();
        retBuilder.setPlayerIdx(req.getPlayerIdx());
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(req.getRoomIdx());
        if (warRoom == null || warRoom.getRoomState() != RoomState.FightingState) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomEnded); // 房间已结束
            gsChn.send(MsgIdEnum.CS_GS_BuyStaminaCheckRet_VALUE, retBuilder);
            return;
        }
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (warPlayer == null) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_NotFoundPlayer);
            gsChn.send(MsgIdEnum.CS_GS_BuyStaminaCheckRet_VALUE, retBuilder);
            return;
        }
        TheWarMapConfigObject mapCfg = TheWarMapConfig.getByMapname(warRoom.getMapName());
        if (mapCfg == null) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_ConfigNotFound); // 配置错误
            gsChn.send(MsgIdEnum.CS_GS_BuyStaminaCheckRet_VALUE, retBuilder);
            return;
        }
        if (warPlayer.getPlayerData().getStamina() > mapCfg.getMaxenergy()) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_PlayerStaminaLimit); // 已达最大体力值
            gsChn.send(MsgIdEnum.CS_GS_BuyStaminaCheckRet_VALUE, retBuilder);
            return;
        }
        retBuilder.setRetCode(TheWarRetCode.TWRC_Success);
        gsChn.send(MsgIdEnum.CS_GS_BuyStaminaCheckRet_VALUE, retBuilder);
    }
}
