package model.warpServer.crossServer.handler.mistforest;

import cfg.GameConfig;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_UpdateEliteMonsterRewardTimes;
import protocol.ServerTransfer.CS_GS_UpdateEliteMonsterRewardTimes;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateEliteMonsterRewardTimes_VALUE)
public class UpdateEliteMonsterRewardTimesHandler extends AbstractHandler<CS_GS_UpdateEliteMonsterRewardTimes> {
    @Override
    protected CS_GS_UpdateEliteMonsterRewardTimes parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateEliteMonsterRewardTimes.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateEliteMonsterRewardTimes ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeFunction(player, entity ->
            entity.getDb_data().getMistForestDataBuilder().setEliteMonsterRewardTimes(ret.getNewRewardTimes()));

        int dailyMax = GameConfig.getById(GameConst.CONFIG_ID).getDailyelitemonsterrewradtimes();
        int rewardCount = dailyMax > ret.getNewRewardTimes() ? dailyMax - ret.getNewRewardTimes() : 0;
        SC_UpdateEliteMonsterRewardTimes.Builder builder = SC_UpdateEliteMonsterRewardTimes.newBuilder();
        builder.setRewardTimes(rewardCount);
        GlobalData.getInstance().sendMsg(ret.getPlayerIdx(), MsgIdEnum.SC_UpdateEliteMonsterRewardTimes_VALUE, builder);
    }
}
