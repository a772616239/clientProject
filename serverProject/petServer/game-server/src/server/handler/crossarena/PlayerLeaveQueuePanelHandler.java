package server.handler.crossarena;

import common.AbstractBaseHandler;
import helper.StringUtils;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena;
import protocol.CrossArena.CS_PlayerLeaveQueuePanel;
import protocol.CrossArena.SC_PlayerLeaveQueuePanel;
import protocol.MessageId.MsgIdEnum;

import static protocol.MessageId.MsgIdEnum.SC_PlayerLeaveQueuePanel_VALUE;

/**
 * 擂台赛排行榜
 */
@MsgId(msgId = MsgIdEnum.CS_PlayerLeaveQueuePanel_VALUE)
public class PlayerLeaveQueuePanelHandler extends AbstractBaseHandler<CS_PlayerLeaveQueuePanel> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_PlayerLeaveQueuePanel.Builder resultBuilder = CrossArena.SC_PlayerLeaveQueuePanel.newBuilder();
        gsChn.send(SC_PlayerLeaveQueuePanel_VALUE, resultBuilder);
    }

    @Override
    protected CS_PlayerLeaveQueuePanel parse(byte[] bytes) throws Exception {
        return CS_PlayerLeaveQueuePanel.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PlayerLeaveQueuePanel req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (StringUtils.isEmpty(playerIdx)){
            return;
        }
        CrossArenaManager.getInstance().playerLeaveQueuePanel(playerIdx);
    }
}
