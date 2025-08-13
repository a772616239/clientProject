package model.warpServer.crossServer.handler.thewar;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.Battle.SC_EnterFight;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_ResOccupyGrid;

@MsgId(msgId = MsgIdEnum.CS_GS_ResOccupyGrid_VALUE)
public class OccupyGridRetHandler extends AbstractHandler<CS_GS_ResOccupyGrid> {
    @Override
    protected CS_GS_ResOccupyGrid parse(byte[] bytes) throws Exception {
        return CS_GS_ResOccupyGrid.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ResOccupyGrid ret, int i) {
        if (ret.getRetCode().getRetCode() != RetCodeEnum.RCE_Success) {
            SC_EnterFight.Builder builder = SC_EnterFight.newBuilder();
            builder.setRetCode(ret.getRetCode());
            GlobalData.getInstance().sendMsg(ret.getPlayerIdx(), MsgIdEnum.SC_EnterFight_VALUE, builder);
        }
    }
}
