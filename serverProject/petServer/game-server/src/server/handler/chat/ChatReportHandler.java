package server.handler.chat;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.chatreport.dbCache.chatreportCache;
import model.chatreport.entity.chatreportEntity;
import protocol.Chat.CS_ChatReport;
import protocol.Chat.SC_ChatReport;
import protocol.Chat.SC_ChatReport.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.ReportUtil;

/**
 * @author huhan
 * @date 2020/07/15
 */
@MsgId(msgId = MsgIdEnum.CS_ChatReport_VALUE)
public class ChatReportHandler extends AbstractBaseHandler<CS_ChatReport> {
    @Override
    protected CS_ChatReport parse(byte[] bytes) throws Exception {
        return CS_ChatReport.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChatReport req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        chatreportEntity entity = chatreportCache.getInstance().getEntity(req.getMsgId(), req.getContent(), req.getTargetPlayerIdx());
        Builder resultBuilder = SC_ChatReport.newBuilder();
        if(entity == null || !ReportUtil.canReport(playerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Report_TimesLimit));
            gsChn.send(MsgIdEnum.SC_ChatReport_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            RetCodeEnum ret = entity.addReport(playerIdx, req.getReportType(), req.getReportMsg());
            resultBuilder.setRetCode(GameUtil.buildRetCode(ret));
            gsChn.send(MsgIdEnum.SC_ChatReport_VALUE, resultBuilder);
        });

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.WordChat;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ChatReport_VALUE, SC_ChatReport.newBuilder().setRetCode(retCode));
    }
}
