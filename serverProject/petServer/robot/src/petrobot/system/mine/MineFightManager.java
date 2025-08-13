package petrobot.system.mine;


import datatool.StringHelper;
import org.apache.commons.lang.math.RandomUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.CS_AllFriendHelpList;
import protocol.MineFight.CS_BuyExploitScroll;
import protocol.MineFight.CS_ChooseRewardType;
import protocol.MineFight.CS_ClaimMineGift;
import protocol.MineFight.CS_ClaimMineReward;
import protocol.MineFight.CS_ExitMineFight;
import protocol.MineFight.CS_JoinMineFight;
import protocol.MineFight.CS_MineFightRecord;
import protocol.MineFight.CS_MinePetForm;
import protocol.MineFight.CS_OccupyMine;
import protocol.MineFight.EnumMineState;
import protocol.MineFight.MineGiftObj;
import protocol.MineFight.MineInfo;
import protocol.MineFight.MineRewardInfo;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetBagInit;
import protocol.PrepareWar.CS_UpdateTeam;
import protocol.PrepareWar.PositionPetMap;
import protocol.PrepareWar.TeamInfo;
import protocol.PrepareWar.TeamNumEnum;

import java.util.List;
import java.util.Map.Entry;

@Controller
public class MineFightManager {

    @Index(value = IndexConst.MINE_JoinMineFight)
    public void joinMineFight(Robot robot) {
        if (robot.getData().getMineInfo() == null) {
            robot.getData().setMineInfo(new RobotMine(robot));
        }
        robot.getClient().send(MsgIdEnum.CS_JoinMineFight_VALUE, CS_JoinMineFight.newBuilder());
    }

