package server.handler.activity;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.ranking.EnumRankingSenderType;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import protocol.Activity.CS_ClaimActivityRanking;
import protocol.Activity.SC_ClaimActivityRanking;
import protocol.Activity.SC_ClaimActivityRanking.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/06/03
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimActivityRanking_VALUE)
public class ClaimActivityRankingHandler extends AbstractBaseHandler<CS_ClaimActivityRanking> {
    @Override
    protected CS_ClaimActivityRanking parse(byte[] bytes) throws Exception {
        return CS_ClaimActivityRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimActivityRanking req, int i) {
        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        Builder resultBuilder = SC_ClaimActivityRanking.newBuilder();
        if (activity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ClaimActivityRanking_VALUE, resultBuilder);
            return;
        }

        LogUtil.debug("claim activity ranking, rankingType:" + activity.getRankingType());
        RankingManager.getInstance().sendRankingInfoToPlayer(EnumRankingSenderType.ERST_Activity, activity.getRankingType(),
                RankingUtils.getActivityRankingName(activity), String.valueOf(gsChn.getPlayerId1()));
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Activity;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimActivityRanking_VALUE, SC_ClaimActivityRanking.newBuilder().setRetCode(retCode));
    }
}
