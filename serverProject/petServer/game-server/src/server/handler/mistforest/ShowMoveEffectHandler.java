package server.handler.mistforest;

import common.AbstractBaseHandler;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_ShowMoveEffectInfo;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ShowMoveEffectInfo_VALUE)
public class ShowMoveEffectHandler extends AbstractBaseHandler<CS_ShowMoveEffectInfo> {
    @Override
    protected CS_ShowMoveEffectInfo parse(byte[] bytes) throws Exception {
        return null;
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ShowMoveEffectInfo req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        player.showAllMoveEffect(GlobalTick.getInstance().getCurrentTime());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}
