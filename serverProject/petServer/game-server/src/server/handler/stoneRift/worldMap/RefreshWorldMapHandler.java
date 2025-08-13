package server.handler.stoneRift.worldMap;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.StoneRiftUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbPlayerWorldMap;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_RefreshStoneRiftWorldMap;
import protocol.StoneRift.SC_RefreshStoneRiftWorldMap;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_RefreshStoneRiftWorldMap_VALUE;

@MsgId(msgId = MsgIdEnum.CS_RefreshStoneRiftWorldMap_VALUE)
public class RefreshWorldMapHandler extends AbstractBaseHandler<CS_RefreshStoneRiftWorldMap> {

    @Override
    protected CS_RefreshStoneRiftWorldMap parse(byte[] bytes) throws Exception {
        return CS_RefreshStoneRiftWorldMap.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RefreshStoneRiftWorldMap req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_RefreshStoneRiftWorldMap.Builder msg = SC_RefreshStoneRiftWorldMap.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_RefreshStoneRiftWorldMap_VALUE, msg);
            return;
        }
        DbPlayerWorldMap worldMap = entity.getDB_Builder().getDbPlayerWorldMap();
        if (worldMap.getUserFreeRefreshTime() >= worldMap.getBuyRefreshTime() + StoneRiftCfgManager.getInstance().getBaseRefreshTime()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_TimeUseOut));
            GlobalData.getInstance().sendMsg(playerId, SC_RefreshStoneRiftWorldMap_VALUE, msg);
            return;
        }

        LogUtil.info("player:{} refresh stone rift world mapId", playerId);

        SyncExecuteFunction.executeConsumer(entity, et -> {
            entity.randomNextWorldMapId();
        });

        buildWorldMapMsg(worldMap, msg);

        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_RefreshStoneRiftWorldMap_VALUE, msg);

    }

    private StoneRift.SC_RefreshStoneRiftWorldMap.Builder buildWorldMapMsg(DbPlayerWorldMap dbPlayerWorldMap, StoneRift.SC_RefreshStoneRiftWorldMap.Builder msg) {
        msg.setMapInfo(StoneRiftUtil.toMapInfo(dbPlayerWorldMap, null));
        return msg;

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_RefreshStoneRiftWorldMap_VALUE, SC_RefreshStoneRiftWorldMap.newBuilder().setRetCode(retCode));

    }
}
