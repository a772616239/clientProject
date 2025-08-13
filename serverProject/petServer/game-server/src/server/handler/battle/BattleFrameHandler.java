package server.handler.battle;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.BattleMono.CS_FrameData;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_FrameData_VALUE)
public class BattleFrameHandler extends AbstractBaseHandler<CS_FrameData> {
    @Override
    protected CS_FrameData parse(byte[] bytes) throws Exception {
        return CS_FrameData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_FrameData req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("recv battle frame data but player is null");
            return;
        }

        BattleManager.getInstance().handleBattleFrameData(playerIdx, req);
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
