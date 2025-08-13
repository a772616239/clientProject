package server.handler.crossarenatop;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaTopManager;
import model.player.util.PlayerUtil;
import protocol.*;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaTopPlay;
import protocol.CrossArena.SC_CrossArenaTopPlay;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaTopPlay_VALUE)
public class CrossArenaTopHandler extends AbstractBaseHandler<CS_CrossArenaTopPlay> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaTopPlay.Builder resultBuilder = SC_CrossArenaTopPlay.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaTopPlay_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaTopPlay parse(byte[] bytes) throws Exception {
        return CS_CrossArenaTopPlay.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaTopPlay req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.PeakMelee)) {
            CrossArena.SC_CrossArenaTopPlay.Builder msg = CrossArena.SC_CrossArenaTopPlay.newBuilder();
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlay_VALUE, msg);
            return;
        }
        CrossArenaTopManager.getInstance().getPanel(playerIdx);
    }
}