    @Index(value = IndexConst.MINE_ExitMineFight)
    public void exitMineFight(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_ExitMineFight_VALUE, CS_ExitMineFight.newBuilder());
        SyncExecuteFunction.executeConsumer(robot, r -> {
            r.getData().getMineInfo().clear();
            r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }

    @Index(value = IndexConst.MINE_UpdateMineTeam)
    public void updateMineTeam(Robot robot) {
        SyncExecuteFunction.executeConsumer(robot, r -> {
            SC_PetBagInit petBag = r.getData().getPetBag();
            if (petBag == null || petBag.getPetCount() <= 0) {
                r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                return;
            }

            List<Pet> petList = petBag.getPetList();
            for (int i = TeamNumEnum.TNE_Mine_1_VALUE; i <= TeamNumEnum.TNE_Mine_3_VALUE; i++) {
                int petPos = i - TeamNumEnum.TNE_Mine_1_VALUE;
                if (petList.size() > petPos) {
                    CS_UpdateTeam.Builder builder = CS_UpdateTeam.newBuilder();
                    builder.setTeamNumValue(i);
                    PositionPetMap.Builder petBuilder = PositionPetMap.newBuilder();
                    petBuilder.setPetIdx(petList.get(petPos).getId());
                    petBuilder.setPositionValue(1);
                    builder.addMaps(petBuilder);
                    r.getClient().send(MsgIdEnum.CS_UpdateTeam_VALUE, builder);
                }
            }
        });
    }

    @Index(value = IndexConst.MINE_BuyExploitScroll)
    public void buyMineFight(Robot robot) {
        if (robot.getData().getMineInfo().getFreeExploitScroll() <= 0) {
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_BuyExploitScroll_VALUE, CS_BuyExploitScroll.newBuilder().setBuyNum(1));
    }

    @Index(value = IndexConst.MINE_OccupyMine)
    public void occupyMine(Robot robot) {
        if (robot.getData().getMineInfo().getMineInfoMap().isEmpty()) {
            LogUtil.error("OccupyMine Failed, mineMap is empty");
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        if (robot.getData().getBattleId() > 0) {
            LogUtil.error("OccupyMine Failed, robot is in battle");
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        int formIndex = 0;
        for (int i = TeamNumEnum.TNE_Mine_1_VALUE; i <= TeamNumEnum.TNE_Mine_3_VALUE; i++) {
            for (TeamInfo teamInfo : robot.getData().getTeamsInfo()) {
                if (teamInfo.getTeamNumValue() == i && !teamInfo.getIsLock()) {
                    formIndex = i;
                    break;
                }
            }
        }
        if (formIndex == 0) {
            LogUtil.error("OccupyMine Failed, team all battling");
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        String mineIdx = null;
        for (MineInfo.Builder mineInfo : robot.getData().getMineInfo().getMineInfoMap().values()) {
            if (mineInfo.getState() == EnumMineState.EMS_Initial || mineInfo.getState() == EnumMineState.EMS_Producting) {
                if (mineInfo.getOwnerInfo() == null || !mineInfo.getOwnerInfo().getPlayerId().equals(robot.getBaseIdx())) {
                    mineIdx = mineInfo.getId();
                    break;
                }
            }
        }
        if (StringHelper.isNull(mineIdx)) {
            LogUtil.error("OccupyMine Failed, no mine can occupy");
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_OccupyMine.Builder builder = CS_OccupyMine.newBuilder();
        builder.setMineId(mineIdx);
        builder.setFormIdx(formIndex);
        robot.getClient().send(MsgIdEnum.CS_OccupyMine_VALUE, builder);
        LogUtil.info("robot[" + robot.getLoginName() + "] try to occupy mine idx=" + mineIdx);
    }

    @Index(value = IndexConst.MINE_ChooseRewardType)
    public void chooseRewardType(Robot robot) {
        boolean choosen = false;
        for (String mineIdx : robot.getData().getMineInfo().getOwnedMineInfo().values()) {
            MineInfo.Builder mineInfo = robot.getData().getMineInfo().getMineInfoMap().get(mineIdx);
            if (mineInfo.getOwnerInfo().getPlayerId().equals(robot.getBaseIdx()) && mineInfo.getState() == EnumMineState.EMS_ChoosingReward) {
                CS_ChooseRewardType.Builder builder = CS_ChooseRewardType.newBuilder();
                builder.setMineId(mineInfo.getId());
                builder.setChooseIndex(RandomUtils.nextInt(3));
                robot.getClient().send(MsgIdEnum.CS_ChooseRewardType_VALUE, builder);
                choosen = true;
            }
        }
        if (!choosen) {
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        }
    }

    @Index(value = IndexConst.MINE_QueryMineForm)
    public void queryMineForm(Robot robot) {
        if (robot.getData().getMineInfo().getMineInfoMap().isEmpty()) {
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        int rand = RandomUtils.nextInt(robot.getData().getMineInfo().getMineInfoMap().size());
        int i = 0;
        for (MineInfo.Builder mineInfo : robot.getData().getMineInfo().getMineInfoMap().values()) {
            if (i == rand) {
                CS_MinePetForm.Builder builder = CS_MinePetForm.newBuilder();
                builder.setMineId(mineInfo.getId());
                robot.getClient().send(MsgIdEnum.CS_MinePetForm_VALUE, builder);
                return;
            }
            ++i;
        }
    }

    @Index(value = IndexConst.MINE_ClaimMineReward)
    public void caliemMineReward(Robot robot) {
        if (robot.getData().getMineInfo().getMineRewardMap().isEmpty()) {
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        for (Entry<Integer, MineRewardInfo> entry : robot.getData().getMineInfo().getMineRewardMap().entrySet()) {
            CS_ClaimMineReward.Builder builder = CS_ClaimMineReward.newBuilder();
            builder.setClaimIndex(entry.getKey());
            robot.getClient().send(MsgIdEnum.CS_ClaimMineReward_VALUE, builder);
        }
    }

    @Index(value = IndexConst.MINE_QueryMineRecord)
    public void queryMineRecord(Robot robot) {
        CS_MineFightRecord.Builder builder = CS_MineFightRecord.newBuilder();
        robot.getClient().send(MsgIdEnum.CS_MineFightRecord_VALUE, builder);
    }

    @Index(value = IndexConst.MINE_QueryFriendHelpList)
    public void queryFriendHelpList(Robot robot) {
        CS_AllFriendHelpList.Builder builder = CS_AllFriendHelpList.newBuilder();
        robot.getClient().send(MsgIdEnum.CS_AllFriendHelpList_VALUE, builder);
    }

    @Index(value = IndexConst.MINE_ApplyFriendHelp)
    public void applyFriendHelp(Robot robot) {
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }

    @Index(value = IndexConst.MINE_ClaimMineGift)
    public void claimMineGift(Robot robot) {
        if (robot.getData().getMineInfo().getMineGiftObjMap().isEmpty()) {
            SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        for (MineGiftObj giftData : robot.getData().getMineInfo().getMineGiftObjMap().values()) {
            CS_ClaimMineGift.Builder builder = CS_ClaimMineGift.newBuilder();
            builder.setGiftId(giftData.getGiftId());
            robot.getClient().send(MsgIdEnum.CS_ClaimMineGift_VALUE, builder);
        }
    }

}
