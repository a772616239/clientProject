package model.warpServer.crossServer.handler.thewar;

import common.GlobalData;
import common.IdGenerator;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battle.AbstractBattleController;
import model.battle.BattleManager;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.SC_EnterFight.Builder;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_EnterTheWarBattle;
import util.GameUtil;
import util.LogUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@MsgId(msgId = MsgIdEnum.CS_GS_EnterTheWarBattle_VALUE)
public class EnterTheWarBattleHandler extends AbstractHandler<CS_GS_EnterTheWarBattle> {
    @Override
    protected CS_GS_EnterTheWarBattle parse(byte[] bytes) throws Exception {
        return CS_GS_EnterTheWarBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_EnterTheWarBattle req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            LogUtil.error("Recv EnterTheWarBattle msg but playerIdx =" + req.getPlayerIdx() + ", entity is null");
            return;
        }

        AbstractBattleController controller =
                BattleManager.getInstance().createBattleController(req.getPlayerIdx(), BattleTypeEnum.BTE_PVE, BattleSubTypeEnum.BSTE_TheWar);
        controller.setBattleId(IdGenerator.getInstance().generateIdNum());
        controller.setCamp(1);
        controller.setRandSeed(GameUtil.randomPositiveLong());
        controller.addFightParams(FightParamTypeEnum.FPTE_PM_BossDamage);
        controller.addFightParams(FightParamTypeEnum.FPTE_FightStar);
        controller.setFightMakeId(req.getFightMakeId());
        controller.setSkipBattle(req.getSkipBattle());

        BattlePlayerInfo.Builder playerInfo = BattlePlayerInfo.newBuilder();
        playerInfo.setCamp(1);
        playerInfo.setPlayerInfo(player.getBattleBaseData());
        List<Integer> playerExBuffs = getBuffsFromExtendPropList(req.getExtendPropList(), 1);

        playerInfo.addAllPetList(petCache.getInstance().getNewBattlePetDataListWithTeamAddition(
                player.getBattleBaseData().getLevel(), req.getSelfPetDataList(), playerExBuffs));

        playerInfo.addAllPlayerSkillIdList(req.getSelfSkillDataList());
        controller.addAllExtendProp(req.getExtendPropList());

        controller.addPlayerBattleData(playerInfo.build());

        if (!StringHelper.isNull(req.getTargetPlayerInfo().getPlayerId())) {
            BattlePlayerInfo.Builder opponentInfo = BattlePlayerInfo.newBuilder();
            opponentInfo.setCamp(2);
            opponentInfo.setPlayerInfo(req.getTargetPlayerInfo());
            List<Integer> opponentExBuffs = getBuffsFromExtendPropList(req.getExtendPropList(), 2);

            opponentInfo.addAllPetList(petCache.getInstance().getNewBattlePetDataListWithTeamAddition(
                    req.getTargetPlayerInfo().getLevel(), req.getTargetPetDataList(), opponentExBuffs));

            controller.addPlayerBattleData(opponentInfo.build());
        }

        LogUtil.info("playerIdx :{} EnterTheWarBattleHandler allRemainMonsters:{}",req.getPlayerIdx(),req.getRemainMonstersList());
        controller.addAllRemainMonsters(req.getRemainMonstersList());

        //初始化战斗时间
        controller.initTime();
        controller.putEnterParam("posX", req.getBattleGridPos().getX());
        controller.putEnterParam("posY", req.getBattleGridPos().getY());
        Builder enterBattleBuilder = controller.buildEnterBattleBuilder();
        controller.setPveEnterFightData(enterBattleBuilder);
        //移交管理
        BattleManager.getInstance().managerBattle(controller);

        GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_EnterFight_VALUE, enterBattleBuilder);
    }

    private List<Integer> getBuffsFromExtendPropList(List<Battle.ExtendProperty> extendPropList, int camp) {
        if (CollectionUtils.isEmpty(extendPropList)) {
            return Collections.emptyList();
        }
        Optional<Battle.ExtendProperty> campExProperty = extendPropList.stream().filter(extendProperty -> extendProperty.getCamp() == camp).findFirst();

        return campExProperty.map(extendProperty -> extendProperty.getBuffDataList().stream()
                .map(Battle.PetBuffData::getBuffCfgId).collect(Collectors.toList())).orElse(Collections.emptyList());

    }
}
