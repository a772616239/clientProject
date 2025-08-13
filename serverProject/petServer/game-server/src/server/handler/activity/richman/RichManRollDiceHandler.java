package server.handler.activity.richman;

import cfg.GameConfig;
import cfg.GameConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.lang.math.RandomUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_RichManRollDice;
import protocol.Activity.SC_RichManRollDice;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;


/**
 * 大富翁掷骰子
 */
@MsgId(msgId = MsgIdEnum.CS_RichManRollDice_VALUE)
public class RichManRollDiceHandler extends AbstractBaseHandler<CS_RichManRollDice> {

    //随机骰子
    private static final int random = 1;
    //自选骰子
    private static final int optional = 2;

    @Override
    protected CS_RichManRollDice parse(byte[] bytes) throws Exception {
        return CS_RichManRollDice.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RichManRollDice req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_RichManRollDice.Builder resultMsg = SC_RichManRollDice.newBuilder();
        int type = req.getType();
        if (illegalParams(req, activityCfg, entity, type)) {
            resultMsg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_RichManRollDice_VALUE, resultMsg);
            return;
        }

        if (!consumeMaterial(playerIdx, type)) {
            resultMsg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_RichManRollDice_VALUE, resultMsg);
            return;
        }

        LogUtil.info("player:{} play rich man req:{}", playerIdx, req);

        int forwardPoint = getPlayerMovePoint(req);
        SyncExecuteFunction.executeConsumer(entity, e -> settlePlayerRollDice(activityCfg, entity, resultMsg, forwardPoint));
        sendClientMsg(gsChn, resultMsg);
        entity.sendRichManInfoUpdate();
    }

    private void settlePlayerRollDice(ServerActivity activityCfg, targetsystemEntity entity, SC_RichManRollDice.Builder resultMsg, int forwardPoint) {
        beforePlayerForward(entity,activityCfg);

        playerForward(entity, forwardPoint, resultMsg, activityCfg);

    }

    private void beforePlayerForward(targetsystemEntity entity, ServerActivity activityCfg) {
        int curPoint = entity.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().getCurPoint();
        Server.ServerRichManPoint richManPointCfg = activityCfg.getRichManPointMap().get(curPoint);
        if (richManPointCfg==null){
            return;
        }
        if (Activity.RichManPointType.RMPT_Store == richManPointCfg.getPointType()) {
            entity.clearActivityBuyMissionPro(activityCfg.getActivityId(), true);
        }
    }


    private boolean illegalParams(CS_RichManRollDice req, ServerActivity activityCfg, targetsystemEntity entity, int type) {
        return entity == null
                || !ActivityUtil.activityInOpen(activityCfg)
                || (type != optional && type != random)
                || (type == optional && !GameUtil.inScope(1, 6, req.getDicePoint()))
                || !ActivityUtil.activityInOpen(activityCfg)
                || activityCfg.getType() != ActivityTypeEnum.ATE_RichMan;
    }

    private void sendClientMsg(GameServerTcpChannel gsChn, SC_RichManRollDice.Builder resultMsg) {
        resultMsg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_RichManRollDice_VALUE, resultMsg);
    }

    private void playerForward(targetsystemEntity entity, int forwardPoint, SC_RichManRollDice.Builder resultMsg, ServerActivity activityCfg) {
        TargetSystemDB.DB_RichMan.Builder richManBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder();

        LogUtil.info("player:{} play rich man  old position:{}, old cycle:{}",
                entity.getLinkplayeridx(), richManBuilder.getCurPoint(), richManBuilder.getCycle());

        updateNewPositionDbData(entity, forwardPoint, activityCfg);

        int newPosition = richManBuilder.getCurPoint();

        resultMsg.addReachPoint(newPosition);
        resultMsg.addRollPoint(forwardPoint);

        LogUtil.info("player:{} play rich man roll dice forwardPoint:{}, in new position:{}, new cycle:{}",
                entity.getLinkplayeridx(), forwardPoint, newPosition, richManBuilder.getCycle());

        triggerPointEvent(entity, newPosition, resultMsg, activityCfg);

    }

    private void triggerPointEvent(targetsystemEntity entity, int newPosition, SC_RichManRollDice.Builder resultMsg, ServerActivity activityCfg) {
        Server.ServerRichManPoint richManPointCfg = activityCfg.getRichManPointMap().get(newPosition);

        String playerIdx = entity.getLinkplayeridx();
        if (richManPointCfg == null) {
            LogUtil.error("player:{} RichMan RollDice can`t triggerEvent  cause by richManPointCfg data is null where new position in :{}", playerIdx, newPosition);
            return;
        }

        Activity.RichManPointType pointType = richManPointCfg.getPointType();

        LogUtil.info("player:{} play rich man arrive new position:{},trigger event:{}", playerIdx, newPosition, pointType);

        switch (pointType) {
            case RMPT_Back:
                int forwardPoint = randomMovePoint();
                LogUtil.info("player:{} play rich trigger back ,back :{} point", playerIdx, forwardPoint);
                playerForward(entity, -forwardPoint, resultMsg, activityCfg);
                break;
            case RMPT_FreeReward:
                settleFreeReward(richManPointCfg,entity);
                break;
            case RMPT_RechargeRebate:
                savePlayerRechargeRebate(entity, richManPointCfg, playerIdx);
                break;
            case RMPT_BigReward:
                List<Common.Reward> rewards = settleFreeReward(richManPointCfg, entity);
                sendBigRewardMarquee(playerIdx, rewards);
                break;
            case RMPT_DoubleReward:
                entity.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().setDoubleReward(true);
                break;
            default:
        }

    }

    private void savePlayerRechargeRebate(targetsystemEntity entity, Server.ServerRichManPoint richMan, String playerIdx) {
        TargetSystemDB.DB_RichMan.Builder richManBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder();
        richManBuilder.setDischargeRebate(Math.max(richManBuilder.getDischargeRebate(), richMan.getRebate()));
        LogUtil.info("player:{} play rich trigger recharge discount:{} ,now can use discount:{}", playerIdx, richMan.getRebate(), richManBuilder.getDischargeRebate());
    }

    private void sendBigRewardMarquee(String playerIdx, List<Common.Reward> rewards) {
        int marqueeId = GameConfig.getById(GameConst.CONFIG_ID).getRichmanbigrewardmarquee();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player != null) {
            GlobalData.getInstance().sendSpecialMarqueeToAllOnlinePlayer(marqueeId, rewards, player.getName());
        }
    }

    private List<Common.Reward> settleFreeReward(Server.ServerRichManPoint richMan, targetsystemEntity target) {
        List<Common.Reward> rewardListList = playerCanObtainRewards(richMan, target);
        RewardManager.getInstance().doRewardByList(target.getLinkplayeridx(), rewardListList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RichMan), true);
        target.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().setDoubleReward(false);
        return rewardListList;
    }

    private List<Common.Reward> playerCanObtainRewards(Server.ServerRichManPoint richMan, targetsystemEntity target) {
        List<Common.Reward> rewardListList = richMan.getRewardListList();
        if (target.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().getDoubleReward()){
            return RewardUtil.multiReward(rewardListList,2);
        }
        return rewardListList;
    }

    private void updateNewPositionDbData(targetsystemEntity entity, int forwardPoint, ServerActivity activityCfg) {
        TargetSystemDB.DB_RichMan.Builder richManBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder();

        int cyclePointNum = activityCfg.getRichManPointCount();

        int newPoint = forwardPoint + richManBuilder.getCurPoint();

        richManBuilder.setCurPoint(newPoint % cyclePointNum);

        int addCycle = newPoint / cyclePointNum;
        richManBuilder.setCycle(richManBuilder.getCycle() + addCycle);

        if (addCycle > 0) {
            int curCycle = richManBuilder.getCycle();
            entity.updateStageRewardTarget(activityCfg.getActivityId(), curCycle);
            entity.sendUpdateStageRewardInfo(activityCfg.getActivityId());
            RankingManager.getInstance().updatePlayerRankingScore(entity.getLinkplayeridx(), activityCfg.getRankingType(),
                    RankingUtils.getActivityRankingName(activityCfg), curCycle, 0);
        }
    }

    private int getPlayerMovePoint(CS_RichManRollDice req) {
        if (req.getType() == random) {
            return randomMovePoint();
        }
        if (req.getType() == optional) {
            return req.getDicePoint();
        }
        return 0;
    }

    private int randomMovePoint() {
        return 1 + RandomUtils.nextInt(6);
    }

    private boolean consumeMaterial(String playerIdx, int type) {
        Consume consume = parseConsumeByRollType(type);
        if (consume == null) {
            LogUtil.error("RichManRollDiceHandler consumeMaterial error cause by consume is null by rollDiceType :{}", type);
            return false;
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RichMan);
        return ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason);
    }

    private Consume parseConsumeByRollType(int type) {
        GameConfigObject config = GameConfig.getById(GameConst.CONFIG_ID);
        if (type == random) {
            return ConsumeUtil.parseConsume(config.getRichmanrandomdicecost());
        }
        if (type == optional) {
            return ConsumeUtil.parseConsume(config.getRichmanoptionaldicecost());
        }
        return null;
    }


    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
