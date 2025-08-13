package server.handler.battle;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle.CS_LoadFinished;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_LoadFinished_VALUE)
public class BattleLoadFinishHandler extends AbstractBaseHandler<CS_LoadFinished> {
    @Override
    protected CS_LoadFinished parse(byte[] bytes) throws Exception {
        return CS_LoadFinished.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_LoadFinished req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        BattleServerManager.getInstance().transferMsgToBattleServer(
                playerIdx, MsgIdEnum.CS_LoadFinished_VALUE, req.toByteString(), false);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        doAction(gsChn, codeNum);
    }
}
