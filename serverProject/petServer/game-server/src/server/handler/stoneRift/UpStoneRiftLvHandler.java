package server.handler.stoneRift;

import cfg.StoneMineLevel;
import cfg.StoneMineLevelObject;
import cfg.StoneRiftLevel;
import cfg.StoneRiftMine;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_UpStoneRiftLv;
import protocol.StoneRift.SC_UpStoneRiftLv;
import util.GameUtil;

import java.util.List;

import static protocol.MessageId.MsgIdEnum.SC_UpStoneRiftLv_VALUE;

@MsgId(msgId = MsgIdEnum.CS_UpStoneRiftLv_VALUE)
public class UpStoneRiftLvHandler extends AbstractBaseHandler<CS_UpStoneRiftLv> {

    @Override
    protected CS_UpStoneRiftLv parse(byte[] bytes) throws Exception {
        return CS_UpStoneRiftLv.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpStoneRiftLv req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_UpStoneRiftLv.Builder msg = SC_UpStoneRiftLv.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_UpStoneRiftLv_VALUE, msg);
            return;
        }
        DbStoneRiftFactory dbStoneRiftFactory = entity.getDB_Builder().getFactoryMap().get(req.getFactoryId());
        if (dbStoneRiftFactory == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_FactoryNotUnlock));
            GlobalData.getInstance().sendMsg(playerId, SC_UpStoneRiftLv_VALUE, msg);
            return;
        }

        StoneMineLevelObject cfg = StoneMineLevel.getByLevel(dbStoneRiftFactory.getLevel());
        if (cfg == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_UpStoneRiftLv_VALUE, msg);
            return;
        }
        if (upMax(dbStoneRiftFactory.getLevel())) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            GlobalData.getInstance().sendMsg(playerId, SC_UpStoneRiftLv_VALUE, msg);
            return;
        }

        List<Common.Consume> consumes = ConsumeUtil.parseToConsumeList(cfg.getUpconsume());
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift, "矿升级"))) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatieralNotEnough));
            GlobalData.getInstance().sendMsg(playerId, SC_UpStoneRiftLv_VALUE, msg);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, et -> {
            entity.upStoneRiftLv(req.getFactoryId());
        });
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_UpStoneRiftLv_VALUE, msg);

    }

    private boolean upMax(int level) {
        return level >= StoneMineLevel._ix_level.keySet().stream().max(Integer::compareTo).orElse(0);
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_UpStoneRiftLv_VALUE, SC_UpStoneRiftLv.newBuilder().setRetCode(retCode));

    }
}
