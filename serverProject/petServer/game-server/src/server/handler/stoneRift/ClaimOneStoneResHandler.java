package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_ClaimOneStone;
import protocol.StoneRift.SC_ClaimOneStone;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_ClaimOneStone_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ClaimOneStone_VALUE)
public class ClaimOneStoneResHandler extends AbstractBaseHandler<CS_ClaimOneStone> {

    @Override
    protected CS_ClaimOneStone parse(byte[] bytes) throws Exception {
        return CS_ClaimOneStone.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimOneStone req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_ClaimOneStone.Builder msg = SC_ClaimOneStone.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_ClaimOneStone_VALUE, msg);
            return;
        }
        DbStoneRiftFactory dbStoneRiftFactory = entity.getDB_Builder().getFactoryMap().get(req.getCfgId());
        if (dbStoneRiftFactory==null){
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_FactoryNotUnlock));
            GlobalData.getInstance().sendMsg(playerId, SC_ClaimOneStone_VALUE, msg);
            return ;
        }
        if (CollectionUtils.isEmpty(dbStoneRiftFactory.getSettleReward())){
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_EmptyRewards));
            GlobalData.getInstance().sendMsg(playerId, SC_ClaimOneStone_VALUE, msg);
            return ;
        }
        if (dbStoneRiftFactory.getNextCanClaimTime() > GlobalTick.getInstance().getCurrentTime()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_TimeNotReach));
            GlobalData.getInstance().sendMsg(playerId, SC_ClaimOneStone_VALUE, msg);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, et -> {
            entity.claimOneStone(req.getCfgId());
        });
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_ClaimOneStone_VALUE, msg);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimOneStone_VALUE, SC_ClaimOneStone.newBuilder().setRetCode(retCode));

    }
}
