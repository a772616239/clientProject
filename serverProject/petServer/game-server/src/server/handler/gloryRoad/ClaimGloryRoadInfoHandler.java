package server.handler.gloryRoad;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.gloryroad.GloryRoadManager;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.CS_ClaimGloryRoadInfo;
import protocol.GloryRoad.SC_ClaimGloryRoadInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/18
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimGloryRoadInfo_VALUE)
public class ClaimGloryRoadInfoHandler extends AbstractBaseHandler<CS_ClaimGloryRoadInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_GloryRoad;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClaimGloryRoadInfo.Builder resultBuilder = SC_ClaimGloryRoadInfo.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_ClaimGloryRoadInfo_VALUE, resultBuilder);
    }

    @Override
    protected CS_ClaimGloryRoadInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimGloryRoadInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimGloryRoadInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        GloryRoadManager.getInstance().sendGloryRoadInfoMsg(playerIdx);
    }
}
