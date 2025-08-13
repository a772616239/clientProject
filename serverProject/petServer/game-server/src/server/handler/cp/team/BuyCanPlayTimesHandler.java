package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_BuyCanPlayTimes;
import protocol.CpFunction.SC_BuyCanPlayTimes;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 申请加入组队
 */
@MsgId(msgId = MsgIdEnum.CS_BuyCanPlayTimes_VALUE)
public class BuyCanPlayTimesHandler extends AbstractBaseHandler<CS_BuyCanPlayTimes> {
    @Override
    protected CS_BuyCanPlayTimes parse(byte[] bytes) throws Exception {
        return CS_BuyCanPlayTimes.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyCanPlayTimes req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().buyCanPlayTimes(playerIdx);
        SC_BuyCanPlayTimes.Builder msg = SC_BuyCanPlayTimes.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_BuyCanPlayTimes_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyCanPlayTimes_VALUE, SC_BuyCanPlayTimes.newBuilder().setRetCode(retCode));
    }
}
