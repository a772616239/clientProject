package server.handler.crossarena;

import common.AbstractBaseHandler;
import common.GlobalData;
import helper.StringUtils;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena;
import protocol.CrossArena.CS_CrossArenaTablePage;
import protocol.CrossArena.SC_CrossArenaTablePage;
import protocol.MessageId.MsgIdEnum;

import static protocol.MessageId.MsgIdEnum.SC_CrossArenaTablePage_VALUE;

/**
 * 擂台赛排行榜
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaTablePage_VALUE)
public class ClaimCrossArenaTablePageHandler extends AbstractBaseHandler<CS_CrossArenaTablePage> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaTablePage.Builder resultBuilder = SC_CrossArenaTablePage.newBuilder();
        gsChn.send(SC_CrossArenaTablePage_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaTablePage parse(byte[] bytes) throws Exception {
        return CS_CrossArenaTablePage.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaTablePage req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (StringUtils.isEmpty(playerIdx)){
            return;
        }
        CrossArenaManager.getInstance().sendCrossArenaTablePage(playerIdx,req.getPage());
    }
}
