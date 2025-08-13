package server.handler.redpoint;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.redpoint.RedPointManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RedPoint.CS_RedPointClientControl;

@MsgId(msgId = MsgIdEnum.CS_RedPointClientControl_VALUE)
public class RedPointClientControlHandler  extends AbstractBaseHandler<CS_RedPointClientControl> {
    @Override
    protected CS_RedPointClientControl parse(byte[] bytes) throws Exception {
        return CS_RedPointClientControl.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_RedPointClientControl req, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        RedPointManager.getInstance().change2ClientControl(playerId, req.getRedPointIdList());
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
