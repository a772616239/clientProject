package model.warpServer.crossServer.handler.mistforest;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.MistForestManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_GainBossActivityReward;

@MsgId(msgId = MsgIdEnum.CS_GS_GainBossActivityReward_VALUE)
public class MistGainBossActivityBoxHandler extends AbstractHandler<CS_GS_GainBossActivityReward> {
    @Override
    protected CS_GS_GainBossActivityReward parse(byte[] bytes) throws Exception {
        return CS_GS_GainBossActivityReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_GainBossActivityReward req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        MistForestManager.getInstance().getBossActivityManager().addMistGainBossActivityRewardData(req.getPlayerIdx(), req.getMistLevel());
    }
}
