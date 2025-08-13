package server.handler.gloryRoad.quiz;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.gloryroad.GloryRoadManager;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.CS_GloryRoadQuizComment;
import protocol.GloryRoad.SC_GloryRoadQuizComment;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/3/17
 */
@MsgId(msgId = MsgIdEnum.CS_GloryRoadQuizComment_VALUE)
public class GloryRoadQuizCommentHandler extends AbstractBaseHandler<CS_GloryRoadQuizComment> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_GloryRoad;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_GloryRoadQuizComment.Builder resultBuilder = SC_GloryRoadQuizComment.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_GloryRoadAddComment_VALUE, resultBuilder);
    }

    @Override
    protected CS_GloryRoadQuizComment parse(byte[] bytes) throws Exception {
        return CS_GloryRoadQuizComment.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GloryRoadQuizComment req, int i) {
        RetCodeEnum retCode = GloryRoadManager.getInstance().addComment(String.valueOf(gsChn.getPlayerId1()), req.getContent());

        SC_GloryRoadQuizComment.Builder resultBuilder = SC_GloryRoadQuizComment.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_GloryRoadQuizComment_VALUE, resultBuilder);
    }
}
