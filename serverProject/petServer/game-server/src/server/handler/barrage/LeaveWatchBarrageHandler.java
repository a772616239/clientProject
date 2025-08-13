package server.handler.barrage;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.barrage.BarrageManager;
import protocol.Barrage;
import protocol.Common;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_ClaimBarrageInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_LeaveWatchBarrage_VALUE;

/**
 * 玩家离开观看弹幕
 */
@MsgId(msgId = MessageId.MsgIdEnum.CS_LeaveWatchBarrage_VALUE)
public class LeaveWatchBarrageHandler extends AbstractBaseHandler<Barrage.CS_LeaveWatchBarrage> {
    @Override
    protected Barrage.CS_LeaveWatchBarrage parse(byte[] bytes) throws Exception {
        return Barrage.CS_LeaveWatchBarrage.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Barrage.CS_LeaveWatchBarrage req, int i) {

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Barrage.SC_LeaveWatchBarrage.Builder msg = Barrage.SC_LeaveWatchBarrage.newBuilder();

        BarrageManager.getInstance().leaveWatch(playerIdx, req.getFunction(), req.getModuleId());

        msg.setCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(SC_LeaveWatchBarrage_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
