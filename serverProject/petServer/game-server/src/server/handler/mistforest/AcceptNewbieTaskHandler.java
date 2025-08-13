package server.handler.mistforest;

import cfg.Mission;
import cfg.MissionObject;
import cfg.MistNewbieTaskConfig;
import cfg.MistNewbieTaskConfigObject;
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
import protocol.TargetSystem.CS_AcceptMistNewbieTask;
import protocol.TargetSystem.SC_UpdateMistNewbieTask;
import protocol.TargetSystem.TargetMission;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_AcceptMistNewbieTask_VALUE)
public class AcceptNewbieTaskHandler extends AbstractBaseHandler<CS_AcceptMistNewbieTask> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_AcceptMistNewbieTask parse(byte[] bytes) throws Exception {
        return CS_AcceptMistNewbieTask.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_AcceptMistNewbieTask req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        targetsystemEntity targetSystem = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (targetSystem == null) {
            return;
        }
        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(playerId) <= 0) {
            return;
        }

        SyncExecuteFunction.executeConsumer(targetSystem, entity->{
            TargetMission.Builder targetBuilder = entity.getDb_Builder().getMistTaskDataBuilder().getCurNewbieTaskBuilder();
            if (targetBuilder.getCfgId() > 0) {
                return;
            }
            MistNewbieTaskConfigObject cfg = MistNewbieTaskConfig.getById(targetBuilder.getCfgId() + 1);
            if (cfg == null) {
                return;
            }
            MissionObject missionCfg = Mission.getById(cfg.getMissionid());
            if (missionCfg == null) {
                return;
            }
            targetBuilder.setCfgId(targetBuilder.getCfgId() + 1);
            SC_UpdateMistNewbieTask.Builder builder = SC_UpdateMistNewbieTask.newBuilder();
            builder.setHasAcceptedTask(true);
            builder.setTaskData(targetBuilder);
            gsChn.send(MsgIdEnum.SC_UpdateMistNewbieTask_VALUE, builder);
        });
    }
}
