/**
 * created by tool DAOGenerate
 */
package model.comment.entity;

import cfg.ReportConfig;
import cfg.ServerStringRes;
import common.GameConst;
import common.GameConst.Ban;
import common.IdGenerator;
import common.tick.GlobalTick;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import model.comment.commentConstant;
import model.comment.dbCache.commentCache;
import model.obj.BaseObj;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.wordFilter.WordFilterManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import platform.logs.LogService;
import platform.logs.entity.ReportAutoDealLog;
import protocol.Comment.CommentContent;
import protocol.Comment.CommentTypeEnum;
import protocol.Comment.EnumReportType;
import protocol.Comment.SC_AddComment;
import protocol.Comment.SC_CommentData;
import protocol.Comment.SC_LikeComment;
import protocol.Comment.SortTypeEnum;
import protocol.CommentDB.CommentDb;
import protocol.CommentDB.CommentDbData;
import protocol.CommentDB.CommentDbData.Builder;
import protocol.CommentDB.DB_Reporter;
import protocol.RetCodeId.RetCodeEnum;
import server.http.entity.report.ReportConst.ReportQueryType;
import server.http.entity.report.ReportQueryTypeSource;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.ReportUtil;
import util.TimeUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class commentEntity extends BaseObj {

    public String getClassType() {
        return "commentEntity";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private int type;

    /**
     *
     */
    private int linkid;

    /**
     *
     */
    private byte[] comment;


    /**
     * 获得
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得
     */
    public int getType() {
        return type;
    }

    /**
     * 设置
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * 获得
     */
    public int getLinkid() {
        return linkid;
    }

    /**
     * 设置
     */
    public void setLinkid(int linkid) {
        this.linkid = linkid;
    }

    /**
     * 获得
     */
    public byte[] getComment() {
        return comment;
    }

    /**
     * 设置
     */
    public void setComment(byte[] comment) {
        this.comment = comment;
    }


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return idx;
    }

    /* ======================================================================= */

    public commentEntity() {

    }

    @Override
    public void putToCache() {
        commentCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.comment = getCommentDbData().build().toByteArray();
    }

    CommentDb.Builder commentData;

    public CommentDb.Builder getCommentDbData() {
        try {
            if (commentData == null) {
                if (this.comment != null) {
                    this.commentData = CommentDb.parseFrom(this.comment).toBuilder();
                } else {
                    this.commentData = CommentDb.newBuilder();
                }
            }
            return this.commentData;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return CommentDb.newBuilder();
        }
    }


    /**
     * <playerIdx,commentId>
     */
    Map<String, Long> playerCommentMap = new HashMap<>();

    /**
     * 时间排序List 时间越大越靠前
     */
    private final List<Long> timeSortedList = new LinkedList<>();
    /**
     * 点赞排序list
     */
    private final List<Long> likedSortedList = new ArrayList<>();

    public void clear() {
        timeSortedList.clear();
        likedSortedList.clear();
    }

    public void loadCommentData() {
        clear();
        CommentDb.Builder dbBuilder = getCommentDbData();
        if (dbBuilder.getDbDataCount() <= 0) {
            return;
        }

        Collection<CommentDbData> commentData = dbBuilder.getDbDataMap().values();

        //更新时间排序
        List<Long> timeSorted = commentData.stream()
                .sorted((e1, e2) -> -Long.compare(e1.getCommentTime(), e2.getCommentTime()))
                .map(CommentDbData::getCommentId)
                .collect(Collectors.toList());
        timeSortedList.addAll(timeSorted);

        //更新喜欢排序
        List<Long> likedSorted = commentData.stream()
                .sorted((e1, e2) -> -Integer.compare(e1.getLikedPlayerCount(), e2.getLikedPlayerCount()))
                .map(CommentDbData::getCommentId)
                .collect(Collectors.toList());
        likedSortedList.addAll(likedSorted);

        commentData.forEach(e -> playerCommentMap.put(e.getPlayerIdx(), e.getCommentId()));
    }

    /**
     * 调用此方法获取builder修改数据后请调用{@see commentEntity#putCommentDbData}方法使更改生效
     *
     * @param commentId
     * @return
     */
    public CommentDbData.Builder getCommentBuilderById(long commentId) {
        CommentDb.Builder dbData = getCommentDbData();
        if (dbData != null) {
            return dbData.getDbDataMap().get(commentId).toBuilder();
        } else {
            return null;
        }
    }

    /**
     * 获取评论的举报玩家Set
     * @param commentId
     * @return
     */
    public Set<String> getReporterSet(long commentId) {
        Builder builder = getCommentBuilderById(commentId);
        return builder == null ? Collections.emptySet() : builder.getReportsMap().keySet();
    }

    public void putCommentDbData(CommentDbData.Builder builder) {
        if (builder == null) {
            return;
        }
        CommentDbData build = builder.build();
        getCommentDbData().putDbData(builder.getCommentId(), builder.build());
        updateCommentQuickSearch(build);
    }

    /**
     * 检查评论数量, 大于最大允许的数量,删除最旧的一条
     */
    public void checkCommentSize() {
        if (getCommentDbData().getDbDataCount() > commentConstant.MaxCommentContSize) {
            Long removeId = this.timeSortedList.get(this.timeSortedList.size() - 1);
            if (removeId != null) {
                removeComment(removeId);
            }
        }
    }

    /**
     * 慎用次方法
     *
     * @param commentId
     */
    public void removeComment(long commentId) {
        String linkPlayerId = getCommentLinkPlayerId(commentId);
        getCommentDbData().removeDbData(commentId);
        this.timeSortedList.remove(commentId);
        this.likedSortedList.remove(commentId);
        if (linkPlayerId != null) {
            this.playerCommentMap.remove(linkPlayerId);
        }
    }

    public String getCommentLinkPlayerId(long commentId) {
        Builder builder = getCommentBuilderById(commentId);
        return builder == null ? null : builder.getPlayerIdx();
    }

    /**
     * 创建一个新的评论实体
     *
     * @param playerIdx
     * @param content
     * @return
     */
    public CommentDbData.Builder createCommentDbDataBuilder(String playerIdx, String content) {
        if (StringUtils.isBlank(playerIdx) || StringUtils.isBlank(content)) {
            return null;
        }
        Builder result = CommentDbData.newBuilder();
        result.setCommentId(IdGenerator.getInstance().generateIdNum());
        result.setPlayerIdx(playerIdx);
        result.setCommentTime(GlobalTick.getInstance().getCurrentTime());
        result.setContent(content);
        return result;
    }


    /**
     * 增删改评论数据后,调用此方法更新快速查询相关信息
     */
    public void updateCommentQuickSearch(CommentDbData build) {
        if (build == null) {
            return;
        }

        //更新时间排序
        if (!this.timeSortedList.contains(build.getCommentId())) {
            this.timeSortedList.add(0, build.getCommentId());
        }

        //添加到点赞排序
        this.likedSortedList.add(build.getCommentId());

        //更新player映射
        this.playerCommentMap.put(build.getPlayerIdx(), build.getCommentId());

        //添加到commentId-entityIdx 映射
        commentCache.getInstance().putToCommentIdEntityIdxMap(getIdx(), build.getCommentId());
    }

    public void addLikedPlayer(long commentId, String playerIdx) {
        if (StringHelper.isNull(playerIdx)) {
            return;
        }

        Builder builder = getCommentBuilderById(commentId);
        if (builder == null || builder.getLikedPlayerList().contains(playerIdx)) {
            return;
        }

        builder.addLikedPlayer(playerIdx);
        putCommentDbData(builder);
    }

    public void addLikedPlayerList(long commentId, List<String> playerList) {
        if (CollectionUtils.isEmpty(playerList)) {
            return;
        }
        Builder builder = getCommentBuilderById(commentId);
        if (builder == null) {
            return;
        }
        playerList.forEach(e -> {
            if (!builder.getLikedPlayerList().contains(playerList)) {
                builder.addLikedPlayer(e);
            }
        });
        putCommentDbData(builder);
    }

    /**
     * 判断玩家是否可以对指定评论进行点赞
     *
     * @param commentId
     * @param playerIdx
     * @return
     */
    public boolean playerCanLikeComment(long commentId, String playerIdx) {
        return !playerAlreadyLikedComment(commentId, playerIdx);
    }

    /**
     * 判断玩家是否已经对指定评论进行电站
     *
     * @param commentId
     * @param playerIdx
     * @return
     */
    public boolean playerAlreadyLikedComment(long commentId, String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return true;
        }

        Builder builder = getCommentBuilderById(commentId);
        if (builder == null) {
            return true;
        }
        return builder.getLikedPlayerList().contains(playerIdx);
    }

    /**
     * 获取指定玩家的评论内容
     *
     * @param playerIdx
     * @return
     */
    public String getPlayerContent(String playerIdx) {
        Builder builder = getCommentBuilderByPlayerId(playerIdx);
        return builder == null ? null : builder.getContent();
    }

    public CommentDbData.Builder getCommentBuilderByPlayerId(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }
        Long commentId = this.playerCommentMap.get(playerIdx);
        return commentId == null ? null : getCommentBuilderById(commentId);
    }

    /**
     * 获取范围内的评论
     *
     * @param playerIdx
     * @param sortType
     * @param startIndex
     * @param pageSize
     * @param needSelfContent
     * @return
     */
    public SC_CommentData.Builder getCommentInfo(String playerIdx, SortTypeEnum sortType, int startIndex, int pageSize, boolean needSelfContent) {
        SC_CommentData.Builder result = SC_CommentData.newBuilder();
        result.setCommentTypeValue(getType());
        result.setLinkId(getLinkid());
        result.setSortType(sortType);
        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        CommentDb.Builder commentDb = getCommentDbData();
        if (commentDb != null) {
            int realPageSize = Integer.min(pageSize, commentConstant.MaxQueryCommentCount);
            int endIndex = Integer.min(commentDb.getDbDataCount(), startIndex + realPageSize);

            long commentId;
            for (int i = startIndex; i < endIndex; ++i) {
                if (sortType == SortTypeEnum.STE_Time) {
                    commentId = timeSortedList.get(i);
                } else {
                    commentId = likedSortedList.get(i);
                }

                CommentContent.Builder commentContent = buildCommentContent(commentId, playerIdx);
                if (commentContent != null) {
                    result.addContentData(commentContent);
                }
            }
            result.setCommentCount(commentDb.getDbDataCount());
            if (needSelfContent) {
                String selfContent = getPlayerContent(playerIdx);
                if (selfContent != null) {
                    result.setSelfComment(selfContent);
                }
            }

        }
        return result;
    }

    /**
     * 玩家是否可以评论
     *
     * @param playerIdx
     * @return
     */
    public boolean playerCanComment(String playerIdx) {
        return getCommentBuilderByPlayerId(playerIdx) == null;
    }

    /**
     * 检查评论内容是否合法
     *
     * @param content
     * @return
     */
    public RetCodeEnum checkCommentContent(String content) {
        if (StringUtils.isBlank(content)) {
            return RetCodeEnum.RCE_Comment_EmptyContent;
        }

        if (content.length() > commentConstant.MaxCommentContSize) {
            return RetCodeEnum.RCE_Comment_ErrorLength;
        }
        return RetCodeEnum.RCE_Success;
    }

    public SC_AddComment.Builder addComment(String playerIdx, String content) {
        SC_AddComment.Builder result = SC_AddComment.newBuilder();

        String afterCheckContent = WordFilterManager.getInstance().filterSensitiveWords(content);
        RetCodeEnum checkRet = checkCommentContent(afterCheckContent);
        if (checkRet != RetCodeEnum.RCE_Success) {
            result.setRetCode(GameUtil.buildRetCode(checkRet));
            return result;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            return result;
        }

        if (!playerCanComment(playerIdx)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Comment_AlreadyComment));
            return result;
        }

        Builder newComment = createCommentDbDataBuilder(playerIdx, afterCheckContent);
        if (newComment == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            return result;
        }

        putCommentDbData(newComment);
        checkCommentSize();

        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        result.setCommentCount(getCommentDbData().getDbDataCount());

        CommentContent.Builder contentBuilder = CommentContent.newBuilder();
        contentBuilder.setCommentId(newComment.getCommentId());
        contentBuilder.setPlayerInfo(player.getBattleBaseData());
        contentBuilder.setContent(newComment.getContent());
        contentBuilder.setCommentTime(newComment.getCommentTime());
        result.setComment(contentBuilder);
        return result;
    }

    /**
     * 构建发送给客户端的评论内容实体
     *
     * @param commentId
     * @param playerIdx
     * @return
     */
    public CommentContent.Builder buildCommentContent(long commentId, String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }

        Builder commentBuilder = getCommentBuilderById(commentId);
        if (commentBuilder == null) {
            return null;
        }

        playerEntity player = playerCache.getByIdx(commentBuilder.getPlayerIdx());
        if (player == null) {
            return null;
        }

        CommentContent.Builder contentBuilder = CommentContent.newBuilder();
        contentBuilder.setCommentId(commentBuilder.getCommentId());
        if (!commentBuilder.getBaned()) {
            contentBuilder.setContent(commentBuilder.getContent());
        } else {
            contentBuilder.setContent("****");
        }
        contentBuilder.setCommentTime(commentBuilder.getCommentTime());
        contentBuilder.setPlayerInfo(player.getBattleBaseData());
        contentBuilder.setLikeCount(commentBuilder.getLikedPlayerCount());

        if (playerAlreadyLikedComment(commentId, playerIdx)) {
            contentBuilder.setLiked(true);
        }
        return contentBuilder;
    }

    public SC_LikeComment.Builder likeComment(long commentId, String playerIdx) {
        SC_LikeComment.Builder builder = SC_LikeComment.newBuilder();

        Builder commentBuilder = getCommentBuilderById(commentId);
        if (commentBuilder == null || commentBuilder.getBaned()) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Comment_NotFoundComment));
            return builder;
        }

        if (commentBuilder.getLikedPlayerList().contains(playerIdx)) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Comment_AlreadyLiked));
            return builder;
        }

        commentBuilder.addLikedPlayer(playerIdx);
        addLikedPlayer(commentId, playerIdx);

        if (!commentCache.getInstance().inUpdateMap(getIdx())) {
            commentCache.getInstance().addUpdateComment(this);
        }

        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        builder.setLikedCount(commentBuilder.getLikedPlayerCount());
        return builder;
    }

    public void updateSortedLikedList() {
        List<Long> likedSorted = getCommentDbData().getDbDataMap().values().stream()
                .sorted((e1, e2) -> -Integer.compare(e1.getLikedPlayerCount(), e2.getLikedPlayerCount()))
                .map(CommentDbData::getCommentId)
                .collect(Collectors.toList());
        likedSortedList.clear();
        likedSortedList.addAll(likedSorted);
    }

    public RetCodeEnum addReported(long commentId, String playerIdx, EnumReportType reportType, String reportMsg) {
        CommentDbData.Builder builder = getCommentBuilderById(commentId);
        if (builder == null || StringUtils.isBlank(playerIdx) || reportType == null
                || reportType == EnumReportType.ERT_NULL || Objects.equals(playerIdx, builder.getPlayerIdx())) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (builder.getReportsMap().containsKey(playerIdx)) {
            return RetCodeEnum.RCE_Report_Repeated;
        }

        if (builder.getBaned()) {
            return RetCodeEnum.RCE_Success;
        }

        DB_Reporter reporter = ReportUtil.createReporter(playerIdx, reportType, reportMsg);
        builder.putReports(reporter.getPlayerIdx(), reporter);

        //判断是否达到自动封禁的
        if (ReportUtil.needBan(builder.getReportsMap().values())) {
            autoDeal(builder);
        } else {
            //添加举报记录
            commentCache.getInstance().addNewReport(getType(), builder.getCommentId());
        }

        putCommentDbData(builder);

        EventUtil.addReportTimes(playerIdx);

        return RetCodeEnum.RCE_Success;
    }

    /**
     * 自动处理
     *
     * @param builder
     */
    public void autoDeal(CommentDbData.Builder builder) {
        if (builder == null) {
            return;
        }

        ReportAutoDealLog autoDealReport = new ReportAutoDealLog();
        autoDealReport.setTypeSource(new ReportQueryTypeSource(ReportQueryType.RQT_COMMENT, getType()));
        autoDealReport.setPlayerIdx(builder.getPlayerIdx());
        autoDealReport.setPlayerName(PlayerUtil.queryPlayerName(autoDealReport.getPlayerIdx()));
        autoDealReport.addReportType(ReportUtil.getAutoDealTypeSet(builder.getReportsMap().values()));
        autoDealReport.setContent(builder.getContent());
        autoDealReport.setReportedTimes(builder.getReportsCount());
        autoDealReport.setDealTime(GlobalTick.getInstance().getCurrentTime());
        autoDealReport.setBanType(Ban.COMMENT);
        autoDealReport.setIdx(String.valueOf(builder.getCommentId()));

        //封禁结束时间
        long endTime = GlobalTick.getInstance().getCurrentTime()
                + ReportConfig.getById(GameConst.CONFIG_ID).getAutodealbandays() * TimeUtil.MS_IN_A_DAY;
        autoDealReport.setEndTime(endTime);

        LogService.getInstance().submit(autoDealReport);

        //禁止评论
        int banMsgId = ReportConfig.getById(GameConst.CONFIG_ID).getBancommenttips();
        String banMsg = ServerStringRes.buildLanguageNumContentJson(banMsgId);
        EventUtil.ban(Collections.singletonList(builder.getPlayerIdx()), Ban.COMMENT, endTime, banMsg);

        //屏蔽同一模块下该玩家所有评论
        EventUtil.shieldComment(Collections.singletonList(CommentTypeEnum.forNumber(getType())), builder.getPlayerIdx());

        //举报邮件
        ReportUtil.sendReportMailToPlayer(Collections.singletonList(builder.getPlayerIdx()), builder.getReportsMap().keySet(), true);
    }

    /**
     * 返回屏蔽的commentId所有举报的玩家
     *
     * @param playerIdx
     * @return
     */
    public Set<String> shieldPlayerComment(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }

        Builder commentBuilder = getCommentBuilderByPlayerId(playerIdx);
        if (commentBuilder == null || commentBuilder.getBaned()) {
            return null;
        }

        commentBuilder.setBaned(true);
        putCommentDbData(commentBuilder);
        //从举报列表中删除
        commentCache.getInstance().removeReportedComment(getType(),
                Collections.singletonList(commentBuilder.getCommentId()));

        return commentBuilder.getReportsMap().keySet();
    }

    public void shieldComment(long commentId) {
        Builder builder = getCommentBuilderById(commentId);
        if (builder != null) {
            builder.setBaned(true);
            putCommentDbData(builder);
        }

        commentCache.getInstance().removeReportedComment(getType(), Collections.singletonList(commentId));
    }

    public void unshieldedPlayerComment(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }

        Builder commentBuilder = getCommentBuilderByPlayerId(playerIdx);
        if (commentBuilder != null) {
            //解除屏蔽并清除举报信息
            commentBuilder.setBaned(false);
            commentBuilder.clearReports();
            putCommentDbData(commentBuilder);
        }
    }

    /**
     * 查询所有未被举报的评论
     *
     * @return
     */
    public List<Long> getUnreportedCommentId() {
        return getCommentDbData().getDbDataMap().values().stream()
                .filter(e -> e.getReportsCount() <= 0 && !e.getBaned())
                .map(CommentDbData::getCommentId)
                .collect(Collectors.toList());
    }

    /**
     * 清除所有屏蔽
     */
    public void clearTotalBan() {
        List<Long> totalId = new ArrayList<>(getCommentDbData().getDbDataMap().keySet());
        totalId.forEach(e -> {
            Builder builder = getCommentBuilderById(e);
            if (builder != null) {
                builder.setBaned(false);
                builder.clearReports();
                putCommentDbData(builder);
            }
        });

        commentCache.getInstance().removeReportedComment(getType(), totalId);
    }

    /**
     * 清除评论的举报记录
     *
     * @param commentId
     */
    public void clearCommentReportRecord(long commentId) {
        Builder builder = getCommentBuilderById(commentId);
        builder.setBaned(false);
        builder.clearReports();
        putCommentDbData(builder);

        commentCache.getInstance().removeReportedComment(getType(), Collections.singletonList(commentId));
    }

    /**
     * 查询指定玩家中未被举报的评论
     * @param playerIdxList
     * @return
     */
    public List<Long> getPlayerUnReportedCommentId(List<String> playerIdxList) {
        List<Long> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(playerIdxList)) {
            return result;
        }

        for (String idx : playerIdxList) {
            Builder builder = getCommentBuilderByPlayerId(idx);
            if (builder != null && !builder.getBaned()) {
                result.add(builder.getCommentId());
            }
        }
        return result;
    }

    public Collection<Long> getAllCommentId() {
        return getCommentDbData().getDbDataMap().keySet();
    }
}