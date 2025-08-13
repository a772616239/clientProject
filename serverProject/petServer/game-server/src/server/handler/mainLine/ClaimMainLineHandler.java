package server.handler.mainLine;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import protocol.Common.EnumFunction;
import protocol.MainLine.CS_ClaimMainLine;
import protocol.MainLine.SC_ClaimMainLine;
import protocol.MainLineDB.DB_MainLine.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimMainLine_VALUE)
public class ClaimMainLineHandler extends AbstractBaseHandler<CS_ClaimMainLine> {
    @Override
    protected CS_ClaimMainLine parse(byte[] bytes) throws Exception {
        return CS_ClaimMainLine.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMainLine req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimMainLine.Builder resultBuilder = SC_ClaimMainLine.newBuilder();
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("ClaimMainLineHandler, playerIdx[" + playerIdx + "] mainLineEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimMainLine_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.checkPointUnlock();

            Builder db_data = entity.getDBBuilder();
            if (db_data == null) {
                LogUtil.error("ClaimMainLineHandler, playerIdx[" + playerIdx + "] dbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimMainLine_VALUE, resultBuilder);
                return;
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setMainLinePro(db_data.getMainLinePro());
            resultBuilder.setCurOnHookNode(db_data.getOnHookIncome().getCurOnHookNode());
            resultBuilder.setTodayQuickTimes(entity.getTodayQuickOnHookTimes());
            resultBuilder.setStartOnHookTime(db_data.getOnHookIncome().getStartOnHookTime());
            resultBuilder.addAllAlreadyGetRewardIndex(db_data.getAlreadyGetRewardIndexList());
            resultBuilder.addAllClaimedAdditionRewardsNodeId(db_data.getClaimedAdditionRewardsNodeIdList());
            resultBuilder.setFreeQuickTimes(db_data.getTodayFreeOnHookTime());
            entity.checkAndFixMainline();
            resultBuilder.addAllEpisodes(db_data.getEpisodeProgressMap().values());
            resultBuilder.addAllPersonalPlot(db_data.getPersonalPlotList());
            gsChn.send(MsgIdEnum.SC_ClaimMainLine_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NullFuntion;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimMainLine_VALUE, SC_ClaimMainLine.newBuilder().setRetCode(retCode));
    }
}
