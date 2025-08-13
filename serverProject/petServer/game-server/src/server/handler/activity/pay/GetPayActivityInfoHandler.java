package server.handler.activity.pay;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import protocol.Activity;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import util.GameUtil;


/**
 * @Description
 * @Author hanx
 * @Date2020/4/26 0026 20:02
 **/
@MsgId(msgId = MessageId.MsgIdEnum.CS_GetPayActivityInfo_VALUE)
public class GetPayActivityInfoHandler extends AbstractBaseHandler<Activity.CS_GetPayActivityInfo> {
    @Override
    protected Activity.CS_GetPayActivityInfo parse(byte[] bytes) throws Exception {
        return Activity.CS_GetPayActivityInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_GetPayActivityInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemCache.getInstance().sendRechargeActivityShow(playerIdx);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
