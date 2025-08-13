package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_MistForestPlayerInfo;
import protocol.MistForest.SC_MistForestPlayerInfo;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MistForestPlayerInfo_VALUE)
public class MistPlayerInfoHandler extends AbstractBaseHandler<CS_MistForestPlayerInfo> {
    @Override
    protected CS_MistForestPlayerInfo parse(byte[] bytes) throws Exception {
        return CS_MistForestPlayerInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MistForestPlayerInfo req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_MistForestPlayerInfo_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_MistForestPlayerInfo_VALUE, SC_MistForestPlayerInfo.newBuilder());
    }
}
