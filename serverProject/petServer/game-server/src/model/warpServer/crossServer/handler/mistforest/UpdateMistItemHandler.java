package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_UpdateMistItemData;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateMistItemData_VALUE)
public class UpdateMistItemHandler extends AbstractHandler<CS_GS_UpdateMistItemData> {
    @Override
    protected CS_GS_UpdateMistItemData parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateMistItemData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateMistItemData req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            switch (req.getMistRule()) {
                case EMRK_Common: {
                    entity.getDb_data().getMistForestDataBuilder().clearMistItemData();
                    entity.getDb_data().getMistForestDataBuilder().addAllMistItemData(req.getItemDataList());
                    if (!req.getAddItem()) {
                        EventUtil.triggerUpdateTargetProgress(entity.getIdx(), TargetTypeEnum.TTE_MistSeasonTask_UseItemCount, 1, 0);
                    }
                    break;
                }
                case EMRK_Maze: {
                    entity.getDb_data().getMazeDataBuilder().clearMistMazeItemData();
                    entity.getDb_data().getMazeDataBuilder().addAllMistMazeItemData(req.getItemDataList());
                    break;
                }
                case EMRK_GhostBuster: {
                    entity.getDb_data().getGhostBusterDataBuilder().clearMistGhostItemData();
                    entity.getDb_data().getGhostBusterDataBuilder().addAllMistGhostItemData(req.getItemDataList());
                    break;
                }
                default:
                    break;
            }
        });
    }
}
