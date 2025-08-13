package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.consume.ConsumeManager;
import model.pet.dbCache.petCache;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_UnlockStoneFactory;
import protocol.StoneRift.SC_UnlockStoneFactory;
import util.EventUtil;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_UnlockStoneFactory_VALUE;
import static protocol.TargetSystem.TargetTypeEnum.TTE_StoneRift_AdvancedFactory;

@MsgId(msgId = MsgIdEnum.CS_UnlockStoneFactory_VALUE)
public class UnlockStoneFactoryHandler extends AbstractBaseHandler<CS_UnlockStoneFactory> {

    @Override
    protected CS_UnlockStoneFactory parse(byte[] bytes) throws Exception {
        return CS_UnlockStoneFactory.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UnlockStoneFactory req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_UnlockStoneFactory.Builder msg = unLockStoneFactory(playerId, req);

        GlobalData.getInstance().sendMsg(playerId, SC_UnlockStoneFactory_VALUE, msg);

    }

    private SC_UnlockStoneFactory.Builder unLockStoneFactory(String playerId, CS_UnlockStoneFactory req) {
        StoneRift.SC_UnlockStoneFactory.Builder msg = StoneRift.SC_UnlockStoneFactory.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }

        DbStoneRiftFactory factory = entity.getDB_Builder().getFactoryMap().get(req.getId());
        if (factory != null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_FactoryAlreadyUnlock));
            return msg;
        }
        Common.Consume unlockConsume = StoneRiftCfgManager.getInstance().findUnlockConsume(req.getId());

        if (unlockConsume == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            return msg;
        }

        if (!ConsumeManager.getInstance().consumeMaterial(playerId, unlockConsume,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift_Unlock))) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought));
            return msg;
        }

        SyncExecuteFunction.executeConsumer(entity, et -> {
            entity.unlockFactory(req.getId());
            EventUtil.triggerUpdateTargetProgress(playerId,TTE_StoneRift_AdvancedFactory,1,0);
        });
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        return msg;

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_UnlockStoneFactory_VALUE, SC_UnlockStoneFactory.newBuilder().setRetCode(retCode));

    }
}
