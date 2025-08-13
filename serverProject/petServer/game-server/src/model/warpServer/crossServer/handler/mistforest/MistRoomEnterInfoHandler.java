package model.warpServer.crossServer.handler.mistforest;

import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.SC_MistForestRoomInfo;
import protocol.ServerTransfer.CS_GS_MistRoomEnterInfo;

@MsgId(msgId = MsgIdEnum.CS_GS_MistRoomEnterInfo_VALUE)
public class MistRoomEnterInfoHandler extends AbstractHandler<CS_GS_MistRoomEnterInfo> {
    @Override
    protected CS_GS_MistRoomEnterInfo parse(byte[] bytes) throws Exception {
        return CS_GS_MistRoomEnterInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_MistRoomEnterInfo ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SC_MistForestRoomInfo.Builder builder = SC_MistForestRoomInfo.newBuilder();
        builder.setIsRevert(ret.getIsRevert());
        builder.setRoomInfo(ret.getRoomInfo());
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_MistForestRoomInfo_VALUE, builder);

        MistWorldMapConfigObject cfg = MistWorldMapConfig.getByMapid(ret.getRoomInfo().getMapId());
        if (cfg == null || cfg.getMaprule() != EnumMistRuleKind.EMRK_Common_VALUE) {
            return;
        }
        targetsystemEntity targetEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(player.getIdx());
        if (targetEntity == null) {
            return;
        }
        targetEntity.updateMistTargetMission();
        targetEntity.updateMistNewbieTask();
    }
}
