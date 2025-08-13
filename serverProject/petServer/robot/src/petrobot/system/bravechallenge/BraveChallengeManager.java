package petrobot.system.bravechallenge;

import java.util.Random;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.LogUtil;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.CS_EnterFight;
import protocol.BraveChallenge.BravePoint;
import protocol.BraveChallenge.CS_BraveChallengeInit;
import protocol.BraveChallenge.CS_BraveChallengeReborn;
import protocol.BraveChallenge.CS_BraveClaimPointRewards;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetBagInit;
import protocol.PrepareWar.CS_UpdateTeam;
import protocol.PrepareWar.PositionPetMap;
import protocol.PrepareWar.TeamNumEnum;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@Controller
public class BraveChallengeManager {

    @Index(IndexConst.BRAVE_CHALLENGE_INIT)
    public void init(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_BraveChallengeInit_VALUE, CS_BraveChallengeInit.newBuilder());
    }

    @Index(IndexConst.BRAVE_UPDATE_TEAM)
    public void updateTeam(Robot robot) {
        SC_PetBagInit petBag = robot.getData().getPetBag();
        if (petBag == null || petBag.getPetCount() <= 0) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            LogUtil.error("robot:" + robot.getLoginName() + " pet size is empty");
            return;
        }

        CS_UpdateTeam.Builder builder = CS_UpdateTeam.newBuilder();
        builder.setTeamNum(TeamNumEnum.TNE_Courge);

        for (int i = 0; i < Math.min(3, petBag.getPetCount()); i++) {
            Pet pet = petBag.getPetList().get(i);
            builder.addMaps(PositionPetMap.newBuilder().setPositionValue(i + 1).setPetIdx(pet.getId()).build());
        }
        robot.getClient().send(MsgIdEnum.CS_UpdateTeam_VALUE, builder);
    }

    @Index(IndexConst.BRAVE_CHALLENGE_BATTLE)
    public void battle(Robot robot) {
        if (robot.getData().getBraveChallengeProgress() != null) {
            int nextProgress = robot.getData().getBraveChallengeProgress().getProgress() + 1;
            BravePoint nextPoint = null;
            for (BravePoint bravePoint : robot.getData().getBraveChallengeProgress().getPointList()) {
                if (bravePoint.getPointId() == nextProgress) {
                    nextPoint = bravePoint;
                    break;
                }
            }

            if (nextPoint == null) {
                robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                return;
            }

            if (new Random().nextBoolean()) {
                robot.getClient().send(MsgIdEnum.CS_EnterFight_VALUE,
                        CS_EnterFight.newBuilder().addParamList(String.valueOf(robot.getData().getBraveChallengeProgress().getProgress() + 1))
                                .setType(BattleSubTypeEnum.BSTE_BreaveChallenge));
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                robot.getClient().send(MsgIdEnum.CS_BattleResult_VALUE,
                        CS_BattleResult.newBuilder().setIsGMEnd(true).setBattleId(robot.getData().getBattleId()).setWinnerCamp(1));
                robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            } else {
                robot.getClient().send(MsgIdEnum.CS_BraveClaimPointRewards_VALUE,
                        CS_BraveClaimPointRewards.newBuilder().setPointId(nextProgress));
            }
        }
    }

    @Index(IndexConst.BRAVE_REBORN)
    public void reborn(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_BraveChallengeReborn_VALUE, CS_BraveChallengeReborn.newBuilder());
    }
}
