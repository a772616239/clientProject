/*CREATED BY TOOL*/

package model.comment.dbCache;

import annotation.annationInit;
import cfg.BossTowerConfig;
import cfg.BossTowerConfigObject;
import cfg.BraveChallengePoint;
import cfg.EndlessSpireConfig;
import cfg.MainLineNode;
import cfg.PetBaseProperties;
import common.GameConst;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import common.tick.Tickable;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.comment.cache.commentUpdateCache;
import model.comment.commentConstant;
import model.comment.entity.CommentTypeReport;
import model.comment.entity.commentEntity;
import model.player.dbCache.playerCache;
import model.player.util.PlayerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Comment.CommentTypeEnum;
import protocol.CommentDB.CommentDbData;
import protocol.CommentDB.CommentDbData.Builder;
import server.http.entity.report.Report;
import server.http.entity.report.ReportConst.ReportOperate;
import server.http.entity.report.ReportConst.ReportQueryType;
import server.http.entity.report.ReportQueryTypeSource;
import server.http.entity.report.ReportResult;
import server.http.entity.report.UnreportedComment;
import server.http.entity.report.UnreportedCommentQuery;
import server.http.entity.report.UnreportedCommentQueryResult;
import util.GameUtil;
import util.LogUtil;
import util.ReportUtil;

@annationInit(value = "commentCache", methodname = "load")
public class commentCache extends baseCache<commentCache> implements IbaseCache, Tickable {

    /******************* MUST HAVE ********************************/

    private static commentCache instance = null;

    public static commentCache getInstance() {

        if (instance == null) {
            instance = new commentCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "commentDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("commentDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (commentCache) o;
        }
        super.loadAllFromDb();

        for (BaseEntity baseEntity : getAll().values()) {
            if (baseEntity instanceof commentEntity) {
                commentEntity entity = (commentEntity) baseEntity;
                entity.loadCommentData();
            }
        }
    }

