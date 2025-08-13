package server.handler.comment;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.comment.commentConstant;
import model.comment.dbCache.commentCache;
import model.comment.entity.commentEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Comment.CS_CommentData;
import protocol.Comment.SC_CommentData;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_CommentData_VALUE)
public class QueryCommentHandler extends AbstractBaseHandler<CS_CommentData> {
    @Override
    protected CS_CommentData parse(byte[] bytes) throws Exception {
        return CS_CommentData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CommentData req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_CommentData.Builder builder;
        if (!commentCache.getInstance().checkCommentTypeParam(req.getCommentType(), req.getLinkId())) {
            builder = SC_CommentData.newBuilder();
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Comment_ErrorParam));
            gsChn.send(MsgIdEnum.SC_CommentData_VALUE, builder);
            return;
        }
        String commentIdx = commentConstant.buildIdx(req.getCommentTypeValue(), req.getLinkId());
        commentEntity entity = commentCache.getByIdx(commentIdx);
        if (entity == null) {
            builder = SC_CommentData.newBuilder();
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            builder.setCommentType(req.getCommentType());
            builder.setLinkId(req.getLinkId());
            gsChn.send(MsgIdEnum.SC_CommentData_VALUE, builder);
            return;
        }
        builder = entity.getCommentInfo(playerIdx, req.getSortType(), req.getStartIndex(), req.getLength(), req.getNeedSelfComment());
        gsChn.send(MsgIdEnum.SC_CommentData_VALUE, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CommentData_VALUE, SC_CommentData.newBuilder().setRetCode(retCode));
    }
}
