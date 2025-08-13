package server.handler.teams;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.team.dbCache.teamCache;
import model.team.entity.TeamsDB;
import model.team.entity.teamEntity;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.CS_BuyTeam;
import protocol.PrepareWar.SC_BuyTeam;
import protocol.PrepareWar.SC_BuyTeam.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_BuyTeam_VALUE)
public class BuyTeamHandler extends AbstractBaseHandler<CS_BuyTeam> {
    @Override
    protected CS_BuyTeam parse(byte[] bytes) throws Exception {
        return CS_BuyTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyTeam req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] util is null");
            return;
        }

        Builder resultBuilder = SC_BuyTeam.newBuilder();
        int[] buyTeamCost = GameConfig.getById(GameConst.CONFIG_ID).getBuyteamcost();
        int buyTeamCount = SyncExecuteFunction.executeFunction(entity, e -> {
            TeamsDB teamsInfo = entity.getDB_Builder();
            //判断是否购买已达上限
            if (teamsInfo.getBuyTeamCount() >= buyTeamCost.length) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_PrepareWar_CanNotBuyMore));
                gsChn.send(MsgIdEnum.SC_BuyTeam_VALUE, resultBuilder);
                return -1;
            }

            return teamsInfo.getBuyTeamCount();
        });

        if (buyTeamCount < 0) {
            return;
        }

        Consume consume = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Diamond_VALUE, 0, buyTeamCost[buyTeamCount]);
        if (ConsumeManager.getInstance().consumeMaterial(playerIdx, consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_BuyTeam))) {

            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.unlockOneTeam(true);
                entity.sendTeamsInfo();
            });

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_BuyTeam_VALUE, resultBuilder);
        } else {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_DiamondNotEnought));
            gsChn.send(MsgIdEnum.SC_BuyTeam_VALUE, resultBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Teams;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyTeam_VALUE, SC_BuyTeam.newBuilder().setRetCode(retCode));
    }
}
