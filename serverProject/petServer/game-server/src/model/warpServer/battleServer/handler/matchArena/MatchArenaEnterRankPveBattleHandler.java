package model.warpServer.battleServer.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.matcharena.MatchArenaManager;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_EnterFight;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaEnterRankPveBattle;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaEnterRankPveBattle_VALUE)
public class MatchArenaEnterRankPveBattleHandler extends AbstractHandler<BS_GS_MatchArenaEnterRankPveBattle> {
    @Override
    protected BS_GS_MatchArenaEnterRankPveBattle parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaEnterRankPveBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaEnterRankPveBattle req, int i) {
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(req.getPlayerIdx());
        if (entity == null) {
            LogUtil.error("matchArena.MatchArenaEnterPveBattleHandler, playerIdx:" + req.getPlayerIdx() + ", matchArenaEntity is null");
            return;
        }

        CS_EnterFight enterBattleBuild = CS_EnterFight.newBuilder()
                .setType(BattleSubTypeEnum.BSTE_MatchArenaRanking)
                .addParamList(req.getOpponentIdx())
                .build();
        RetCodeEnum retCodeEnum = BattleManager.getInstance().enterPveBattle(req.getPlayerIdx(), enterBattleBuild);
        if (retCodeEnum != RetCodeEnum.RCE_Success) {
            LogUtil.error("MatchArenaEnterPveBattleHandler.execute, enter battle failed, detail:" + req);
        }

        MatchArenaManager.getInstance().removeMatchingPlayer(req.getPlayerIdx());
    }
}