    public static void put(commentEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static commentEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (commentEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return commentUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        commentEntity t = (commentEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    /**
     * 下次更新喜欢时间
     */
    private long lastUpdateLikeTime;
    //需要更新点赞的实体
    private Map<String, commentEntity> updateLikeCommentMap;

    public commentEntity getEntity(int type, int linkId) {
        if (!checkCommentTypeParam(type, linkId)) {
            return null;
        }

        String idx = commentConstant.buildIdx(type, linkId);
        commentEntity entity = getByIdx(idx);
        if (entity == null) {
            entity = new commentEntity();
            entity.setIdx(idx);
            entity.setType(type);
            entity.setLinkid(linkId);
            entity.putToCache();
        }
        return entity;
    }

    public boolean inUpdateMap(String commentIdx) {
        if (!StringHelper.isNull(commentIdx)) {
            return false;
        }
        return updateLikeCommentMap != null && updateLikeCommentMap.containsKey(commentIdx);
    }

    public void addUpdateComment(commentEntity entity) {
        if (updateLikeCommentMap == null) {
            updateLikeCommentMap = new ConcurrentHashMap<>();
        }
        updateLikeCommentMap.put(entity.getIdx(), entity);
    }

    public boolean checkCommentTypeParam(CommentTypeEnum commentTypeEnum, int linkId) {
        if (commentTypeEnum == null) {
            return false;
        }
        return checkCommentTypeParam(commentTypeEnum.getNumber(), linkId);
    }

    public boolean checkCommentTypeParam(int commentTypeEnum, int linkId) {
        switch (commentTypeEnum) {
            case CommentTypeEnum.CTE_Pet_VALUE:
                return PetBaseProperties.getByPetid(linkId) != null;
            case CommentTypeEnum.CTE_MainLine_VALUE:
                return MainLineNode.getById(linkId) != null;
            case CommentTypeEnum.CTE_EndlessSpire_VALUE:
                return EndlessSpireConfig.getBySpirelv(linkId) != null;
            case CommentTypeEnum.CTE_BraveChallenge_VALUE:
                return BraveChallengePoint.getById(linkId) != null;
            case CommentTypeEnum.CTE_BossTower_VALUE:
                return BossTowerConfig.fightMakeExist(linkId);
            default:
                return false;
        }
    }

    @Override
    public void onTick() {
        if (updateLikeCommentMap == null || updateLikeCommentMap.isEmpty()) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (lastUpdateLikeTime + commentConstant.UpdateCommentInterval < curTime) {
            lastUpdateLikeTime = curTime;

            Iterator<Map.Entry<String, commentEntity>> iter = updateLikeCommentMap.entrySet().iterator();
            while (iter.hasNext()) {
                commentEntity entity = iter.next().getValue();
                SyncExecuteFunction.executeConsumer(entity, commentEntity::updateSortedLikedList);
                iter.remove();
            }
        }
    }


    /**
     * 被举报的评论Id  CommentTypeEnum.CTE_Null 特殊处理为全部类型
     */
    private final Map<CommentTypeEnum, CommentTypeReport> reportTypeMap = new ConcurrentHashMap<>();

    /**
     * <CommentId,CommentEntityIdx>
     */
    private final Map<Long, String> commentIdEntityIdxMap = new ConcurrentHashMap<>();

    /**
     * 初始化被举报且未被处理的评论
     */
    public void initReportComment() {
        for (BaseEntity value : getAll().values()) {
            if (!(value instanceof commentEntity)) {
                continue;
            }
            commentEntity entity = (commentEntity) value;
            for (CommentDbData data : entity.getCommentDbData().getDbDataMap().values()) {
                putToCommentIdEntityIdxMap(entity.getIdx(), data.getCommentId());
                if (data.getReportsCount() > 0 && !data.getBaned()) {
                    addNewReport(entity.getType(), data.getCommentId());
                }
            }
        }
    }

    public void putToCommentIdEntityIdxMap(String entityId, long commentId) {
        if (StringUtils.isBlank(entityId)) {
            return;
        }
        this.commentIdEntityIdxMap.put(commentId, entityId);
    }

    /**
     * 屏蔽模块下所有的玩家评论
     *
     * @param commentTypeList
     * @param playerIdx
     */
    public Set<String>  shieldCommentByType(List<CommentTypeEnum> commentTypeList, String playerIdx) {
        if (CollectionUtils.isEmpty(commentTypeList)) {
            return null;
        }
        return shieldCommentByValue(playerIdx, commentTypeList.stream().map(CommentTypeEnum::getNumber).collect(Collectors.toSet()));
    }

    /**
     * 返回屏蔽的评论的所有举报玩家
     * @param playerIdx
     * @param commentTypeValue
     * @return
     */
    public Set<String> shieldCommentByValue(String playerIdx, Set<Integer> commentTypeValue) {
        if (CollectionUtils.isEmpty(commentTypeValue) || PlayerUtil.playerNotExist(playerIdx)) {
            return null;
        }

        LogUtil.info("commentCache.shieldCommentByValue, shield comment, playerIdx:" + playerIdx + ",type:"
                + GameUtil.collectionToString(commentTypeValue));

        Set<String> result = new HashSet<>();
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof commentEntity)) {
                continue;
            }

            commentEntity entity = (commentEntity) value;
            if (!commentTypeValue.contains(entity.getType())) {
                continue;
            }
            SyncExecuteFunction.executeConsumer(entity, e -> {
                Set<String> reporter = entity.shieldPlayerComment(playerIdx);
                if (CollectionUtils.isNotEmpty(reporter)) {
                    result.addAll(reporter);
                }
            });
        }
        return result;
    }


    /**
     * 查询被举报但是未被处理的评论
     *
     * @param sourceType //类型查询
     * @param firstQuery 是否是第一次查询
     * @return
     */
    public ReportResult queryReportComment(int sourceType, boolean firstQuery) {
        return queryReportComment(CommentTypeEnum.forNumber(sourceType), firstQuery);
    }

    public ReportResult queryReportComment(CommentTypeEnum sourceType, boolean firstQuery) {
        if (sourceType == null) {
            return null;
        }

        ReportResult result = new ReportResult();
        CommentTypeReport typeReport = reportTypeMap.get(sourceType);
        if (typeReport != null) {
            //第一次查询补充并查询所有, 后续查询只需要查询新增
            Set<Long> resultIdSet = typeReport.addQueryListToFull();
            if (firstQuery) {
                resultIdSet = typeReport.getCurQuerySet();
            }

            if (CollectionUtils.isNotEmpty(resultIdSet)) {
                resultIdSet.forEach(e -> {
                    Report report = buildReport(e);
                    if (report != null) {
                        result.addReport(report);
                    }
                });
            }

            result.setRemainSize(typeReport.getBesideQuerySetSize());
        }
        return result;
    }

    private int getCommentType(long commentId) {
        commentEntity entity = getEntityByCommentId(commentId);
        return entity == null ? 0 : entity.getType();
    }

