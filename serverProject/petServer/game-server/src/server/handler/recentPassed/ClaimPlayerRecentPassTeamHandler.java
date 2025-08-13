package server.handler.recentPassed;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import model.playerrecentpass.dbCache.playerrecentpassCache;
import model.playerrecentpass.entity.playerrecentpassEntity;
import protocol.Activity.EnumRankingType;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RecentPassedDB.DB_RecentPlayerInfo;
import protocol.RecentPassedOuterClass.CS_ClaimPlayerRecentPassTeam;
import protocol.RecentPassedOuterClass.RecentPassed;
import protocol.RecentPassedOuterClass.SC_ClaimPlayerRecentPassTeam;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2021/1/19
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimPlayerRecentPassTeam_VALUE)
public class ClaimPlayerRecentPassTeamHandler extends AbstractBaseHandler<CS_ClaimPlayerRecentPassTeam> {

    public static final Map<EnumRankingType, EnumFunction> RANKING_FUNCTION_MAP;

    static {
        Map<EnumRankingType, EnumFunction> tempMap = new HashMap<>();
        tempMap.put(EnumRankingType.ERT_MainLine, EnumFunction.MainLine);
        tempMap.put(EnumRankingType.ERT_Spire, EnumFunction.Endless);

        RANKING_FUNCTION_MAP = Collections.unmodifiableMap(tempMap);
    }

    @Override
    protected CS_ClaimPlayerRecentPassTeam parse(byte[] bytes) throws Exception {
        return CS_ClaimPlayerRecentPassTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimPlayerRecentPassTeam req, int i) {
        playerrecentpassEntity entity = playerrecentpassCache.getInstance().getEntity(req.getPlayerIdx());

        SC_ClaimPlayerRecentPassTeam.Builder resultBuilder = SC_ClaimPlayerRecentPassTeam.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimPlayerRecentPassTeam_VALUE, resultBuilder);
            return;
        }

        EnumFunction mapFunction = RANKING_FUNCTION_MAP.get(req.getRankingType());
        DB_RecentPlayerInfo recentPlayerInfo = SyncExecuteFunction.executeFunction(entity, e -> entity.getRecentPlayerTeam(mapFunction));
        RecentPassed.Builder recentInfo = ClaimRecentPassedHandler.builderRecentPassed(recentPlayerInfo);

        if (recentInfo == null) {
            LogUtil.error("ClaimPlayerRecentPassTeamHandler.execute, ranking type can not find pass team:"
                    + req.getRankingType() + ", playerIdx:" + req.getPlayerIdx());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimPlayerRecentPassTeam_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setRecentPassed(recentInfo);
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimPlayerRecentPassTeam_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}
