package server.handler.gloryRoad.quiz;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.gloryroad.GloryRoadManager;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.CS_GloryRoadQuizSupportPlayer;
import protocol.GloryRoad.SC_GloryRoadQuizSupportPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/17
 */
@MsgId(msgId = MsgIdEnum.CS_GloryRoadQuizSupportPlayer_VALUE)
public class GloryRoadQuizSupportPlayerHandler extends AbstractBaseHandler<CS_GloryRoadQuizSupportPlayer> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_GloryRoad;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_GloryRoadQuizSupportPlayer.Builder resultBuilder = SC_GloryRoadQuizSupportPlayer.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_GloryRoadQuizSupportPlayer_VALUE, resultBuilder);
    }

    @Override
    protected CS_GloryRoadQuizSupportPlayer parse(byte[] bytes) throws Exception {
        return CS_GloryRoadQuizSupportPlayer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GloryRoadQuizSupportPlayer req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        RetCodeEnum retCode = GloryRoadManager.getInstance().supportPlayer(playerIdx, req.getSupportedPlayer());

        SC_GloryRoadQuizSupportPlayer.Builder resultBuilder = SC_GloryRoadQuizSupportPlayer.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_GloryRoadQuizSupportPlayer_VALUE, resultBuilder);
    }
}
