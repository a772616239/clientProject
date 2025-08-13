package server.handler.crossarenatop;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaTopManager;
import model.player.util.PlayerUtil;
import protocol.*;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaTopPlayJion;
import protocol.CrossArena.SC_CrossArenaTopPlayJion;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaTopPlayJion_VALUE)
public class CrossArenaTopJionHandler extends AbstractBaseHandler<CS_CrossArenaTopPlayJion> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaTopPlayJion.Builder resultBuilder = SC_CrossArenaTopPlayJion.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaHonor_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaTopPlayJion parse(byte[] bytes) throws Exception {
        return CS_CrossArenaTopPlayJion.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaTopPlayJion req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (PlayerUtil.queryFunctionLock(playerIdx, Common.EnumFunction.PeakMelee)) {
            CrossArena.SC_CrossArenaTopPlayJion.Builder msg = CrossArena.SC_CrossArenaTopPlayJion.newBuilder();
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CrossArenaTopPlayJion_VALUE, msg);
            return;
        }
        CrossArenaTopManager.getInstance().jionTop(playerIdx, req);
    }
}
