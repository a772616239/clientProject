package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_UpdateMistTicket;
import protocol.MistForest.SC_UpdateMistTicket;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateMistTicket_VALUE)
public class UpdateMistTicketHandler extends AbstractBaseHandler<CS_UpdateMistTicket> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_UpdateMistTicket_VALUE, SC_UpdateMistTicket.newBuilder());
    }

    @Override
    protected CS_UpdateMistTicket parse(byte[] bytes) throws Exception {
        return CS_UpdateMistTicket.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateMistTicket req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        player.sendMistFreeTickets();
    }
}
