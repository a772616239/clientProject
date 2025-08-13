package model.comment.entity;

import common.GameConst;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import model.comment.dbCache.commentCache;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author huhan
 * @date 2020.7.17
 * 此类用于保存同一来源当前所有的举报和当前正在处理的举报
 */
@Getter
public class CommentTypeReport {
    private final Set<Long> totalReport = new HashSet<>();
    private final Set<Long> curQuerySet = new HashSet<>();

    public synchronized void addNewReport(long commentId) {
        this.totalReport.add(commentId);
    }

    public synchronized void removeReport(List<Long> commentIdList) {
        if (CollectionUtils.isEmpty(commentIdList)) {
            return;
        }
        this.totalReport.removeAll(commentIdList);
        this.curQuerySet.removeAll(commentIdList);
    }

    public synchronized Set<Long> addQueryListToFull() {
        int needAddSize = GameConst.REPORT_QUERY_SIZE - curQuerySet.size();
        if (needAddSize <= 0) {
            return null;
        }
        Set<Long> findResult = totalReport.stream()
                .filter(e -> {
                    if (curQuerySet.contains(e)) {
                        return false;
                    }
                    commentEntity entity = commentCache.getInstance().getEntityByCommentId(e);
                    if (entity == null) {
                        return false;
                    }
                    return true;
                })
                .limit(needAddSize)
                .collect(Collectors.toSet());

        curQuerySet.addAll(findResult);
        return findResult;
    }

    /**
     * 返回未在查询队里中的数量
     * @return
     */
    public int getBesideQuerySetSize() {
        return totalReport.size() - curQuerySet.size();
    }

}
