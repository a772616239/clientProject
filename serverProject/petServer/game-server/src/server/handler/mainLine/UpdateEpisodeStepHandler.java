package server.handler.mainLine;

import cfg.MainLineEpisodeNodeConfig;
import cfg.MainLineEpisodeNodeConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import protocol.Common;
import protocol.MainLine;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;
import util.LogUtil;

import java.util.List;

@MsgId(msgId = MessageId.MsgIdEnum.CS_UpdateEpisodeStep_VALUE)
public class UpdateEpisodeStepHandler extends AbstractBaseHandler<MainLine.CS_UpdateEpisodeStep> {
    @Override
    protected MainLine.CS_UpdateEpisodeStep parse(byte[] bytes) throws Exception {
        return MainLine.CS_UpdateEpisodeStep.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, MainLine.CS_UpdateEpisodeStep req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        MainLine.SC_UpdateEpisodeStep.Builder resultBuilder = MainLine.SC_UpdateEpisodeStep.newBuilder();
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("UpdateEpisodeStepHandler, playerIdx[" + playerIdx + "] mainLineEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_UpdateEpisodeStep_VALUE, resultBuilder);
            return;
        }
        int episodeId = req.getEpisodeId();

        MainLine.EpisodeProgress episodeProgress = entity.getDBBuilder().getEpisodeProgressMap().get(episodeId);
        if (episodeProgress == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_UpdateEpisodeStep_VALUE, resultBuilder);
            return;
        }

        int curEpisodeNodeId = episodeProgress.getCurEpisodeId();

        MainLineEpisodeNodeConfigObject cfg = MainLineEpisodeNodeConfig.getById(curEpisodeNodeId);
        if (cfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_UpdateEpisodeStep_VALUE, resultBuilder);
            return;
        }
        int reqEpisodeNodeId = req.getCurEpisodeId();
        if (reqEpisodeNodeId != curEpisodeNodeId) {
            LogUtil.warn("player:{} update episodeNode:{} not match playerCurEpisode:{}", playerIdx, reqEpisodeNodeId, curEpisodeNodeId);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_UpdateEpisodeStep_VALUE, resultBuilder);
            return;
        }

        if (!checkStep(req.getCompleteProgress(), cfg, episodeProgress)) {
            LogUtil.warn("player:{} update episodeNode step fail,req:{},playerProgress:{}", playerIdx, req, episodeProgress);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_UpdateEpisodeStep_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> entity.addEpisodeProgress(req.getEpisodeId(), req.getCompleteProgress()));

        entity.sendEpisodeUpdate(episodeProgress.getEpisodeId());
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_UpdateEpisodeStep_VALUE, resultBuilder);
    }

    private boolean checkStep(MainLine.EpisodeProgressType type, MainLineEpisodeNodeConfigObject cfg, MainLine.EpisodeProgress episodeProgress) {
        if (episodeProgress.getEpisodeFinish()) {
            return false;
        }
        List<MainLine.EpisodeProgressType> progressList = episodeProgress.getCompleteProgressList();
        if (progressList.contains(type)) {
            return false;
        }
        switch (type) {
            /* 客戶端不管有沒有都會發*/
            case EPT_ChatBeforeFight:
                return true;
            case EPT_ChatAfterFight:
                return hasAfterPlot(cfg) && progressList.contains(MainLine.EpisodeProgressType.EPT_Fight);
            default:
                return false;
        }
    }

    private boolean hasAfterPlot(MainLineEpisodeNodeConfigObject cfg) {
        return cfg.getLaterplot().length > 0;
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.NullFuntion;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_UpdateEpisodeStep_VALUE, MainLine.SC_UpdateEpisodeStep.newBuilder().setRetCode(retCode));
    }
}
