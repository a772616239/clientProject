package server.handler.mainLine;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import protocol.Common;
import protocol.MainLine;
import protocol.MainLineDB;
import protocol.MessageId;
import protocol.PrepareWar;
import protocol.RetCodeId;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_EnterEpisode_VALUE)
public class EnterEpisodeHandler extends AbstractBaseHandler<MainLine.CS_EnterEpisode> {
    @Override
    protected MainLine.CS_EnterEpisode parse(byte[] bytes) throws Exception {
        return MainLine.CS_EnterEpisode.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, MainLine.CS_EnterEpisode req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        MainLine.SC_EnterEpisode.Builder resultBuilder = MainLine.SC_EnterEpisode.newBuilder();
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("EnterEpisodeHandler, playerIdx[" + playerIdx + "] mainLineEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_EnterEpisode_VALUE, resultBuilder);
            return;
        }

        if (req.getEpisodeId() == entity.getDBBuilder().getPlayerCurEpisode()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
            gsChn.send(MessageId.MsgIdEnum.SC_EnterEpisode_VALUE, resultBuilder);
            return;
        }

        MainLine.EpisodeProgress episodeProgress = entity.getDBBuilder().getEpisodeProgressMap().get(req.getEpisodeId());
        if (episodeProgress == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_EnterEpisode_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            MainLineDB.DB_MainLine.Builder dbBuilder = entity.getDBBuilder();
            dbBuilder.setPlayerCurEpisode(req.getEpisodeId());
            if (episodeProgress.getNew()) {
                LogUtil.info("player:{} enter new episode,episodeId:{}", playerIdx, req.getEpisodeId());
                dbBuilder.putEpisodeProgress(episodeProgress.getEpisodeId()
                        , episodeProgress.toBuilder().setNew(false).build());
                entity.sendEpisodeUpdate(episodeProgress.getEpisodeId());
            }
        });
        clearEpisodeTeam(playerIdx);
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_EnterEpisode_VALUE, resultBuilder);
    }

    private void clearEpisodeTeam(String playerIdx) {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(teamEntity, t -> {
            Team dbTeam = teamEntity.getDBTeam(PrepareWar.TeamNumEnum.TNE_Episode_1);
            if (dbTeam == null) {
                return;
            }
            teamEntity.clearTeam(PrepareWar.TeamNumEnum.TNE_Episode_1, true);
        });

    }

    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.MainLine;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_EnterEpisode_VALUE, MainLine.SC_EnterEpisode.newBuilder().setRetCode(retCode));
    }
}
