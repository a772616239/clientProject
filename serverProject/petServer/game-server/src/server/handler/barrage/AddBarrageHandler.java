package server.handler.barrage;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.barrage.BarrageManager;
import org.apache.commons.lang.StringUtils;
import protocol.Barrage;
import protocol.Common;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_AddBarrage_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_ClaimBarrageInfo_VALUE;

/**
 * 玩家发送弹幕
 */
@MsgId(msgId = MessageId.MsgIdEnum.CS_AddBarrage_VALUE)
public class AddBarrageHandler extends AbstractBaseHandler<Barrage.CS_AddBarrage> {
    @Override
    protected Barrage.CS_AddBarrage parse(byte[] bytes) throws Exception {
        return Barrage.CS_AddBarrage.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Barrage.CS_AddBarrage req, int i) {

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Barrage.SC_AddBarrage.Builder msg = Barrage.SC_AddBarrage.newBuilder();

        if (StringUtils.isBlank(req.getMsg())) {
            msg.setCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
            gsChn.send(SC_AddBarrage_VALUE, msg);
        }

        RetCodeId.RetCodeEnum retCodeEnum = BarrageManager.getInstance().playerAddMessage(playerIdx, req.getFunction(),
                req.getModuleId(), req.getMsg());

        msg.setCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(SC_AddBarrage_VALUE, msg);
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
