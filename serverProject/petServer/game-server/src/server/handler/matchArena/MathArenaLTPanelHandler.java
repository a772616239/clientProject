package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaLTPanel;
import protocol.MatchArena.SC_MathArenaStartMatch;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MatchArenaLTPanel_VALUE)
public class MathArenaLTPanelHandler extends AbstractBaseHandler<CS_MatchArenaLTPanel> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MathArenaStartMatch.Builder resultBuilder = SC_MathArenaStartMatch.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MatchArenaLTPanel_VALUE, resultBuilder);
    }

    @Override
    protected CS_MatchArenaLTPanel parse(byte[] bytes) throws Exception {
        return CS_MatchArenaLTPanel.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaLTPanel req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        MatchArenaLTManager.getInstance().getMainPanelInfo(playerIdx);
    }
}
