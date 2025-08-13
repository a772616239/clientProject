package server.handler.mistforest;

import cfg.MistWorldMapConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.CS_FinishMistDialog;
import protocol.TargetSystem.TargetTypeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_FinishMistDialog_VALUE)
public class FinishMistDialogHandler extends AbstractBaseHandler<CS_FinishMistDialog> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_FinishMistDialog parse(byte[] bytes) throws Exception {
        return CS_FinishMistDialog.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_FinishMistDialog req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (targetEntity == null) {
            return;
        }
        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(playerId) <= 0) {
            return;
        }
        if (targetEntity.getDb_Builder().getMistTaskData().getCurNewbieTask().getCfgId() != req.getTaskId()) {
            return;
        }
        SyncExecuteFunction.executeConsumer(targetEntity, entity -> {
            entity.doMistNewbieTask(TargetTypeEnum.TTE_Mist_FinishMistNewbieDialog, 1, 0);
            int enterLevel = entity.getDb_Builder().getMistTaskData().getCurEnterLevel();
            if (entity.isFinishedMistNewbieTask() && enterLevel == MistWorldMapConfig.getInstance().getDefaultCommonMistLevel()) {
                entity.initNewLevelSweepTaskData(enterLevel);
                entity.sendMistSweepTaskData();
            }
        });
    }
}
