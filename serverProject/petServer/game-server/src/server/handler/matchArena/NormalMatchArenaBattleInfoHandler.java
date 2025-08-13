package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaManager;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.dto.MatchArenaRobotCfg;
import model.matcharena.entity.matcharenaEntity;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.MatchArena;
import protocol.MatchArena.CS_NormalMatchArenaBattleInfo;
import protocol.MatchArena.SC_NormalMatchArenaBattleInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/05/18
 */
@MsgId(msgId = MsgIdEnum.CS_NormalMatchArenaBattleInfo_VALUE)
public class NormalMatchArenaBattleInfoHandler extends AbstractBaseHandler<CS_NormalMatchArenaBattleInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_NormalMatchArenaBattleInfo.Builder resultBuilder = SC_NormalMatchArenaBattleInfo.newBuilder();
        MatchArenaRobotCfg curRobotCfg = MatchArenaManager.getInstance().getCurRobotCfg();
        if (curRobotCfg != null) {
            resultBuilder.setPetRarity(curRobotCfg.getRarity());
            resultBuilder.setPetLevel(curRobotCfg.getLevel());
            resultBuilder.setExpireTime(curRobotCfg.getExpireTime());
        }
        gsChn.send(MsgIdEnum.SC_NormalMatchArenaBattleInfo_VALUE, resultBuilder);
    }


    @Override
    protected CS_NormalMatchArenaBattleInfo parse(byte[] bytes) throws Exception {
        return CS_NormalMatchArenaBattleInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_NormalMatchArenaBattleInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);

        SC_NormalMatchArenaBattleInfo.Builder resultBuilder = SC_NormalMatchArenaBattleInfo.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_MatchArena)) {
            gsChn.send(MsgIdEnum.SC_NormalMatchArenaBattleInfo_VALUE, resultBuilder);
            return;
        }

        if (entity == null) {
            gsChn.send(MsgIdEnum.SC_NormalMatchArenaBattleInfo_VALUE, resultBuilder);
            return;
        }

        MatchArenaManager.getInstance().sendNormalMatchArenaBattleInfo(playerIdx);
    }
}
