package server.handler.battle;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_BattleResult_VALUE)
public class BattleResultHandler extends AbstractBaseHandler<CS_BattleResult> {
    @Override
    protected CS_BattleResult parse(byte[] bytes) throws Exception {
        return CS_BattleResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BattleResult req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_BattleResult.Builder resultBuilder = SC_BattleResult.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("battleResult player not found,playerId=" + gsChn.getPlayerId1());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BattleResult_VALUE, resultBuilder);
            return;
        }

        BattleTypeEnum battleType = BattleManager.getInstance().getBattleType(playerIdx);
        LogUtil.debug("settle battle, battle type:" + battleType);
        if (battleType == BattleTypeEnum.BTE_PVE) {
            BattleManager.getInstance().settleBattle(playerIdx, req);
        } else {
            BattleServerManager.getInstance().transferMsgToBattleServer(
                    playerIdx, MsgIdEnum.CS_BattleResult_VALUE, req.toByteString(), true);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        doAction(gsChn, codeNum);
    }
}
