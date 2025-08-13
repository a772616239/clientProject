package server.handler.comment;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.comment.commentConstant;
import model.comment.dbCache.commentCache;
import model.comment.entity.commentEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Comment.CS_LikeComment;
import protocol.Comment.SC_LikeComment;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_LikeComment_VALUE)
public class LikeCommentHandler extends AbstractBaseHandler<CS_LikeComment> {
    @Override
    protected CS_LikeComment parse(byte[] bytes) throws Exception {
        return CS_LikeComment.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_LikeComment req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_LikeComment.Builder builder;
        String commentIdx = commentConstant.buildIdx(req.getCommentTypeValue(), req.getLinkId());
        commentEntity commentEntity = commentCache.getByIdx(commentIdx);
        if (commentEntity == null) {
            builder = SC_LikeComment.newBuilder();
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Comment_ErrorParam));
            gsChn.send(MsgIdEnum.SC_LikeComment_VALUE, builder);
            return;
        }
        builder = SyncExecuteFunction.executeFunction(commentEntity, entity -> entity.likeComment(req.getCommentId(), playerIdx));
        gsChn.send(MsgIdEnum.SC_LikeComment_VALUE, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_LikeComment_VALUE, SC_LikeComment.newBuilder().setRetCode(retCode));
    }
}
