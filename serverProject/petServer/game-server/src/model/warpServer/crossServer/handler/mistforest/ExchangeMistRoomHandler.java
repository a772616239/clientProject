package model.warpServer.crossServer.handler.mistforest;

import common.SyncExecuteFunction;
import common.load.ServerConfig;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.dbCache.teamCache;
import model.warpServer.BaseNettyClient;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_ExchangeMistForest;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.ServerTransfer.CS_GS_ExchangeMistRoom;
import protocol.ServerTransfer.EnumJoinMistForestType;
import protocol.ServerTransfer.GS_CS_JoinMistForest;

@MsgId(msgId = MsgIdEnum.CS_GS_ExchangeMistRoom_VALUE)
public class ExchangeMistRoomHandler extends AbstractHandler<CS_GS_ExchangeMistRoom> {
    @Override
    protected CS_GS_ExchangeMistRoom parse(byte[] bytes) throws Exception {
        return CS_GS_ExchangeMistRoom.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ExchangeMistRoom req, int i) {
        playerEntity player = playerCache.getByIdx(req.getIdx());
        if (player == null) {
            return;
        }
        CrossServerManager.getInstance().removeMistForestPlayer(player.getIdx());
        SyncExecuteFunction.executeConsumer(player, entity -> entity.settleMistCarryReward());

        BaseNettyClient nettyClient = CrossServerManager.getInstance().getAvailableMistForestClient(req.getMistRuleValue(), req.getNewMistLevel());

//        //目标：迷雾深林阶层
//        EventUtil.triggerUpdateTargetProgress(req.getIdx(), TargetTypeEnum.TTE_MistLevel, req.getNewMistLevel(), 0);

        if (nettyClient == null) {
            gsChn.send(MsgIdEnum.SC_ExchangeMistForest_VALUE, SC_ExchangeMistForest.newBuilder());
            return;
        }
        List<BattlePetData> petData = teamCache.getInstance().buildBattlePetData(req.getIdx(), TeamTypeEnum.TTE_Common,
                BattleSubTypeEnum.BSTE_MistForest);
        if (petData == null) {
            // TODO 返回错误
            return;
        }
        List<Integer> skills = teamCache.getInstance().getCurUsedTeamSkillList(req.getIdx(), TeamTypeEnum.TTE_Common);

        GS_CS_JoinMistForest.Builder builder = GS_CS_JoinMistForest.newBuilder();
        builder.setServerIndex(ServerConfig.getInstance().getServer());
        builder.setPlayerBaseData(player.getBattleBaseData());
        builder.setMistForestLevel(req.getNewMistLevel());
        builder.setJoinType(EnumJoinMistForestType.EJFT_ExchangeJoin);
        builder.addAllPetList(petData);
        if (skills != null) {
            builder.addAllPlayerSkillIdList(player.getSkillBattleDict(skills));
        }
        builder.addAllItemData(player.getDb_data().getMistForestData().getMistItemDataList());

        nettyClient.send(MsgIdEnum.GS_CS_JoinMistForest_VALUE, builder);
    }
}