    private Report buildReport(long commentId) {
        CommentDbData.Builder comment = getComment(commentId);
        if (comment == null) {
            return null;
        }
        Report report = Report.create(comment);
        if (report != null) {
            report.setQueryTypeSource(new ReportQueryTypeSource(ReportQueryType.RQT_COMMENT, getCommentType(commentId)));
        }
        return report;
    }

    public commentEntity getEntityByCommentId(long commentId) {
        String entityId = this.commentIdEntityIdxMap.get(commentId);
        commentEntity entity = getByIdx(entityId);
        if (entity == null) {
            LogUtil.error("model.comment.dbCache.commentCache.getEntityByCommentId, can not find entity, commentId:" + commentId);
        }
        return entity;
    }

    private CommentDbData.Builder getComment(long commentId) {
        commentEntity entity = getEntityByCommentId(commentId);
        if (entity == null) {
            return null;
        }
        return entity.getCommentBuilderById(commentId);
    }

    public void addNewReport(int type, long commentId) {
        addNewReport(CommentTypeEnum.forNumber(type), commentId);
    }

    public void addNewReport(CommentTypeEnum type, long commentId) {
        if (type == null) {
            return;
        }
        //添加到所有类型列表
        reportTypeMap.computeIfAbsent(CommentTypeEnum.CTE_Null, e -> new CommentTypeReport()).addNewReport(commentId);
        //添加到指定类型列表
        reportTypeMap.computeIfAbsent(type, e -> new CommentTypeReport()).addNewReport(commentId);
    }

    public boolean reportOperate(List<String> commentIdxList, int operate) {
        if (CollectionUtils.isEmpty(commentIdxList)) {
            return false;
        }

        Set<Long> commentId = commentIdxList.stream().map(e -> GameUtil.stringToLong(e, 0)).collect(Collectors.toSet());

        if (operate == ReportOperate.RO_CLEAR_REPORT_RECORD) {
            clearReport(commentId);
        } else if (operate == ReportOperate.RO_SHIELD_CURRENT) {
            shieldById(commentId);
        } else if (operate == ReportOperate.RO_SHIELD_TOTAL_SOURCE_TYPE) {
            //需要屏蔽的玩家类型mp
            Map<String, Set<Integer>> playerTypeMap = new HashMap<>();
            commentId.forEach(e -> {
                commentEntity entity = getEntityByCommentId(e);
                if (entity == null) {
                    return;
                }

                Builder commentBuilderById = entity.getCommentBuilderById(e);
                if (commentBuilderById != null) {
                    playerTypeMap.computeIfAbsent(commentBuilderById.getPlayerIdx(), en -> new HashSet<>())
                            .add(entity.getType());
                }
            });

            Set<String> totalReporter = new HashSet<>();
            playerTypeMap.forEach((k, v) -> {
                Set<String> reporter = shieldCommentByValue(k, v);
                if (CollectionUtils.isNotEmpty(reporter)) {
                    totalReporter.addAll(reporter);
                }
            });

            //发送举报邮件
            ReportUtil.sendReportMailToPlayer(playerTypeMap.keySet(), totalReporter, true);
        }
        return true;
    }

    private void shieldById(Collection<Long> commentIdList) {
        if (CollectionUtils.isEmpty(commentIdList)) {
            return;
        }
        Set<String> reported = new HashSet<>();
        Set<String> reporter = new HashSet<>();

        commentIdList.forEach(e -> {
            commentEntity entity = getEntityByCommentId(e);
            if (entity != null) {
                SyncExecuteFunction.executeConsumer(entity, en -> en.shieldComment(e));
            }

            Builder comment = getComment(e);
            if (comment != null) {
                reported.add(comment.getPlayerIdx());
                reporter.addAll(comment.getReportsMap().keySet());
            }
        });

        //发送举报邮件
        ReportUtil.sendReportMailToPlayer(reported, reporter, true);
    }

    private void clearReport(Collection<Long> commentIdColl) {
        if (CollectionUtils.isEmpty(commentIdColl)) {
            return;
        }

        Set<String> reported = new HashSet<>();
        Set<String> reporter = new HashSet<>();
        commentIdColl.forEach(e -> {
            commentEntity entity = getEntityByCommentId(e);
            if (entity != null) {
                SyncExecuteFunction.executeConsumer(entity, en -> {
                    Set<String> reporterSet = entity.getReporterSet(e);
                    if (CollectionUtils.isNotEmpty(reporterSet)) {
                        reporter.addAll(reporterSet);
                    }

                    String linkPlayerId = en.getCommentLinkPlayerId(e);
                    if (StringUtils.isNotBlank(linkPlayerId)) {
                        reported.add(linkPlayerId);
                    }

                    en.clearCommentReportRecord(e);
                });
            }
        });

        //举报邮件
        ReportUtil.sendReportMailToPlayer(reported, reporter, false);
    }

