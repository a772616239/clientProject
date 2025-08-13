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
import protocol.ServerTransfer.CS_GS_BuyBackCheckRet;
import protocol.ServerTransfer.GS_CS_BuyBackCheck;
import protocol.TheWarDefine.TheWarRetCode;

@MsgId(msgId = MsgIdEnum.GS_CS_BuyBackCheck_VALUE)
public class BuyBackChekHandler extends AbstractHandler<GS_CS_BuyBackCheck> {
    @Override
    protected GS_CS_BuyBackCheck parse(byte[] bytes) throws Exception {
        return GS_CS_BuyBackCheck.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_BuyBackCheck req, int i) {
        CS_GS_BuyBackCheckRet.Builder retBuilder = CS_GS_BuyBackCheckRet.newBuilder();
        retBuilder.setPlayerIdx(req.getPlayerIdx());
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(req.getRoomIdx());
        if (warRoom == null || warRoom.getRoomState() != RoomState.FightingState) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomEnded); // 房间已结束
            gsChn.send(MsgIdEnum.CS_GS_BuyBackCheckRet_VALUE, retBuilder);
            return;
        }
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (warPlayer == null) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_NotFoundPlayer);
            gsChn.send(MsgIdEnum.CS_GS_BuyBackCheckRet_VALUE, retBuilder);
            return;
        }
        TheWarMapConfigObject mapCfg = TheWarMapConfig.getByMapname(warRoom.getMapName());
        if (mapCfg == null) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_ConfigNotFound); // 配置错误
            gsChn.send(MsgIdEnum.CS_GS_BuyBackCheckRet_VALUE, retBuilder);
            return;
        }
        if (warPlayer.isAllPetsFullHp()) {
            retBuilder.setRetCode(TheWarRetCode.TWRC_PetIsAllFullHp); // 宠物都是满血，未受伤
            gsChn.send(MsgIdEnum.CS_GS_BuyBackCheckRet_VALUE, retBuilder);
            return;
        }
        retBuilder.setRetCode(TheWarRetCode.TWRC_Success);
        gsChn.send(MsgIdEnum.CS_GS_BuyBackCheckRet_VALUE, retBuilder);
    }
}
