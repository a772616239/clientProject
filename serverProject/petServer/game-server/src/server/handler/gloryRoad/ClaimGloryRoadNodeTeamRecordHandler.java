package server.handler.gloryRoad;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.gloryroad.GloryRoadManager;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.CS_ClaimGloryRoadNodeTeamRecord;
import protocol.GloryRoad.SC_ClaimGloryRoadNodeTeamRecord;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/4/4
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimGloryRoadNodeTeamRecord_VALUE)
public class ClaimGloryRoadNodeTeamRecordHandler extends AbstractBaseHandler<CS_ClaimGloryRoadNodeTeamRecord> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_GloryRoad;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClaimGloryRoadNodeTeamRecord.Builder resultBuilder = SC_ClaimGloryRoadNodeTeamRecord.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_ClaimGloryRoadNodeTeamRecord_VALUE, resultBuilder);
    }

    @Override
    protected CS_ClaimGloryRoadNodeTeamRecord parse(byte[] bytes) throws Exception {
        return CS_ClaimGloryRoadNodeTeamRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimGloryRoadNodeTeamRecord req, int i) {
        GloryRoadManager.getInstance().sendIndexRecordTeamInfoMsg(String.valueOf(gsChn.getPlayerId1()), req.getNodeIndex());
    }
}
