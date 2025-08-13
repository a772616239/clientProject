package server.handler.crossarena;

import common.AbstractBaseHandler;
import helper.StringUtils;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaQueuePanel;
import protocol.CrossArena.SC_CrossArenaQueuePanel;
import protocol.MessageId.MsgIdEnum;

import static protocol.MessageId.MsgIdEnum.SC_CrossArenaQueuePanel_VALUE;

/**
 * 擂台赛排行榜
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaQueuePanel_VALUE)
public class ClaimCrossArenaQueuePanelHandler extends AbstractBaseHandler<CS_CrossArenaQueuePanel> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaQueuePanel.Builder resultBuilder = SC_CrossArenaQueuePanel.newBuilder();
        gsChn.send(SC_CrossArenaQueuePanel_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaQueuePanel parse(byte[] bytes) throws Exception {
        return CS_CrossArenaQueuePanel.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaQueuePanel req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (StringUtils.isEmpty(playerIdx)){
            return;
        }
        CrossArenaManager.getInstance().sendCrossArenaQueuePanel(playerIdx,req.getPage());
    }
}
