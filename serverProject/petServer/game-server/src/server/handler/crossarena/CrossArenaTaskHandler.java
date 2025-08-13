package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_CrossArenaTaskMission;
import protocol.TargetSystem.SC_CrossArenaTaskMission;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaTaskMission_VALUE)
public class CrossArenaTaskHandler extends AbstractBaseHandler<CS_CrossArenaTaskMission> {

    @Override
    protected CS_CrossArenaTaskMission parse(byte[] bytes) throws Exception {
        return CS_CrossArenaTaskMission.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaTaskMission req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_CrossArenaTaskMission.Builder resultBuilder = SC_CrossArenaTaskMission.newBuilder();
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_CrossArenaTaskMission_VALUE, resultBuilder);
            return;
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.addAllMission(entity.getDb_Builder().getCrossArenaInfoMap().values());
        gsChn.send(MsgIdEnum.SC_CrossArenaTaskMission_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.GrowthTrack;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CrossArenaTaskMission_VALUE, SC_CrossArenaTaskMission.newBuilder().setRetCode(retCode));
    }
}
