package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistVipSkillData;
import protocol.PlayerDB.DB_MistForestData;
import protocol.ServerTransfer.CS_GS_UpdateVipSkillData;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateVipSkillData_VALUE)
public class UpdateVipSkillDataHandler extends AbstractHandler<CS_GS_UpdateVipSkillData> {
    @Override
    protected CS_GS_UpdateVipSkillData parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateVipSkillData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateVipSkillData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_MistForestData.Builder mistBuilder = entity.getDb_data().getMistForestDataBuilder();
            for (MistVipSkillData skillData : ret.getVipSkillDataList()) {
                boolean existFlag = false;
                for (MistVipSkillData.Builder skill : mistBuilder.getVipSkillDataBuilderList()) {
                    if (skill.getSkillId() == skillData.getSkillId()) {
                        skill.setSkillStack(skillData.getSkillStack());
                        skill.setExpireTimestamp(skillData.getExpireTimestamp());
                        existFlag = true;
                        break;
                    }
                }

                if (!existFlag) {
                    MistVipSkillData.Builder builder = MistVipSkillData.newBuilder();
                    builder.setSkillId(skillData.getSkillId());
                    builder.setSkillStack(skillData.getSkillStack());
                    builder.setExpireTimestamp(skillData.getExpireTimestamp());
                    mistBuilder.addVipSkillData(builder);
                }
            }
        });
    }
}
