package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaWinTask;
import protocol.MessageId.MsgIdEnum;

/**
 * 连胜奖励领取
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaWinTask_VALUE)
public class CrossArenaWinTaskHandler extends AbstractBaseHandler<CS_CrossArenaWinTask> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }

    @Override
    protected CS_CrossArenaWinTask parse(byte[] bytes) throws Exception {
        return CS_CrossArenaWinTask.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaWinTask req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaManager.getInstance().getWinTaskReward(playerIdx);
    }
}
