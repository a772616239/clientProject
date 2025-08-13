package model.warpServer.crossServer.handler.thewar;

import cfg.TheWarConstConfig;
import cfg.TheWarSeasonConfig;
import cfg.TheWarSeasonConfigObject;
import common.GameConst;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TheWarRoomSettleData;
import util.EventUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_TheWarRoomSettleData_VALUE)
public class TheWarRoomSettleHandler extends AbstractHandler<CS_GS_TheWarRoomSettleData> {
    @Override
    protected CS_GS_TheWarRoomSettleData parse(byte[] bytes) throws Exception {
        return CS_GS_TheWarRoomSettleData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_TheWarRoomSettleData req, int i) {
        LogUtil.info("recv TheWarRoom settleInfo, rank="+req.getRank());
        TheWarSeasonConfigObject config = TheWarSeasonConfig.getInstance().getWarOpenConfig();
        if (config == null) {
            return;
        }
        List<Reward> rewards = RewardUtil.getRewardsByRewardId(config.getRewardIdByRank(req.getRank()));
        if (rewards == null) {
            LogUtil.error("WarRoom settle reward not found, rank = " + req.getRank());
            return;
        }
        int mailId = TheWarConstConfig.getById(GameConst.CONFIG_ID).getCampseasonrankmailid();
        playerEntity player;
        for (String playerIdx : req.getPlayerIdxList()) {
            player = playerCache.getByIdx(playerIdx);
            if (player == null) {
                continue;
            }
            SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().setTheWarRoomIdx(""));

            EventUtil.triggerAddMailEvent(playerIdx, mailId, rewards,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TheWar), StringHelper.IntTostring(req.getRank(), ""));
        }
    }
}
