package server.http.entity.report;

import lombok.Getter;
import lombok.Setter;
import model.player.util.PlayerUtil;
import protocol.CommentDB.CommentDbData;

/**
 * @author huhan
 * @date 2020/07/21
 */
@Getter
@Setter
public class UnreportedComment {
    private String id;

    private String playerIdx;

    private String playerName;

    private String content;

    private int querySource;

    /**
     *评价时间
     */
    private long commentTime;

    public static UnreportedComment create(CommentDbData.Builder comment, int querySource) {
        if (comment == null) {
            return null;
        }
        UnreportedComment unreportedComment = new UnreportedComment();
        unreportedComment.setId(String.valueOf(comment.getCommentId()));
        unreportedComment.setPlayerIdx(comment.getPlayerIdx());
        unreportedComment.setPlayerName(PlayerUtil.queryPlayerName(unreportedComment.getPlayerIdx()));
        unreportedComment.setContent(comment.getContent());
        unreportedComment.setQuerySource(querySource);
        unreportedComment.setCommentTime(comment.getCommentTime());
        return unreportedComment;
    }
}
