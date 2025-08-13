package server.handler.stoneRift.worldMap;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.StoneRiftWorldMapManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbPlayerWorldMap;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCodeEnum;
import protocol.StoneRift.CS_StealStoneResource;
import protocol.StoneRift.SC_StealStoneResource;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_StealStoneResource_VALUE;
import static protocol.StoneRift.StoneRiftScienceEnum.SRSE_StealMoreTimes;

@MsgId(msgId = MsgIdEnum.CS_StealStoneResource_VALUE)
public class StealStoneResHandler extends AbstractBaseHandler<CS_StealStoneResource> {

    @Override
    protected CS_StealStoneResource parse(byte[] bytes) throws Exception {
        return CS_StealStoneResource.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_StealStoneResource req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_StealStoneResource.Builder msg = SC_StealStoneResource.newBuilder();
        if (playerId.equals(req.getPlayerIdx())) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            GlobalData.getInstance().sendMsg(playerId, SC_StealStoneResource_VALUE, msg);
            return;
        }

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_StealStoneResource_VALUE, msg);
            return;
        }
        DbPlayerWorldMap dbPlayerWorldMap = entity.getDB_Builder().getDbPlayerWorldMap();

        if (dbPlayerWorldMap.getUseStealTime() >= dbPlayerWorldMap.getBuyStealTime()
                + StoneRiftCfgManager.getInstance().getBaseStealTime() + entity.getScienceEffect(SRSE_StealMoreTimes)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_TimeUseOut));
            GlobalData.getInstance().sendMsg(playerId, SC_StealStoneResource_VALUE, msg);
            return;
        }
        RetCodeEnum stealResult = StoneRiftWorldMapManager.getInstance().stealRes(playerId, req.getPlayerIdx()
                , req.getFactoryId(), dbPlayerWorldMap.getUniqueMapId(), entity);
        if (stealResult != RetCodeEnum.RCE_Success) {
            msg.setRetCode(GameUtil.buildRetCode(stealResult));
            GlobalData.getInstance().sendMsg(playerId, SC_StealStoneResource_VALUE, msg);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, et -> {
            DbPlayerWorldMap db = entity.getDB_Builder().getDbPlayerWorldMap();
            db.setUseStealTime(db.getUseStealTime() + 1);
            LogUtil.info("player:{} steal stone rift world map player resource,now steal times:{}", playerId, db.getUseStealTime());
        });

        entity.sendPlayerWorldMapInfoUpdate();

        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_StealStoneResource_VALUE, msg);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_StealStoneResource_VALUE, SC_StealStoneResource.newBuilder().setRetCode(retCode));

    }
}
