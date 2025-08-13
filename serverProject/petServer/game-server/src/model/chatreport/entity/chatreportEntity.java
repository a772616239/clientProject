/**
 *created by tool DAOGenerate
 */
package model.chatreport.entity;

import cfg.ReportConfig;
import cfg.ServerStringRes;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.Ban;
import common.tick.GlobalTick;
import java.util.Collections;
import java.util.HashSet;
import lombok.Getter;
import lombok.Setter;
import model.chatreport.dbCache.chatreportCache;
import model.obj.BaseObj;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import platform.logs.LogService;
import platform.logs.entity.ReportAutoDealLog;
import protocol.ChatDB.DB_ChatReport;
import protocol.Comment.EnumReportType;
import protocol.CommentDB.DB_Reporter;
import protocol.RetCodeId.RetCodeEnum;
import server.http.entity.report.ReportConst.ReportQueryType;
import server.http.entity.report.ReportQueryTypeSource;
import util.EventUtil;
import util.LogUtil;
import util.ReportUtil;
import util.TimeUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
@Getter
@Setter
public class chatreportEntity extends BaseObj {

    @Override
	public String getClassType() {
		return "chatreportEntity";
	}

    @Override
    public void putToCache() {
        chatreportCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.info = this.getDbBuilder().build().toByteArray();
    }

    /**
     * 
     */
    private String idx;

    /**
     * 举报内容
     */
    private String content;

    /**
     * 内容所属玩家
     */
    private String linkplayer;

    /**
     * 举报信息
     */
    private byte[] info;

    @Override
	public String getBaseIdx() {
		// TODO Auto-generated method stub
		return idx;
	}

	private DB_ChatReport.Builder builder;

    public DB_ChatReport.Builder getDbBuilder() {
        if (builder == null) {
            if (getInfo() != null) {
                try {
                    builder = DB_ChatReport.parseFrom(getInfo()).toBuilder();
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.printStackTrace(e);
                    builder = DB_ChatReport.newBuilder();
                }
            } else {
                builder = DB_ChatReport.newBuilder();
            }
        }
        return builder;
    }

    public RetCodeEnum addReport(String playerIdx, EnumReportType reportType, String reportMsg) {
        if (StringUtils.isBlank(playerIdx) || reportType == null || reportType == EnumReportType.ERT_NULL) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        if (getDbBuilder().getReportsMap().containsKey(playerIdx)) {
            return RetCodeEnum.RCE_Report_Repeated;
        }

        if (getDbBuilder().getBaned()) {
            return RetCodeEnum.RCE_Success;
        }

        DB_Reporter reporter = ReportUtil.createReporter(playerIdx, reportType, reportMsg);
        getDbBuilder().putReports(reporter.getPlayerIdx(),reporter);

        if (ReportUtil.needBan(getDbBuilder().getReportsMap().values())) {
            autoDeal();
        } else {
            chatreportCache.getInstance().addReported(getIdx());
        }

        return RetCodeEnum.RCE_Success;
    }

    private void autoDeal() {
        ReportAutoDealLog autoDealReport = new ReportAutoDealLog();
        autoDealReport.setTypeSource(new ReportQueryTypeSource(ReportQueryType.RQT_CHAT, 0));
        autoDealReport.setPlayerIdx(getLinkplayer());
        autoDealReport.setPlayerName(PlayerUtil.queryPlayerName(autoDealReport.getPlayerIdx()));
        autoDealReport.addReportType(ReportUtil.getAutoDealTypeSet(getDbBuilder().getReportsMap().values()));
        autoDealReport.setContent(getContent());
        autoDealReport.setReportedTimes(getDbBuilder().getReportsCount());
        autoDealReport.setDealTime(GlobalTick.getInstance().getCurrentTime());
        autoDealReport.setBanType(Ban.CHAT);
        autoDealReport.setIdx(getIdx());

        //封禁结束时间
        long endTime = GlobalTick.getInstance().getCurrentTime()
                + ReportConfig.getById(GameConst.CONFIG_ID).getAutodealbandays() * TimeUtil.MS_IN_A_DAY;
        autoDealReport.setEndTime(endTime);

        LogService.getInstance().submit(new ReportAutoDealLog());

        //禁言
        int banMsgId = ReportConfig.getById(GameConst.CONFIG_ID).getBancommenttips();
        String banMsg = ServerStringRes.buildLanguageNumContentJson(banMsgId);
        EventUtil.ban(Collections.singletonList(getLinkplayer()), Ban.CHAT, endTime, banMsg);

        //屏蔽玩家的聊天
        chatreportCache.getInstance().shieldByPlayerId(new HashSet<>(Collections.singletonList(getLinkplayer())));

        //举报邮件
        ReportUtil.sendReportMailToPlayer(Collections.singletonList(getLinkplayer()), getDbBuilder().getReportsMap().keySet(), true);
    }
}