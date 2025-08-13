package server.handler.comment;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.comment.dbCache.commentCache;
import model.comment.entity.commentEntity;
import protocol.Comment.CS_CommentReport;
import protocol.Comment.SC_CommentReport;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.ReportUtil;

/**
 * @author huhan
 * @date 2020/07/15
 */
@MsgId(msgId = MsgIdEnum.CS_CommentReport_VALUE)
public class CommentReportHandler extends AbstractBaseHandler<CS_CommentReport> {
    @Override
    protected CS_CommentReport parse(byte[] bytes) throws Exception {
        return CS_CommentReport.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CommentReport req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        commentEntity entity = commentCache.getInstance().getEntity(req.getCommentTypeValue(), req.getLinkId());
        SC_CommentReport.Builder resultBuilder = SC_CommentReport.newBuilder();
        if (entity == null || !ReportUtil.canReport(playerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Report_TimesLimit));
            gsChn.send(MsgIdEnum.SC_CommentReport_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            RetCodeEnum ret = entity.addReported(req.getCommentId(), playerIdx, req.getReportType(), req.getReportMsg());
            resultBuilder.setRetCode(GameUtil.buildRetCode(ret));
            gsChn.send(MsgIdEnum.SC_CommentReport_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CommentReport_VALUE, SC_CommentReport.newBuilder().setRetCode(retCode));
    }
}
