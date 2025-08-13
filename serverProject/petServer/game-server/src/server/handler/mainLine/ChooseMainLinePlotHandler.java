package server.handler.mainLine;

import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import cfg.Plot;
import cfg.PlotObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import helper.StringUtils;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.pet.HelpPetManager;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MainLine;
import protocol.MainLine.CS_ChooseMainlinePlot;
import protocol.MainLine.SC_ChooseMainlinePlot;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_ChooseMainlinePlot_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ChooseMainlinePlot_VALUE)
public class ChooseMainLinePlotHandler extends AbstractBaseHandler<CS_ChooseMainlinePlot> {
    @Override
    protected CS_ChooseMainlinePlot parse(byte[] bytes) throws Exception {
        return CS_ChooseMainlinePlot.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChooseMainlinePlot req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ChooseMainlinePlot.Builder msg = SC_ChooseMainlinePlot.newBuilder();
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_ChooseMainlinePlot_VALUE, msg);
            return;
        }
        PlotObject cfg = Plot.getById(req.getPlotId());
        if (cfg == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_ChooseMainlinePlot_VALUE, msg);
            return;
        }
        if (req.getType() == 0) {
            settleMainlinePoint(req.getPlotId(), entity, cfg);
        } else if (req.getType() == 1) {
            settleEpPoint(req.getEpisodeId(), req.getPlotId(), entity);
        }

        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_ChooseMainlinePlot_VALUE, msg);
    }

    private void settleEpPoint(int episodeId, int plotId, mainlineEntity e) {
        SyncExecuteFunction.executeConsumer(e, entity -> {
            MainLine.EpisodeProgress episodeProgress = entity.getDBBuilder().getEpisodeProgressMap().getOrDefault(episodeId, MainLine.EpisodeProgress.newBuilder().setEpisodeId(episodeId).build());
            MainLine.EpisodeProgress.Builder newEpisode = episodeProgress.toBuilder().addPersonalPlot(plotId);
            entity.getDBBuilder().putEpisodeProgress(episodeId, newEpisode.build());
        });
    }

    private RetCodeEnum settleMainlinePoint(int plotId, mainlineEntity entity, PlotObject cfg) {
        int curCheckPoint = entity.getDBBuilder().getMainLinePro().getCurCheckPoint();

        if (entity.getDBBuilder().getClaimedMainLinePlotRewardList().contains(curCheckPoint)) {
            return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
        }


        MainLineNodeObject mainlineCfg = MainLineNode.getById(curCheckPoint);
        if (ArrayUtils.contains(mainlineCfg.getRewarplot(), plotId)) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDBBuilder().addClaimedMainLinePlotReward(plotId).addPersonalPlot(plotId);
        });

        doMainlinePlotReward(entity, cfg);

        addMainlineHelpPet(entity, cfg);

        return RetCodeEnum.RCE_Success;
    }

    private void addMainlineHelpPet(mainlineEntity entity, PlotObject cfg) {
        if (StringUtils.isNotBlank(cfg.getHelppetid())) {
            addHelpPet(entity.getLinkplayeridx(), cfg);
        }
    }

    private void doMainlinePlotReward(mainlineEntity entity, PlotObject cfg) {
        if (ArrayUtils.isEmpty(cfg.getRewardlist())) {
            return;
        }
        Common.Reward reward = RewardUtil.parseReward(cfg.getRewardlist());
        RewardManager.getInstance().doReward(entity.getLinkplayeridx(), reward, ReasonManager.getInstance()
                        .borrowReason(Common.RewardSourceEnum.RSE_MainLinePlot)
                , true);
    }

    private void addHelpPet(String playerIdx, PlotObject cfg) {
        PetMessage.Pet pet = HelpPetManager.getHelpPet(cfg.getHelppetid());
        if (pet != null) {
            petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerIdx);
            PetMessage.HelpPetBagItem helpPetBagItem = petEntity.getDbPetsBuilder().getHelpPetMap().get(EnumFunction.MainLine_VALUE);
            if (helpPetBagItem == null) {
                helpPetBagItem = PetMessage.HelpPetBagItem.newBuilder().setFunction(EnumFunction.MainLine).build();
            }
            petEntity.getDbPetsBuilder().putHelpPet(EnumFunction.MainLine_VALUE, helpPetBagItem.toBuilder().addPet(pet).build());
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NullFuntion;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_ChooseMainlinePlot_VALUE, SC_ChooseMainlinePlot.newBuilder().setRetCode(retCode));
    }
}
