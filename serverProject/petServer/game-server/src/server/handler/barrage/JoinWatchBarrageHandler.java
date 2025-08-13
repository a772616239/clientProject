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

import static protocol.MessageId.MsgIdEnum.SC_AddJoinWatchBarrage_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_ClaimBarrageInfo_VALUE;

/**
 * 玩家加入观看弹幕
 */
@MsgId(msgId = MessageId.MsgIdEnum.CS_AddJoinWatchBarrage_VALUE)
public class JoinWatchBarrageHandler extends AbstractBaseHandler<Barrage.CS_AddJoinWatchBarrage> {
    @Override
    protected Barrage.CS_AddJoinWatchBarrage parse(byte[] bytes) throws Exception {
        return Barrage.CS_AddJoinWatchBarrage.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Barrage.CS_AddJoinWatchBarrage req, int i) {

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Barrage.SC_AddJoinWatchBarrage.Builder msg = Barrage.SC_AddJoinWatchBarrage.newBuilder();

        BarrageManager.getInstance().joinWatch(playerIdx, req.getFunction(), req.getModuleId());

        msg.setCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(SC_AddJoinWatchBarrage_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
