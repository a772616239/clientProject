package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_UpdateMistCarryInfo;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateMistCarryInfo_VALUE)
public class QueryMistDailyRewardHandler extends AbstractBaseHandler<CS_UpdateMistCarryInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_UpdateMistCarryInfo parse(byte[] bytes) throws Exception {
        return CS_UpdateMistCarryInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateMistCarryInfo req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        player.sendMistDailyRewardInfoByRule(req.getRuleValue());
    }
}
