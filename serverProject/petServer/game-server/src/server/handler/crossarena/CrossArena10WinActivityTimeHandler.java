package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_Claim10WinActivityTime;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_Claim10WinActivityTime_VALUE)
public class CrossArena10WinActivityTimeHandler extends AbstractBaseHandler<CS_Claim10WinActivityTime> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }

    @Override
    protected CS_Claim10WinActivityTime parse(byte[] bytes) throws Exception {
        return CS_Claim10WinActivityTime.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_Claim10WinActivityTime req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaManager.getInstance().send10ActivityTime(playerIdx);
    }
}
