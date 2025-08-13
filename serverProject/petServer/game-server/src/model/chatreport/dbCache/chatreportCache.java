/*CREATED BY TOOL*/

package model.chatreport.dbCache;

import annotation.annationInit;
import common.GameConst;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import io.netty.util.internal.ConcurrentSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.chatreport.cache.chatreportUpdateCache;
import model.chatreport.entity.chatreportEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import server.http.entity.report.Report;
import server.http.entity.report.ReportConst.ReportOperate;
import server.http.entity.report.ReportResult;
import util.ReportUtil;

@annationInit(value = "chatreportCache", methodname = "load")
public class chatreportCache extends baseCache<chatreportCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static chatreportCache instance = null;

    public static chatreportCache getInstance() {

        if (instance == null) {
            instance = new chatreportCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "chatreportDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("chatreportDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (chatreportCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(chatreportEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static chatreportEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (chatreportEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return chatreportUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        chatreportEntity t = (chatreportEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    public chatreportEntity getEntity(String msgIdx, String msgContent, String reportedPlayer) {
        chatreportEntity entity = getByIdx(msgIdx);
        if (entity == null) {
            entity = new chatreportEntity();
            entity.setIdx(msgIdx);
            entity.setContent(msgContent);
            entity.setLinkplayer(reportedPlayer);
            entity.putToCache();
        }
        return entity;
    }

    /**
     * 所有的被举报的聊天
     */
    private final Set<String> totalReported = new ConcurrentSet<>();
    private final Set<String> curQuerySet = new ConcurrentSet<>();

    public ReportResult queryReport(boolean firstQuery) {
        //第一次查询查询所有, 后续查询只添加待查询
        Set<String> result = addCurQueryListFull();
        if (firstQuery) {
            result = curQuerySet;
        }

        ReportResult reportResult = new ReportResult();
        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(e -> {
                Report report = Report.create(getByIdx(e));
                if (report != null) {
                    reportResult.addReport(report);
                }
                reportResult.setRemainSize(this.totalReported.size() - curQuerySet.size());
            });
        }
        return reportResult;
    }



    /**
     * 填充当前正在查询的集合到最大查询数量,并返回添加的对象
     *
     * @return
     */
    private Set<String> addCurQueryListFull() {
        if (totalReported.isEmpty()) {
            initTotalReported();
        }

        int needAddSize = GameConst.REPORT_QUERY_SIZE - curQuerySet.size();
        if (needAddSize <= 0) {
            return null;
        }

        Set<String> newAdd = this.totalReported.stream()
                .filter(e -> !curQuerySet.contains(e))
                .limit(needAddSize)
                .collect(Collectors.toSet());

        curQuerySet.addAll(newAdd);
        return newAdd;
    }

    private void initTotalReported() {
        Set<String> findResult = getAll().keySet().stream()
                .filter(e -> {
                    chatreportEntity entity = getByIdx(e);
                    if (entity == null) {
                        return false;
                    }
                    return !entity.getDbBuilder().getBaned()
                            && entity.getDbBuilder().getReportsCount() > 0;
                })
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(findResult)) {
            this.totalReported.addAll(findResult);
        }
    }

    public void addReported(String idx) {
        if (StringUtils.isBlank(idx)) {
            return;
        }
        this.totalReported.add(idx);
    }

    /**
     * @param idx
     */
    private void removeReported(String idx) {
        if (StringUtils.isBlank(idx)) {
            return;
        }
        this.curQuerySet.remove(idx);
        this.totalReported.remove(idx);
    }

    public boolean reportOperate(List<String> idxList, int operate) {
        if (CollectionUtils.isEmpty(idxList)) {
            return true;
        }


        if (operate == ReportOperate.RO_CLEAR_REPORT_RECORD) {
            clearReport(idxList);
        } else if (operate == ReportOperate.RO_SHIELD_CURRENT) {
            shieldById(idxList);
        } else if (operate == ReportOperate.RO_SHIELD_TOTAL_SOURCE_TYPE) {
            Set<String> playerIdxSet = new HashSet<>();
            idxList.forEach(e -> {
                String linkPlayerIdx = getLinkPlayerIdx(e);
                if (StringUtils.isNotBlank(linkPlayerIdx)) {
                    playerIdxSet.add(linkPlayerIdx);
                }
            });
            shieldByPlayerId(playerIdxSet);
        }
        return true;
    }

    /**
     * 清空举报
     * @param idxList
     */
    private void clearReport(List<String> idxList) {
        if (CollectionUtils.isEmpty(idxList)) {
            return;
        }

        Set<String> reported = new HashSet<>();
        Set<String> reporter = new HashSet<>();
        idxList.forEach(e -> {
            chatreportEntity entity = getByIdx(e);
            if (entity != null) {
                SyncExecuteFunction.executeConsumer(entity, en -> {
                    reporter.addAll(entity.getDbBuilder().getReportsMap().keySet());
                    entity.getDbBuilder().clearReports();
                });
                reported.add(entity.getLinkplayer());
            }
            removeReported(e);
        });

        //发送举报邮件
        ReportUtil.sendReportMailToPlayer(reported, reporter, false);
    }

    public void shieldById(List<String> idx) {
        if (CollectionUtils.isEmpty(idx)) {
            return;
        }

        Set<String> reported = new HashSet<>();
        Set<String> reporter = new HashSet<>();

        idx.forEach(e -> {
            chatreportEntity entity = getByIdx(e);
            if (entity != null) {
                SyncExecuteFunction.executeConsumer(entity, en -> en.getDbBuilder().setBaned(true));
                removeReported(entity.getIdx());
                reported.add(entity.getLinkplayer());
                reporter.addAll(entity.getDbBuilder().getReportsMap().keySet());
            }
        });

        //发送举报邮件
        ReportUtil.sendReportMailToPlayer(reported, reporter, true);
    }

    public void shieldByPlayerId(Set<String> playerIdxSet) {
        if (CollectionUtils.isEmpty(playerIdxSet)) {
            return;
        }

        //所有的举报玩家
        Set<String> reporter = new HashSet<>();

        getAll().values().forEach(e -> {
            if (e instanceof chatreportEntity) {
                chatreportEntity entity = (chatreportEntity) e;
                if (playerIdxSet.contains(entity.getLinkplayer())) {
                    SyncExecuteFunction.executeConsumer(entity, en -> en.getDbBuilder().setBaned(true));
                    removeReported(entity.getIdx());
                    reporter.addAll(entity.getDbBuilder().getReportsMap().keySet());
                }
            }
        });

        //发送举报邮件
        ReportUtil.sendReportMailToPlayer(playerIdxSet, reporter, true);
    }

    public String getLinkPlayerIdx(String idx) {
        if (StringUtils.isBlank(idx)) {
            return null;
        }
        chatreportEntity entity = getByIdx(idx);
        return entity == null ? null : entity.getLinkplayer();
    }
}
