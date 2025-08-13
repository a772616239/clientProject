package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_QueryStoneRiftCanClaimReward;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_QueryStoneRiftCanClaimReward_VALUE;

@MsgId(msgId = MsgIdEnum.CS_QueryStoneRiftCanClaimReward_VALUE)
public class QueryCanClaimStoneHandler extends AbstractBaseHandler<CS_QueryStoneRiftCanClaimReward> {

    @Override
    protected CS_QueryStoneRiftCanClaimReward parse(byte[] bytes) throws Exception {
        return CS_QueryStoneRiftCanClaimReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QueryStoneRiftCanClaimReward req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        StoneRift.SC_QueryStoneRiftCanClaimReward.Builder msg = StoneRift.SC_QueryStoneRiftCanClaimReward.newBuilder();

        stoneriftEntity entity = stoneriftCache.getByIdx(playerId);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(playerId, SC_QueryStoneRiftCanClaimReward_VALUE, msg);
            return;
        }
        int factoryId = req.getFactoryId();
        if (queryOne(factoryId)) {
            msg.addAllReward(entity.findCanClaimReward(factoryId));
        } else {
            msg.addAllReward(entity.findAllCanClaimReward());
        }
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(playerId, SC_QueryStoneRiftCanClaimReward_VALUE, msg);

    }

    private boolean queryOne(int factoryId) {
        return factoryId > 0;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_QueryStoneRiftCanClaimReward_VALUE, StoneRift.SC_QueryStoneRiftCanClaimReward.newBuilder().setRetCode(retCode));

    }
}
