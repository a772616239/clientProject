package server.handler.battle;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle.CS_BattleWatchQuit;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_BattleWatchQuit_VALUE)
public class BattleWatchQuitHandler extends AbstractBaseHandler<CS_BattleWatchQuit> {
    @Override
    protected CS_BattleWatchQuit parse(byte[] bytes) throws Exception {
        return CS_BattleWatchQuit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BattleWatchQuit req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        BattleServerManager.getInstance().quitWatch(playerIdx, req.toByteString());
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
