package server.handler.stoneRift;

import cfg.StoneRiftLevel;
import cfg.StoneRiftLevelObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import org.apache.commons.lang.ArrayUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCodeEnum;
import protocol.StoneRift.CS_RecoveryStoneFactory;
import protocol.StoneRift.SC_RecoveryStoneFactory;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.SC_RecoveryStoneFactory_VALUE;

/**
 * 恢复耐久
 */
@MsgId(msgId = MsgIdEnum.CS_RecoveryStoneFactory_VALUE)
public class RecoveryStoneFactoryHandler extends AbstractBaseHandler<CS_RecoveryStoneFactory> {


    @Override
    protected CS_RecoveryStoneFactory parse(byte[] bytes) throws Exception {
        return CS_RecoveryStoneFactory.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RecoveryStoneFactory req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_RecoveryStoneFactory.Builder msg = SC_RecoveryStoneFactory.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_RecoveryStoneFactory_VALUE, msg);
            return;
        }
        DbStoneRiftFactory dbStoneRiftFactory = entity.getDB_Builder().getFactoryMap().get(req.getFactoryId());
        if (dbStoneRiftFactory == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_FactoryNotUnlock));
            GlobalData.getInstance().sendMsg(playerId, SC_RecoveryStoneFactory_VALUE, msg);
            return;
        }
        if (dbStoneRiftFactory.getCurStore() <= 0) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_FullDurable));
            GlobalData.getInstance().sendMsg(playerId, SC_RecoveryStoneFactory_VALUE, msg);
            return;
        }

        StoneRiftLevelObject cfg = StoneRiftLevel.getByLevel(dbStoneRiftFactory.getLevel());
        if (cfg == null || ArrayUtils.isEmpty(cfg.getRecoveryconsume())) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            GlobalData.getInstance().sendMsg(playerId, SC_RecoveryStoneFactory_VALUE, msg);
            return;
        }

        Consume consume = ConsumeUtil.parseConsume(cfg.getRecoveryconsume());

        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift, "矿升级"))) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatieralNotEnough));
            GlobalData.getInstance().sendMsg(playerId, SC_RecoveryStoneFactory_VALUE, msg);
            return;
        }

        LogUtil.info("player:{} recovery stone factory ,factoryId:{}", playerId, dbStoneRiftFactory.getCfgId());

        SyncExecuteFunction.executeConsumer(entity, et -> {
            entity.recoveryStoneFactory(req.getFactoryId());
        });
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_RecoveryStoneFactory_VALUE, msg);

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_RecoveryStoneFactory_VALUE, SC_RecoveryStoneFactory.newBuilder().setRetCode(retCode));

    }
}
