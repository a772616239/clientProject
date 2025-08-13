package model.warpServer.crossServer.handler.mistforest;

import common.GlobalData;
import common.IdGenerator;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battle.AbstractBattleController;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.ExtendProperty;
import protocol.Battle.SC_EnterFight.Builder;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_EnterMistPveBattle;
import protocol.ServerTransfer.EnumMistPveBattleType;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_EnterMistPveBattle_VALUE)
public class EnterMistPveBattleHandler extends AbstractHandler<CS_GS_EnterMistPveBattle> {
    @Override
    protected CS_GS_EnterMistPveBattle parse(byte[] bytes) throws Exception {
        return CS_GS_EnterMistPveBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_EnterMistPveBattle req, int i) {
        LogUtil.info("recv enter mist pve battle,playerId=" + req.getIdx());
        playerEntity player = playerCache.getByIdx(req.getIdx());
        if (player == null || BattleManager.getInstance().isInBattle(req.getIdx())) {
            return;
        }

        AbstractBattleController controller =
                BattleManager.getInstance().createBattleController(req.getIdx(), BattleTypeEnum.BTE_PVE,  BattleSubTypeEnum.BSTE_MistForest);
        controller.setBattleId(IdGenerator.getInstance().generateIdNum());
        controller.setCamp(1);
        controller.setRandSeed(GameUtil.randomPositiveLong());
        controller.addAllPlayerBattleData(req.getPlayerInfoList());
        controller.setFightMakeId(req.getFightMakeId());
        controller.putEnterParam("monsterCfgId", req.getMonsterCfgId());
        if (req.getBuffDataCount() > 0) {
            ExtendProperty.Builder propBuilder = ExtendProperty.newBuilder();
            propBuilder.setCamp(controller.getCamp());
            propBuilder.addAllBuffData(req.getBuffDataList());
            controller.addExtendProp(propBuilder.build());
        }

        controller.putEnterParam("pveType", req.getPveTypeValue());
        if (req.getPveType() == EnumMistPveBattleType.EMPBT_EliteMonsterBattle) {
            controller.addFightParams(FightParamTypeEnum.FPTE_PM_BossDamage);
        }

        Builder enterBattleBuilder = controller.buildEnterBattleBuilder();
        controller.setPveEnterFightData(enterBattleBuilder);

        //初始化时间并移交给battleManager
        controller.initTime();
        BattleManager.getInstance().managerBattle(controller);

        GlobalData.getInstance().sendMsg(req.getIdx(), MsgIdEnum.SC_EnterFight_VALUE, enterBattleBuilder);
    }
}
