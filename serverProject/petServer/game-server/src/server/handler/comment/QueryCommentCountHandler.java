package server.handler.comment;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.comment.commentConstant;
import model.comment.dbCache.commentCache;
import model.comment.entity.commentEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Comment.CS_CommentCount;
import protocol.Comment.SC_CommentCount;
import protocol.CommentDB.CommentDb;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_CommentCount_VALUE)
public class QueryCommentCountHandler extends AbstractBaseHandler<CS_CommentCount> {
    @Override
    protected CS_CommentCount parse(byte[] bytes) throws Exception {
        return CS_CommentCount.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CommentCount req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_CommentCount.Builder builder = SC_CommentCount.newBuilder();
        if (!commentCache.getInstance().checkCommentTypeParam(req.getCommentType(), req.getLinkId())) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Comment_ErrorParam));
            gsChn.send(MsgIdEnum.SC_CommentCount_VALUE, builder);
            return;
        }
        String commentId = commentConstant.buildIdx(req.getCommentTypeValue(), req.getLinkId());
        commentEntity commentEntity = commentCache.getByIdx(commentId);
        if (commentEntity != null) {
            CommentDb.Builder commentDb = commentEntity.getCommentDbData();
            if (builder != null) {
                builder.setCommentCount(commentDb.getDbDataCount());
            }
        }
        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_CommentCount_VALUE, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CommentCount_VALUE, SC_CommentCount.newBuilder().setRetCode(retCode));
    }
}
