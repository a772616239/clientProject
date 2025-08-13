package model.warpServer.crossServer.handler.mistforest;

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
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_LeaveMistForest;
import protocol.ServerTransfer.CS_GS_LeaveMistForest;
import protocol.TransServerCommon.PlayerMistServerInfo;

@MsgId(msgId = MsgIdEnum.CS_GS_LeaveMistForest_VALUE)
public class LeaveMistForestHandler extends AbstractHandler<CS_GS_LeaveMistForest> {
    @Override
    protected CS_GS_LeaveMistForest parse(byte[] bytes) throws Exception {
        return CS_GS_LeaveMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_LeaveMistForest req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        if (req.getRetCode() == MistRetCode.MRC_Success) {
            PlayerMistServerInfo svrInfo = CrossServerManager.getInstance().getMistForestPlayerServerInfo(req.getPlayerIdx());
            if (svrInfo != null) {
                SyncExecuteFunction.executeConsumer(player, entity -> {
                    CrossServerManager.getInstance().removeMistForestPlayer(player.getIdx());
                    if (svrInfo.getMistRule() == EnumMistRuleKind.EMRK_GhostBuster) {
                        entity.addForceExitGhostScoreRecord();
                    }
                    entity.settleMistCarryReward();
                    if (entity.getLastEnterMistTime() > 0 && svrInfo.getMistRule() == EnumMistRuleKind.EMRK_Common) {
                        entity.setLastEnterMistTime(0);
                        int mistLevel = 0;
                        targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(entity.getIdx());
                        if (targetEntity != null) {
                            mistLevel = targetEntity.getDb_Builder().getMistTaskData().getCurEnterLevel();
                        }
                        LogService.getInstance().submit(new MistPlayTimeLog(entity.getIdx(), mistLevel, entity.getDb_data().getMistForestData().getStamina(), false));
                    }
                });
            }

            BattleManager.getInstance().onOwnerLeave(req.getPlayerIdx(), true);
//            SyncExecuteFunction.executeConsumer(player, entity -> entity.getBattleController().onOwnerLeave(true));
        }
        SC_LeaveMistForest.Builder builder = SC_LeaveMistForest.newBuilder();
        builder.setRetCode(req.getRetCode());
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_LeaveMistForest_VALUE, builder);
    }
}
