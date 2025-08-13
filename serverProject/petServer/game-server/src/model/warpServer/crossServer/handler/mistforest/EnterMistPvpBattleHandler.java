package model.warpServer.crossServer.handler.mistforest;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battle.AbstractBattleController;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.SC_EnterFight.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_EnterMistPvpBattle;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_EnterMistPvpBattle_VALUE)
public class EnterMistPvpBattleHandler extends AbstractHandler<CS_GS_EnterMistPvpBattle> {
    @Override
    protected CS_GS_EnterMistPvpBattle parse(byte[] bytes) throws Exception {
        return CS_GS_EnterMistPvpBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_EnterMistPvpBattle req, int i) {
        LogUtil.info("recv enter mist PVP battle,playerId=" + req.getPlayerIdx());
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null || BattleManager.getInstance().isInBattle(req.getPlayerIdx())) {
            return;
        }

        AbstractBattleController controller =
                BattleManager.getInstance().createBattleController(req.getPlayerIdx(), BattleTypeEnum.BTE_PVP, BattleSubTypeEnum.BSTE_MistForest);
        controller.setBattleId(req.getBattleId());
        controller.setFightMakeId(req.getFightMakeId());
        controller.setCamp(req.getCamp());
        controller.setRandSeed(req.getRandSeed());
        controller.addAllPlayerBattleData(req.getPlayerInfoList());
        controller.addAllExtendProp(req.getExtendPropList());

        Builder enterBattleBuilder = controller.buildEnterBattleBuilder();
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_EnterFight_VALUE, enterBattleBuilder);
        BattleServerManager.getInstance().addPlayerBattleInfo(player.getIdx(), req.getBattleSvrIndex());

        controller.initTime();
        BattleManager.getInstance().managerBattle(controller);


//        SC_EnterFight.Builder builder = SC_EnterFight.newBuilder();
//        builder.setBattleId(req.getBattleId());
//        builder.setBattleType(BattleTypeEnum.BTE_PVP);
//        builder.setSubType(BattleSubTypeEnum.BSTE_MistForest);
//        builder.setFightMakeId(req.getFightMakeId());
//        builder.setCamp(req.getCamp());
//        builder.setRandSeed(req.getRandSeed());
//        builder.addAllPlayerInfo(req.getPlayerInfoList());
//        builder.addAllExtendProp(req.getExtendPropList());
//        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_EnterFight_VALUE, builder);
//
//        BattleServerManager.getInstance().addPlayerBattleInfo(player.getIdx(), req.getBattleIp());
//
//        SyncExecuteFunction.executeConsumer(player, player1 -> {
//            player1.getBattleController().setBattleId(req.getBattleId());
//            player1.getBattleController().setFightMakeId(req.getFightMakeId());
//            player1.getBattleController().setBattleType(BattleTypeEnum.BTE_PVP_VALUE);
//            player1.getBattleController().setSubBattleType(BattleSubTypeEnum.BSTE_MistForest);
//            player1.getBattleController().setCamp(req.getCamp());
//            player1.getBattleController().setBattleTimeOut(GameConst.BattleTimeout);
//            player1.getBattleController().setEnterBattleTime(GlobalTick.getInstance().getCurrentTime());
//        });
    }
}
