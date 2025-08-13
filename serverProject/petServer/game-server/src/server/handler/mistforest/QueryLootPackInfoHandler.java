package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_UpdateMistLootPackInfo;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateMistLootPackInfo_VALUE)
public class QueryLootPackInfoHandler extends AbstractBaseHandler<CS_UpdateMistLootPackInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_UpdateMistLootPackInfo parse(byte[] bytes) throws Exception {
        return CS_UpdateMistLootPackInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateMistLootPackInfo req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        player.sendMistCarryRewardInfoByRule(req.getRuleValue());
    }
}
