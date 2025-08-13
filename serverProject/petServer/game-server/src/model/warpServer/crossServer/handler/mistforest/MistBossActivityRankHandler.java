package model.warpServer.crossServer.handler.mistforest;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.MistForestManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_UpdateActivityBossDmgRank;
import protocol.ServerTransfer.MistBossDmgRankInfo;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateActivityBossDmgRank_VALUE)
public class MistBossActivityRankHandler extends AbstractHandler<CS_GS_UpdateActivityBossDmgRank> {
    @Override
    protected CS_GS_UpdateActivityBossDmgRank parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateActivityBossDmgRank.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateActivityBossDmgRank req, int i) {
        playerEntity player;
        for (MistBossDmgRankInfo rankInfo : req.getRankDataList()) {
            player = playerCache.getByIdx(rankInfo.getPlayerIdx());
            if (player == null) {
                continue;
            }
            MistForestManager.getInstance().getBossActivityManager().addMistRankData(req.getMistLevel(), rankInfo);
        }
    }
}
