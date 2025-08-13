package server.http.entity.report;

import common.GameConst;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/21
 */
@Getter
@Setter
public class UnreportedCommentQueryResult {
    private int totalSize;

    private List<UnreportedComment> pageInfo;

    private int pageSize = GameConst.UNREPORTED_COMMENT_QUERY_SIZE;

    public void addPageInfo(UnreportedComment unreportedComment) {
        if (unreportedComment == null) {
            return;
        }
        if (pageInfo == null) {
            pageInfo = new ArrayList<>();
        }
        pageInfo.add(unreportedComment);
    }
}
