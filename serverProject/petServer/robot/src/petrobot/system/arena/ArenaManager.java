package petrobot.system.arena;

import org.apache.commons.collections4.CollectionUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Arena.ArenaOpponent;
import protocol.Arena.CS_BuyArenaChallengeItem;
import protocol.Arena.CS_ClaimArenaInfo;
import protocol.Arena.CS_ClaimArenaOpponentTotalInfo;
import protocol.Arena.CS_ClaimArenaRanking;
import protocol.Arena.CS_ClaimArenaRecords;
import protocol.Arena.SC_ClaimArenaInfo.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetBagInit;
import protocol.PrepareWar.CS_UpdateTeam;
import protocol.PrepareWar.PositionPetMap;
import protocol.PrepareWar.TeamNumEnum;

import java.util.List;
import java.util.Random;

/**
 * @author huhan
 * @date 2020/05/28
 */
@Controller
public class ArenaManager {
    @Index(value = IndexConst.ARENA_UPDATE_TEAM)
    public void updateArenTem(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            SC_PetBagInit petBag = robot.getData().getPetBag();
            CS_UpdateTeam.Builder builder = CS_UpdateTeam.newBuilder();
            builder.setTeamNum(TeamNumEnum.TNE_Arena_Attack_1);
            for (int i = 0; i < Math.min(2, petBag.getPetCount()); i++) {
                builder.addMaps(PositionPetMap.newBuilder().setPositionValue(i).setPetIdx(petBag.getPet(i).getId()));
            }
            robot.getClient().send(MsgIdEnum.CS_UpdateTeam_VALUE, builder);
        });
    }

    @Index(value = IndexConst.ARENA_CLAIM_ARENA_INFO)
    public void claimArenaInfo(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimArenaInfo_VALUE, CS_ClaimArenaInfo.newBuilder());
    }

    @Index(value = IndexConst.ARENA_CLAIM_RANKING)
    public void claimArenaRanking(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_ClaimArenaRanking_VALUE, CS_ClaimArenaRanking.newBuilder());
    }

    @Index(value = IndexConst.ARENA_CLAIM_RECORDS)
    public void claimArenaRecords(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_ClaimArenaRecords_VALUE, CS_ClaimArenaRecords.newBuilder());
    }

    @Index(value = IndexConst.ARENA_TOTAL_INFO)
    public void claimTotalInfo(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, robot1 -> {
            Builder info = robot.getData().getArenaInfo();
            if (info == null || info.getOpponnentInfoCount() <= 0) {
                robot.getClient().send(MsgIdEnum.CS_ClaimArenaInfo_VALUE, CS_ClaimArenaInfo.newBuilder());
                return;
            }

            List<ArenaOpponent> infoList = info.getOpponnentInfoList();
            if (CollectionUtils.isEmpty(infoList)){
                return;
            }
            ArenaOpponent opponent = infoList.get(new Random().nextInt(infoList.size()));
            robot.getClient().send(MsgIdEnum.CS_ClaimArenaOpponentTotalInfo_VALUE,
                    CS_ClaimArenaOpponentTotalInfo.newBuilder().setPlayerIdx(opponent.getSimpleInfo().getPlayerIdx()));
        });
    }

    @Index(value = IndexConst.ARENA_BUY_ITEM)
    public void buyItem(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, robot1 -> {
            CS_BuyArenaChallengeItem.Builder builder = CS_BuyArenaChallengeItem.newBuilder();
            builder.setBuyCount(1);

            robot.getClient().send(MsgIdEnum.CS_BuyArenaChallengeItem_VALUE, builder);
        });
    }
}
