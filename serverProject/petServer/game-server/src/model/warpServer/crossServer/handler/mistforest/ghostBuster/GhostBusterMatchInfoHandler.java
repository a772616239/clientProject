package model.warpServer.crossServer.handler.mistforest.ghostBuster;

import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.mistforest.MistForestManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.SC_GhostBusterMatchInfo;
import protocol.ServerTransfer.CS_GS_MatchGhostBusterRoomInfo;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_MatchGhostBusterRoomInfo_VALUE)
public class GhostBusterMatchInfoHandler extends AbstractHandler<CS_GS_MatchGhostBusterRoomInfo> {
    @Override
    protected CS_GS_MatchGhostBusterRoomInfo parse(byte[] bytes) throws Exception {
        return CS_GS_MatchGhostBusterRoomInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_MatchGhostBusterRoomInfo ret, int i) {
        playerEntity player;
        String ipPort = gsChn.channel.remoteAddress().toString().substring(1);
        LogUtil.info("recv CS_GS_MatchGhostBusterRoomInfo from:" + ipPort);
        SC_GhostBusterMatchInfo.Builder builder = SC_GhostBusterMatchInfo.newBuilder();
        int itemId = GameConst.GhostBusterTicketItemId;
        RewardSourceEnum reason = RewardSourceEnum.RSE_MistGhostBuster;
        player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        boolean removeTicket = SyncExecuteFunction.executePredicate(player, entity->{
            int ticket = entity.getDb_data().getGhostBusterData().getFreeTickets();
            if (ticket > 0) {
                entity.getDb_data().getGhostBusterDataBuilder().setFreeTickets(ticket - 1);
                return true;
            }
            //统计：玩法
            LogService.getInstance().submit(new GamePlayLog(entity.getIdx(), EnumFunction.MistGhostBuster));
            return false;
        });
        if (removeTicket) {
            player.sendMistFreeTickets();
        } else {
            Consume consume = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Item_VALUE, itemId, 1);
            if (!ConsumeManager.getInstance().consumeMaterial(player.getIdx(), consume,
                    ReasonManager.getInstance().borrowReason(reason, "入场"))) {
                LogUtil.error("MistForest consume error, remove MistForestTicketItem failed,rule=" + EnumMistRuleKind.EMRK_GhostBuster_VALUE);
            }
        }
        int svrIndex = CrossServerManager.getInstance().getServerIndexByCsAddr(ipPort);
        builder.setRoomInfo(ret.getRoomInfo());
        GlobalData.getInstance().sendMsg(ret.getPlayerIdx(), MsgIdEnum.SC_GhostBusterMatchInfo_VALUE, builder);
        CrossServerManager.getInstance().addMistForestPlayer(player.getIdx(), EnumMistRuleKind.EMRK_GhostBuster, svrIndex);

        MistForestManager.getInstance().getGhostBusterManager().matchSuccess(player, svrIndex);
    }
}