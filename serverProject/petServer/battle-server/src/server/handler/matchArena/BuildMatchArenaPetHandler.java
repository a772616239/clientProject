package server.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matchArena.MatchArenaNormalManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_BuildMatchArenaPet;


@MsgId(msgId = MsgIdEnum.GS_BS_BuildMatchArenaPet_VALUE)
public class BuildMatchArenaPetHandler extends AbstractHandler<GS_BS_BuildMatchArenaPet> {
    @Override
    protected GS_BS_BuildMatchArenaPet parse(byte[] bytes) throws Exception {
        return GS_BS_BuildMatchArenaPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_BuildMatchArenaPet req, int i) {
        MatchArenaNormalManager.getInstance().initSuccessMatchPlayerPets(req);
    }
}
