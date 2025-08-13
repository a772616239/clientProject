package server.handler.stoneRift.worldMap;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.*;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbPlayerWorldMap;
import model.stoneRift.entity.DbStoneRiftSteal;
import model.stoneRift.entity.StoneRiftMsg;
import model.stoneRift.entity.StoneRiftWorldMapPlayer;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_QueryWorldMapPlayerDetail;
import protocol.StoneRift.SC_QueryWorldMapPlayerDetail;
import util.GameUtil;

import java.util.List;

import static protocol.MessageId.MsgIdEnum.SC_QueryWorldMapPlayerDetail_VALUE;

@MsgId(msgId = MsgIdEnum.CS_QueryWorldMapPlayerDetail_VALUE)
public class QueryWorldMapPlayerDetailHandler extends AbstractBaseHandler<CS_QueryWorldMapPlayerDetail> {

    @Override
    protected CS_QueryWorldMapPlayerDetail parse(byte[] bytes) throws Exception {
        return CS_QueryWorldMapPlayerDetail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryWorldMapPlayerDetail req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_QueryWorldMapPlayerDetail.Builder msg = SC_QueryWorldMapPlayerDetail.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_QueryWorldMapPlayerDetail_VALUE, msg);
            return;
        }
        DbPlayerWorldMap dbPlayerWorldMap = entity.getDB_Builder().getDbPlayerWorldMap();

        StoneRiftWorldMapPlayer mapPlayer = StoneRiftWorldMapManager.getInstance().findMapPlayerById(dbPlayerWorldMap.getUniqueMapId(), req.getPlayerIdx());

        if (mapPlayer == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Player_QueryPlayerNotExist));
            GlobalData.getInstance().sendMsg(playerId, SC_QueryWorldMapPlayerDetail_VALUE, msg);
            return;
        }
        msg.setPlayerIdx(mapPlayer.getPlayerIdx());
        msg.setHeader(mapPlayer.getHeader());
        msg.setRiftLv(mapPlayer.getRiftLv());
        msg.setPlayerName(mapPlayer.getPlayerName());
        msg.setExp(mapPlayer.getExp());
        msg.setHeadBroderId(mapPlayer.getHeadBroder());
        boolean canSteal = mapPlayer.isCanSteal(playerId);
        for (DbStoneRiftSteal stel : mapPlayer.getCanStealMap().values()) {
            StoneRift.WorldStoneFactoryVo.Builder item = StoneRift.WorldStoneFactoryVo.newBuilder();
            item.setLevel(stel.getLevel());
            item.setCanSteal(canSteal && stel.getStealCount() < StoneRiftCfgManager.getInstance().getCanStolenTime());
            item.setCfgId(stel.getFactoryId());
            msg.addFactory(item);
        }
        List<StoneRiftMsg> stoneRiftMsgs = StoneRiftManager.getInstance().claimStoneRiftMsg(req.getPlayerIdx(), 0);
        for (StoneRiftMsg stoneRiftMsg : stoneRiftMsgs) {
            msg.addMsg(StoneRiftUtil.toVo(stoneRiftMsg));
        }
        msg.setMapId(mapPlayer.getBackGroundId());
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_QueryWorldMapPlayerDetail_VALUE, msg);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_QueryWorldMapPlayerDetail_VALUE, SC_QueryWorldMapPlayerDetail.newBuilder().setRetCode(retCode));

    }
}