    /**
     * 将评论移除举报列表(举报已被处理)
     *
     * @param commentIdList
     */
    public void removeReportedComment(CommentTypeEnum type, List<Long> commentIdList) {
        if (CollectionUtils.isEmpty(commentIdList)) {
            return;
        }

        //从所有类型中移除
        CommentTypeReport totalType = reportTypeMap.get(CommentTypeEnum.CTE_Null);
        if (totalType != null) {
            totalType.removeReport(commentIdList);
        }

        CommentTypeReport typeReport = reportTypeMap.get(type);
        if (typeReport != null) {
            typeReport.removeReport(commentIdList);
        }
    }

    public void removeReportedComment(int type, List<Long> commentIdList) {
        removeReportedComment(CommentTypeEnum.forNumber(type), commentIdList);
    }

    public void unshielded(String playerIdx, int commentSource) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }

        for (BaseEntity value : getAll().values()) {
            if (!(value instanceof commentEntity)) {
                continue;
            }

            commentEntity entity = (commentEntity) value;
            if (entity.getType() != commentSource) {
                continue;
            }
            SyncExecuteFunction.executeConsumer(entity, e -> entity.unshieldedPlayerComment(playerIdx));
        }
    }

    /**
     * =========================================未被举报查询(评论监控)    start==========================================
     */

    /**
     * 当前查询的条件
     */
    private UnreportedCommentQuery curUnreportedQueryCondition;
    /**
     * 当前查询的目标列表
     */
    private final List<Long> curUnreportedQueryList = Collections.synchronizedList(new ArrayList<>());

    public UnreportedCommentQueryResult queryUnreportedComment(UnreportedCommentQuery unreportedComment) {
        if (unreportedComment == null) {
            return null;
        }

        List<Long> pageList = getUnreportedPageList(unreportedComment);
        if (CollectionUtils.isEmpty(pageList)) {
            return null;
        }
        List<UnreportedComment> commentList = pageList.stream()
                .map(this::getComment)
                .filter(e -> e != null && !e.getBaned() && e.getReportsCount() <= 0)
                .map(e -> UnreportedComment.create(e, getCommentType(e.getCommentId())))
                .collect(Collectors.toList());

        UnreportedCommentQueryResult result = new UnreportedCommentQueryResult();
        result.setPageInfo(commentList);
        result.setTotalSize(curUnreportedQueryList.size());
        return result;
    }

    private List<Long> getUnreportedPageList(UnreportedCommentQuery unreportedComment) {
        if (unreportedComment == null) {
            return null;
        }

        //不存在或者查询页为1重新初始化列表
        if (!unreportedComment.equals(this.curUnreportedQueryCondition) || unreportedComment.getPage() == 1) {
            initUnreportedList(unreportedComment);
        }

        if (curUnreportedQueryList.isEmpty()) {
            return null;
        }

        int page = unreportedComment.getPage();
        int totalSize = curUnreportedQueryList.size();
        int startIndex = (page - 1) * GameConst.UNREPORTED_COMMENT_QUERY_SIZE;
        if (totalSize <= startIndex) {
            return null;
        }
        int endIndex = Math.min(page * GameConst.UNREPORTED_COMMENT_QUERY_SIZE, curUnreportedQueryList.size());
        return curUnreportedQueryList.subList(startIndex, endIndex);
    }

    private void initUnreportedList(UnreportedCommentQuery unreportedComment) {
        List<commentEntity> findEntity = findTargetUnreportedEntity(unreportedComment);
        if (CollectionUtils.isEmpty(findEntity)) {
            return;
        }

        Optional<List<Long>> findOption = Optional.empty();
        //查询全部
        if (StringUtils.isBlank(unreportedComment.getName())) {
            findOption = findEntity.stream()
                    .map(commentEntity::getUnreportedCommentId)
                    .reduce((l1, l2) -> {
                        l1.addAll(l2);
                        return l1;
                    });
        } else {
            List<String> nameLikeList = playerCache.getInstance().getPlayerIdxByNameLike(unreportedComment.getName());
            if (CollectionUtils.isNotEmpty(nameLikeList)) {
                findOption = findEntity.stream()
                        .map(e -> e.getPlayerUnReportedCommentId(nameLikeList))
                        .reduce((l1, l2) -> {
                            l1.addAll(l2);
                            return l1;
                        });
            }
        }

        //更新当前查询条件
        curUnreportedQueryCondition = unreportedComment;
        //更新查询列表
        curUnreportedQueryList.clear();
        findOption.ifPresent(e -> {
            //对时间排序
            e.sort((e1, e2) -> -Long.compare(getCommentAddTime(e1), getCommentAddTime(e2)));
            curUnreportedQueryList.addAll(e);
        });
    }

    /**
     * 获取评论的添加时间
     *
     * @param commentId
     * @return
     */
    private long getCommentAddTime(long commentId) {
        Builder comment = getComment(commentId);
        return comment == null ? 0 : comment.getCommentTime();
    }

    /**
     * 宠物(宠物稀有度)
     * 主线(节点)
     * 无尽尖塔(塔层)
     * 勇气试炼(关卡)
     * boss塔(塔层)
     *
     * @param unreportedComment
     * @return
     */
    private List<commentEntity> findTargetUnreportedEntity(UnreportedCommentQuery unreportedComment) {
        if (unreportedComment == null) {
            return null;
        }

        Set<String> findIdx = new HashSet<>();
        if (unreportedComment.getQuerySource() == CommentTypeEnum.CTE_Pet_VALUE) {
            List<Integer> findPetIdList = new ArrayList<>();
            for (int i = unreportedComment.getStartRange(); i <= unreportedComment.getEndRange(); i++) {
                List<Integer> rarityPetId = PetBaseProperties.getRarityTotalPetId(i);
                if (CollectionUtils.isNotEmpty(rarityPetId)) {
                    findPetIdList.addAll(rarityPetId);
                }
            }

            findPetIdList.forEach(e -> {
                String idx = commentConstant.buildIdx(CommentTypeEnum.CTE_Pet_VALUE, e);
                if (StringUtils.isNotBlank(idx)) {
                    findIdx.add(idx);
                }
            });
        } else if (unreportedComment.getQuerySource() == CommentTypeEnum.CTE_EndlessSpire_VALUE) {
            for (int i = unreportedComment.getStartRange(); i < unreportedComment.getEndRange(); i++) {
                String idx = commentConstant.buildIdx(CommentTypeEnum.CTE_EndlessSpire_VALUE, i);
                if (StringUtils.isNotBlank(idx)) {
                    findIdx.add(idx);
                }
            }
        } else if (unreportedComment.getQuerySource() == CommentTypeEnum.CTE_MainLine_VALUE) {
            for (int i = unreportedComment.getStartRange(); i < unreportedComment.getEndRange(); i++) {
                String idx = commentConstant.buildIdx(CommentTypeEnum.CTE_MainLine_VALUE, i);
                if (StringUtils.isNotBlank(idx)) {
                    findIdx.add(idx);
                }
            }
        } else if (unreportedComment.getQuerySource() == CommentTypeEnum.CTE_BraveChallenge_VALUE) {

        } else if (unreportedComment.getQuerySource() == CommentTypeEnum.CTE_BossTower_VALUE) {
            for (int i = unreportedComment.getStartRange(); i < unreportedComment.getEndRange(); i++) {
                BossTowerConfigObject bossTowerCfg = BossTowerConfig.getById(i);
                if (bossTowerCfg != null) {
                    String idx_1 = commentConstant.buildIdx(CommentTypeEnum.CTE_BossTower_VALUE, bossTowerCfg.getFightmakeid());
                    String idx_2 = commentConstant.buildIdx(CommentTypeEnum.CTE_BossTower_VALUE, bossTowerCfg.getDifficultfightmakeid());
                    String idx_3 = commentConstant.buildIdx(CommentTypeEnum.CTE_BossTower_VALUE, bossTowerCfg.getUnbeatablefightmakeid());
                    if (StringUtils.isNotBlank(idx_1)) {
                        findIdx.add(idx_1);
                    }
                    if (StringUtils.isNotBlank(idx_2)) {
                        findIdx.add(idx_2);
                    }
                    if (StringUtils.isNotBlank(idx_3)) {
                        findIdx.add(idx_3);
                    }
                }
            }
        }

        if (findIdx.isEmpty()) {
            return null;
        }

        List<commentEntity> result = new ArrayList<>();
        for (String idx : findIdx) {
            commentEntity entity = getByIdx(idx);
            if (entity != null) {
                result.add(entity);
            }
        }

        return result;
    }

    /**
     * =========================================未被举报查询(评论监控)  end==========================================
     */

}

