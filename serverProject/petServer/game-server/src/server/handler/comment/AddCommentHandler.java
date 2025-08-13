package server.handler.comment;

import cfg.FunctionOpenLvConfig;
import cfg.ReportConfig;
import cfg.ServerStringRes;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GameConst.Ban;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.comment.commentConstant;
import model.comment.dbCache.commentCache;
import model.comment.entity.commentEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import protocol.Comment.CS_AddComment;
import protocol.Comment.SC_AddComment;
import protocol.Common.EnumFunction;
import protocol.Common.SC_Tips;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_AddComment_VALUE)
public class AddCommentHandler extends AbstractBaseHandler<CS_AddComment> {
    @Override
    protected CS_AddComment parse(byte[] bytes) throws Exception {
        return CS_AddComment.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_AddComment req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_AddComment.Builder builder;
        if (player.isBaned(Ban.COMMENT)) {
            builder = SC_AddComment.newBuilder();
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Ban_Comment));
            gsChn.send(MsgIdEnum.SC_AddComment_VALUE, builder);

            //tips
            int tips = ReportConfig.getById(GameConst.CONFIG_ID).getBancommenttips();
            String content = ServerStringRes.getContentByLanguage(tips, player.getLanguage());
            if (StringUtils.isNotBlank(content)) {
                gsChn.send(MsgIdEnum.SC_AddComment_VALUE, SC_Tips.newBuilder().setMsg(content));
            }
            return;
        }


        if (PlayerUtil.queryFunctionLock(playerIdx,EnumFunction.Comment)) {
            builder = SC_AddComment.newBuilder();
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionNotUnLock));
            gsChn.send(MsgIdEnum.SC_AddComment_VALUE, builder);
            return;
        }
        if (!commentCache.getInstance().checkCommentTypeParam(req.getCommentType(), req.getLinkId())) {
            builder = SC_AddComment.newBuilder();
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Comment_ErrorParam));
            gsChn.send(MsgIdEnum.SC_AddComment_VALUE, builder);
            return;
        }
        String commentIdx = commentConstant.buildIdx(req.getCommentTypeValue(), req.getLinkId());
        commentEntity commentEntity = commentCache.getByIdx(commentIdx);
        if (commentEntity == null) {
            commentEntity = new commentEntity();
            commentEntity.setIdx(commentIdx);
            commentEntity.setType(req.getCommentTypeValue());
            commentEntity.setLinkid(req.getLinkId());
            commentEntity.putToCache();
        }
        builder = SyncExecuteFunction.executeFunction(commentEntity, entity -> entity.addComment(playerIdx, req.getCommentContent()));
        gsChn.send(MsgIdEnum.SC_AddComment_VALUE, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Comment;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_AddComment_VALUE, SC_AddComment.newBuilder().setRetCode(retCode));
    }
}
