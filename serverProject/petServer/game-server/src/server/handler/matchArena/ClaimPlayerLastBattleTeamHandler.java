package server.handler.matchArena;

import cfg.FunctionOpenLvConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import common.AbstractBaseHandler;
import common.GameConst.RedisKey;
import common.JedisUtil;
import static common.JedisUtil.jedis;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_ClaimPlayerLastBattleTeam;
import protocol.MatchArena.SC_ClaimPlayerLastBattleTeam;
import protocol.MessageId.MsgIdEnum;
import protocol.RecentPassedOuterClass.RecentPassed;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author huhan
 * @date 2021/05/18
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimPlayerLastBattleTeam_VALUE)
public class ClaimPlayerLastBattleTeamHandler extends AbstractBaseHandler<CS_ClaimPlayerLastBattleTeam> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClaimPlayerLastBattleTeam.Builder resultBuilder = SC_ClaimPlayerLastBattleTeam.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_ClaimPlayerLastBattleTeam_VALUE, resultBuilder);
    }

    @Override
    protected CS_ClaimPlayerLastBattleTeam parse(byte[] bytes) throws Exception {
        return CS_ClaimPlayerLastBattleTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimPlayerLastBattleTeam req, int i) {
        SC_ClaimPlayerLastBattleTeam.Builder resultBuilder = SC_ClaimPlayerLastBattleTeam.newBuilder();
        if (StringUtils.isEmpty(req.getPlayerIdx())) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimPlayerLastBattleTeam_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_MatchArena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_ClaimPlayerLastBattleTeam_VALUE, resultBuilder);
            return;
        }

        RecentPassed recentPassed = null;
        byte[] value = jedis.hget(RedisKey.MatchArenaRecentBattle.getBytes(StandardCharsets.UTF_8),
                req.getPlayerIdx().getBytes(StandardCharsets.UTF_8));
        if (value != null) {
            try {
                recentPassed = RecentPassed.parseFrom(value);
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }
        if (recentPassed == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimPlayerLastBattleTeam_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setRecentPassed(recentPassed);
        gsChn.send(MsgIdEnum.SC_ClaimPlayerLastBattleTeam_VALUE, resultBuilder);
    }
}
