package model.warpServer.crossServer.handler;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.warpServer.crossServer.CrossServerManager;
import platform.logs.LogService;
import platform.logs.entity.MistPlayTimeLog;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.SC_KickOutFromMistForest;
import protocol.ServerTransfer.PlayerOffline;
import protocol.TransServerCommon.PlayerMistServerInfo;

@MsgId(msgId = MsgIdEnum.PlayerOffline_VALUE)
public class OfflineHandler extends AbstractHandler<PlayerOffline> {
    @Override
    protected PlayerOffline parse(byte[] bytes) throws Exception {
        return PlayerOffline.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, PlayerOffline req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }

        SC_KickOutFromMistForest.Builder builder = SC_KickOutFromMistForest.newBuilder();
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_KickOutFromMistForest_VALUE, builder);

        PlayerMistServerInfo serverInfo =CrossServerManager.getInstance().getMistForestPlayerServerInfo(player.getIdx());
        boolean leaveCommonRule = serverInfo != null && serverInfo.getMistRule() == EnumMistRuleKind.EMRK_Common;
        CrossServerManager.getInstance().removeMistForestPlayer(player.getIdx());
        SyncExecuteFunction.executeConsumer(player, entity -> {
            entity.settleMistCarryReward();
            if (entity.getLastEnterMistTime() > 0 && leaveCommonRule) {
                entity.setLastEnterMistTime(0);
                int mistLevel = 0;
                targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(entity.getIdx());
                if (targetEntity != null) {
                    mistLevel = targetEntity.getDb_Builder().getMistTaskData().getCurEnterLevel();
                }
                LogService.getInstance().submit(new MistPlayTimeLog(entity.getIdx(), mistLevel, entity.getDb_data().getMistForestData().getStamina(), false));
            }
        });

        BattleManager.getInstance().onOwnerLeave(req.getPlayerIdx(), true);
//        SyncExecuteFunction.executeConsumer(player, entity -> entity.getBattleController().onOwnerLeave(true));
    }
}
