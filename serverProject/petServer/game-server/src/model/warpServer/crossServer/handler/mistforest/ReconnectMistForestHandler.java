package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.warpServer.crossServer.CrossServerManager;
import platform.logs.LogService;
import platform.logs.entity.MistPlayTimeLog;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.ServerTransfer.CS_GS_ReconnectMistForest;
import protocol.TransServerCommon.PlayerMistServerInfo;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_ReconnectMistForest_VALUE)
public class ReconnectMistForestHandler extends AbstractHandler<CS_GS_ReconnectMistForest> {
    @Override
    protected CS_GS_ReconnectMistForest parse(byte[] bytes) throws Exception {
        return CS_GS_ReconnectMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ReconnectMistForest req, int i) {
        if (!req.getResult()) {
            PlayerMistServerInfo serverInfo = CrossServerManager.getInstance().getMistForestPlayerServerInfo(req.getPlayerIdx());
            CrossServerManager.getInstance().removeMistForestPlayer(req.getPlayerIdx());
            playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
            if (player != null) {
                SyncExecuteFunction.executeConsumer(player, entity -> {
                    if (entity.getLastEnterMistTime() > 0 && serverInfo.getMistRule() == EnumMistRuleKind.EMRK_Common) {
                        entity.setLastEnterMistTime(0);
                        int mistLevel = 0;
                        targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(entity.getIdx());
                        if (targetEntity != null) {
                            mistLevel = targetEntity.getDb_Builder().getMistTaskData().getCurEnterLevel();
                        }
                        LogService.getInstance().submit(new MistPlayTimeLog(entity.getIdx(), mistLevel, entity.getDb_data().getMistForestData().getStamina(), false));
                    }
                    entity.settleMistCarryReward();
                });
            }
        }
        LogUtil.info("Recv ReconnectMistForest info, playerIdx=" + req.getPlayerIdx());
    }
}
