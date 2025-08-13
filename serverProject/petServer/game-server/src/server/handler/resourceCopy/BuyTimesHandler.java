package server.handler.resourceCopy;

import cfg.ResourceCopyConfig;
import cfg.ResourceCopyConfigObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import platform.logs.ReasonManager;
import platform.logs.StatisticsLogUtil;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MonthCard;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.PlayerDB.DB_ResourceCopy;
import protocol.ResourceCopy.CS_BuyTimes;
import protocol.ResourceCopy.ResourceCopyTypeEnum;
import protocol.ResourceCopy.SC_BuyTimes;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_BuyTimes_VALUE)
public class BuyTimesHandler extends AbstractBaseHandler<CS_BuyTimes> {
    @Override
    protected CS_BuyTimes parse(byte[] bytes) throws Exception {
        return CS_BuyTimes.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyTimes req, int i) {
        SC_BuyTimes.Builder resultBuilder = SC_BuyTimes.newBuilder();
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
            return;
        }

        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.ResCopy)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
            return;
        }

        ResourceCopyTypeEnum type = req.getType();
        if (type == ResourceCopyTypeEnum.RCTE_Null || req.getBuyTimes() <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
            return;
        }

        ResourceCopyConfigObject cfg = ResourceCopyConfig.getById(type.getNumber());
        if (cfg == null) {
            LogUtil.error("ResourceCopyConfigObject is null, type = " + type);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
            return;
        }

        //检查购买次数是否已达上限
        int canBuyTimes = SyncExecuteFunction.executeFunction(player, p -> {
            DB_ResourceCopy.Builder db_resourceCopy = player.getResourceCopyData(type.getNumber());

            if (db_resourceCopy == null) {
                db_resourceCopy = DB_ResourceCopy.newBuilder();
                player.getDb_data().getResCopyDataBuilder().addResourceCopyData(db_resourceCopy);
            }

            VIPConfigObject vipCfg = VIPConfig.getById(player.getVip());
            int limit = 0;
            if (vipCfg != null) {
                limit = vipCfg.getBuyrescopylimit();
            }
            limit += player.queryPrivilegedCardNum(MonthCard.PrivilegedCardFunction.PCE_ResCopyBuyTime);
            return Math.min(limit - db_resourceCopy.getBuyTimes(), req.getBuyTimes());
        });

        if (canBuyTimes <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ResCopy_BuyTimesLimit));
            gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
            return;
        }

        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, ConsumeUtil.parseAndMulti(cfg.getBuytimesconsume(), canBuyTimes),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ResCopy, StatisticsLogUtil.getResCopyName(req.getTypeValue()) + "购买次数"))) {

            LogUtil.error("ResourceCopyConfigObject is null, type = " + type);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            Builder db_data = player.getDb_data();
            if (db_data == null) {
                LogUtil.error("playerIdx = " + playerIdx + ", entity is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
                return;
            }

            DB_ResourceCopy.Builder resourceCopy = player.getResourceCopyData(type.getNumber());
            if (resourceCopy == null) {
                LogUtil.error("playerIdx = " + playerIdx + ", entity is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
                return;
            }

            resourceCopy.setBuyTimes(resourceCopy.getBuyTimes() + canBuyTimes);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setNewbuyTimes(resourceCopy.getBuyTimes());
            gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ResCopy;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, SC_BuyTimes.newBuilder().setRetCode(retCode));
    }
}
