package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_ClaimAllStone;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_ClaimAllStone_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ClaimAllStone_VALUE)
public class ClaimAllStoneHandler extends AbstractBaseHandler<CS_ClaimAllStone> {

    @Override
    protected CS_ClaimAllStone parse(byte[] bytes) throws Exception {
        return CS_ClaimAllStone.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimAllStone req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        StoneRift.SC_ClaimAllStone.Builder msg = StoneRift.SC_ClaimAllStone.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_ClaimAllStone_VALUE, msg);
            return;
        }
        if (entity.getDB_Builder().getNextCanClaimTime() > GlobalTick.getInstance().getCurrentTime()) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_TimeNotReach));
            GlobalData.getInstance().sendMsg(playerId, SC_ClaimAllStone_VALUE, msg);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, et -> {
            entity.claimAllStone();
        });
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_ClaimAllStone_VALUE, msg);

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimAllStone_VALUE, StoneRift.SC_ClaimAllStone.newBuilder().setRetCode(retCode));

    }
}
