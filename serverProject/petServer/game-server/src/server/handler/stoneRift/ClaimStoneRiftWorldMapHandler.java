package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.StoneRiftUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbPlayerWorldMap;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_ClaimStoneRiftWorldMap;
import protocol.StoneRift.SC_ClaimStoneRiftWorldMap;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_ClaimStoneRiftWorldMap_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ClaimStoneRiftWorldMap_VALUE)
public class ClaimStoneRiftWorldMapHandler extends AbstractBaseHandler<CS_ClaimStoneRiftWorldMap> {

    @Override
    protected CS_ClaimStoneRiftWorldMap parse(byte[] bytes) throws Exception {
        return CS_ClaimStoneRiftWorldMap.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimStoneRiftWorldMap req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_ClaimStoneRiftWorldMap.Builder msg = SC_ClaimStoneRiftWorldMap.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_ClaimStoneRiftWorldMap_VALUE, msg);
            return;
        }
        DbPlayerWorldMap dbPlayerWorldMap = entity.getDB_Builder().getDbPlayerWorldMap();

        if (dbPlayerWorldMap.getUniqueMapId() == null) {
            SyncExecuteFunction.executeConsumer(entity, et -> {
                entity.randomNextWorldMapId();
            });
        }

        msg.setMapInfo(StoneRiftUtil.toMapInfo(dbPlayerWorldMap,playerId));

        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_ClaimStoneRiftWorldMap_VALUE, msg);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimStoneRiftWorldMap_VALUE, SC_ClaimStoneRiftWorldMap.newBuilder().setRetCode(retCode));

    }
}
