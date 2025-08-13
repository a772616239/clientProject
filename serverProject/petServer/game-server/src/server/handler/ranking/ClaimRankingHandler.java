package server.handler.ranking;

import common.AbstractBaseHandler;
import common.GameConst.RankingName;
import common.GlobalData;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.magicthron.MagicThronManager;
import model.player.util.PlayerUtil;
import model.ranking.EnumRankingSenderType;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Activity;
import protocol.Activity.CS_ClaimRanking;
import protocol.Activity.EnumRankingType;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.Server;
import protocol.ServerTransfer;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/12/7
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimRanking_VALUE)
public class ClaimRankingHandler extends AbstractBaseHandler<CS_ClaimRanking> {
    @Override
    protected CS_ClaimRanking parse(byte[] bytes) throws Exception {
        return CS_ClaimRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimRanking req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (req.getRankingType() == EnumRankingType.ERT_Null) {
            return;
        }
        //远征
        if (req.getRankingType() == EnumRankingType.ERT_TheWar_KillMonster) {
            sendTheWarRanking(playerIdx);
            return;
        }
        //活动排行榜(大富翁走)
        if (req.getRankingType() == EnumRankingType.ERT_RichMan) {
            sendActivityRanking(playerIdx, req);
            return;
        }
        if (EnumRankingType.ERT_ArenaGainScore == req.getRankingType()) {
            claimLocalDanRanking(gsChn);
            return;
        }
        if (EnumRankingType.ERT_ArenaScoreLocalDan == req.getRankingType()) {
            claimLocalDanLocalRanking(gsChn);
            return;
        }
        if (EnumRankingType.ERT_ArenaScoreLocal == req.getRankingType()) {
            sendLocalRanking(req, playerIdx);
            return;
        }
       /* if (EnumRankingType.ERT_MagicThronDamage == req.getRankingType()) {
            RankingManager.getInstance().sendRankingInfoToPlayer(getRankingSenderType(req.getRankingType()),
                    req.getRankingType(), RankingUtils.getMagicRankName(MagicThronManager.getInstance().findPlayerNowArea(playerIdx)), playerIdx);
            return;
        }*/

        //通用排行榜
        RankingManager.getInstance().sendRankingInfoToPlayer(getRankingSenderType(req.getRankingType()),
                req.getRankingType(), RankingUtils.getRankingTypeDefaultName(req.getRankingType()), playerIdx);
    }

    private void sendActivityRanking(String playerIdx, CS_ClaimRanking req) {
        Server.ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        if (activity == null) {
            Activity.SC_ClaimActivityRanking.Builder msg = Activity.SC_ClaimActivityRanking.newBuilder();
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimActivityRanking_VALUE, msg);
            return;
        }
        RankingManager.getInstance().sendRankingInfoToPlayer(getRankingSenderType(req.getRankingType()),
                req.getRankingType(), RankingUtils.getActivityRankingName(activity), playerIdx);

    }

    private void sendTheWarRanking(String playerIdx) {
        RankingManager.getInstance().sendRankingInfoToPlayer(EnumRankingSenderType.ERST_Common,
                EnumRankingType.ERT_TheWar_KillMonster, RankingName.RN_TheWar_KillMonsterCount, playerIdx);
    }


    private EnumRankingSenderType getRankingSenderType(EnumRankingType rankingType) {
        if (EnumRankingType.ERT_HellPet == rankingType || EnumRankingType.ERT_AbyssPet == rankingType
                || EnumRankingType.ERT_NaturePet == rankingType || EnumRankingType.ERT_WildPet == rankingType) {
            return EnumRankingSenderType.ERST_PetAbilityRanking;
        }
        if (rankingType == EnumRankingType.ERT_ArenaScoreLocal || rankingType == EnumRankingType.ERT_ArenaScoreCross
                || rankingType == EnumRankingType.ERT_ArenaScoreLocalDan) {
            return EnumRankingSenderType.ERST_ArenaDanRanking;
        }

        if (rankingType == EnumRankingType.ERT_MatchArena_Local
                || rankingType == EnumRankingType.ERT_MatchArena_Cross) {
            return EnumRankingSenderType.ERST_MatchArenaRanking;
        }

        return EnumRankingSenderType.ERST_Common;
    }

    private void sendLocalRanking(CS_ClaimRanking req, String playerIdx) {
        EnumRankingSenderType senderType = getRankingSenderType(req.getRankingType());

        RankingManager.getInstance().sendRankingInfoToPlayer(senderType, EnumRankingType.ERT_ArenaScoreLocal, playerIdx);
    }

    private void claimLocalDanLocalRanking(GameServerTcpChannel gsChn) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Activity.SC_ClaimRanking.Builder resultBuilder = Activity.SC_ClaimRanking.newBuilder();
        if (!PlayerUtil.queryFunctionUnlock(playerIdx,EnumFunction.Arena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_ClaimRanking_VALUE, resultBuilder);
            return;
        }
        arenaEntity entity = arenaCache.getByIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimRanking_VALUE, resultBuilder);
            return;
        }

        String arenaGainScoreRankName = RankingUtils.getArenaScoreLocalDanRankName(entity.getDbBuilder().getDan());
        RankingManager.getInstance().sendRankingInfoToPlayer(getRankingSenderType(EnumRankingType.ERT_ArenaScoreLocalDan),
                EnumRankingType.ERT_ArenaScoreLocalDan, arenaGainScoreRankName, playerIdx);
    }

    private void claimLocalDanRanking(GameServerTcpChannel gsChn) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Activity.SC_ClaimRanking.Builder resultBuilder = Activity.SC_ClaimRanking.newBuilder();
        if (!PlayerUtil.queryFunctionUnlock(playerIdx,EnumFunction.Arena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_ClaimRanking_VALUE, resultBuilder);
            return;
        }

        arenaEntity entity = arenaCache.getByIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimRanking_VALUE, resultBuilder);
            return;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        //TODO 暂时屏蔽排行榜刷新限制
    /*    if ((currentTime - entity.getDbBuilder().getLastClaimRankingTime()) <=
                (ArenaConfig.getById(GameConst.CONFIG_ID).getRankrefrshtime() - 5) * TimeUtil.MS_IN_A_S) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Arena_RefreshFrequently));
            gsChn.send(MsgIdEnum.SC_ClaimRanking_VALUE, resultBuilder);
            return;
        }*/

        if (!CrossServerManager.getInstance().sendMsgToArena(playerIdx, MsgIdEnum.GS_CS_ClaimArenaRanking_VALUE,
                ServerTransfer.GS_CS_ClaimArenaRanking.newBuilder().setPlayerIdx(playerIdx), false)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Arena_CanNotFindServer));
            gsChn.send(MsgIdEnum.SC_ClaimRanking_VALUE, resultBuilder);
        }
    }


    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}
