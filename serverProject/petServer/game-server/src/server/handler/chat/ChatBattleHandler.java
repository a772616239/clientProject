package server.handler.chat;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle.BattleTypeEnum;
import protocol.Chat.CS_BattleChat;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_BattleChat_VALUE)
public class ChatBattleHandler extends AbstractBaseHandler<CS_BattleChat> {
    @Override
    protected CS_BattleChat parse(byte[] bytes) throws Exception {
        return CS_BattleChat.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BattleChat req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        if (BattleManager.getInstance().getBattleType(playerIdx) != BattleTypeEnum.BTE_PVP) {
            LogUtil.error("player battle chat failed,not in pvp battle,id=" + playerIdx);
            return;
        }

        BattleServerManager.getInstance().transferMsgToBattleServer(
                playerIdx, MsgIdEnum.CS_BattleChat_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.WordChat;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}
