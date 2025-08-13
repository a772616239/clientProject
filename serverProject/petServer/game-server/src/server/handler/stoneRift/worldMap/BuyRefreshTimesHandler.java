package server.handler.stoneRift.worldMap;

import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.player.util.PlayerUtil;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbPlayerWorldMap;
import model.stoneRift.stoneriftEntity;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_BuyWorldMapRefreshTime;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_BuyWorldMapRefreshTime_VALUE;

@MsgId(msgId = MsgIdEnum.CS_BuyWorldMapRefreshTime_VALUE)
public class BuyRefreshTimesHandler extends AbstractBaseHandler<CS_BuyWorldMapRefreshTime> {

    @Override
    protected CS_BuyWorldMapRefreshTime parse(byte[] bytes) throws Exception {
        return CS_BuyWorldMapRefreshTime.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyWorldMapRefreshTime req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        StoneRift.SC_BuyWorldMapRefreshTime.Builder msg = StoneRift.SC_BuyWorldMapRefreshTime.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_BuyWorldMapRefreshTime_VALUE, msg);
            return;
        }
        int vipLv = PlayerUtil.queryPlayerVipLv(playerId);
        VIPConfigObject cfg = VIPConfig.getById(vipLv);
        if (cfg == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_ConfigNotExist));
            GlobalData.getInstance().sendMsg(playerId, SC_BuyWorldMapRefreshTime_VALUE, msg);
            return;
        }
        DbPlayerWorldMap dbPlayerWorldMap = entity.getDB_Builder().getDbPlayerWorldMap();
        if (dbPlayerWorldMap.getBuyRefreshTime() >= cfg.getStonrriftrefreshlimitbuy()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_LimitBuy));
            GlobalData.getInstance().sendMsg(playerId, SC_BuyWorldMapRefreshTime_VALUE, msg);
            return;
        }
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, StoneRiftCfgManager.getInstance().getBuyStealConsume(),
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift, "购买偷取次数"))) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought));
            GlobalData.getInstance().sendMsg(playerId, SC_BuyWorldMapRefreshTime_VALUE, msg);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, et -> {
            DbPlayerWorldMap worldMap = entity.getDB_Builder().getDbPlayerWorldMap();
            worldMap.setBuyRefreshTime(worldMap.getBuyRefreshTime() + 1);
        });

        entity.sendPlayerWorldMapInfoUpdate();

        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_BuyWorldMapRefreshTime_VALUE, msg);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyWorldMapRefreshTime_VALUE, StoneRift.SC_BuyWorldMapRefreshTime.newBuilder().setRetCode(retCode));

    }
}
