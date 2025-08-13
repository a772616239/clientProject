package petrobot.system.thewar;

import java.util.Map;
import java.util.Random;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.system.thewar.map.grid.WarMapGrid;
import petrobot.system.thewar.room.WarRoom;
import petrobot.tick.GlobalTick;
import petrobot.util.SyncExecuteFunction;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_EnterFight;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.TheWar.CS_AddNewPetToWar;
import protocol.TheWar.CS_BuyStamia;
import protocol.TheWar.CS_ClaimAfkReward;
import protocol.TheWar.CS_ClearOwnedGrid;
import protocol.TheWar.CS_EnterTheWar;
import protocol.TheWar.CS_PromoteJobTile;
import protocol.TheWar.CS_QueryWarTeam;
import protocol.TheWar.CS_StationTroops;
import protocol.TheWar.CS_UpdateWarTeamPet;
import protocol.TheWar.StationTroopsInfo;
import protocol.TheWar.WarPetPosData;
import protocol.TheWarDefine.CS_QueryWarGridList;
import protocol.TheWarDefine.CS_QueryWarPetData;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.WarPetData;
import protocol.TheWarDefine.WarTeamType;

@Controller
public class TheWarManager {
    @Index(value = IndexConst.TheWar_EnterWarRoom)
    public void enterWarRoom(Robot robot) {
        robot.getClient().send(MsgIdEnum.CS_EnterTheWar_VALUE, CS_EnterTheWar.newBuilder());
    }

    @Index(value = IndexConst.TheWar_QueryAllWarPet)
    public void queryAllWarPet(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_QueryWarPetData.Builder builder = CS_QueryWarPetData.newBuilder();
        builder.setQueryAllPet(true);
        robot.getClient().send(MsgIdEnum.CS_QueryWarPetData_VALUE, builder);
    }

    @Index(value = IndexConst.TheWar_AddPetToWar)
    public void addPetToWar(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        Map<String, WarPetData> warPetMap = robot.getData().getRobotWarData().getPlayerData().getPlayerPetsMap();
        if (warPetMap != null && warPetMap.size() >= 2) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        Random random = new Random();
        Pet pet = robot.getData().getPetBag().getPet(random.nextInt(robot.getData().getPetBag().getPetCount()));
        if (pet == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        Long expireTime = robot.getData().getRobotWarData().getPlayerData().getBanedPetsMap().get(pet.getId());
        if (expireTime != null && GlobalTick.getInstance().getCurrentTime() > expireTime) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_AddNewPetToWar.Builder builder = CS_AddNewPetToWar.newBuilder();
        WarPetPosData.Builder posBuilder = WarPetPosData.newBuilder().setPetIdx(pet.getId()).setIndexToAdd(random.nextInt(10));
        builder.addPetPosData(posBuilder);
        robot.getClient().send(MsgIdEnum.CS_AddNewPetToWar_VALUE, builder);
    }

    @Index(value = IndexConst.TheWar_QueryWarTeam)
    public void queryWarTeam(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_QueryWarTeam_VALUE, CS_QueryWarTeam.newBuilder());
    }

    @Index(value = IndexConst.TheWar_UpdatePetTeam)
    public void updatePetTeam(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
//        WarTeamData teamData = robot.getData().getRobotWarData().getPlayerData().getTeamDbData().getTeamDataMap().get(WarTeamType.WTT_AttackTeam_VALUE);
        CS_UpdateWarTeamPet.Builder builder = CS_UpdateWarTeamPet.newBuilder();
        builder.setTeamType(WarTeamType.WTT_AttackTeam);
        int index = 0;
        for (String petIdx : robot.getData().getRobotWarData().getPlayerData().getPlayerPetsMap().keySet()) {
            if (index >= 2) {
                break;
            }
            builder.getPetInfoBuilder().addPos(index).addPetIdx(petIdx);
            ++index;
        }
        if (builder.getPetInfo().getPetIdxCount() <= 0) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        } else {
            robot.getClient().send(MsgIdEnum.CS_UpdateWarTeamPet_VALUE, builder);
        }
    }

    @Index(value = IndexConst.TheWar_QueryGridList)
    public void queryGridList(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_QueryWarGridList.Builder builder = CS_QueryWarGridList.newBuilder();
        int queryCount = new Random().nextInt(50);
        for (Position pos : warRoom.getWarMap().getGridMap().keySet()) {
            if (queryCount <= 0) {
                break;
            }
            builder.addQueryWarGrids(pos);
            --queryCount;
        }
        robot.getClient().send(MsgIdEnum.CS_QueryWarGridList_VALUE, builder);
    }

    @Index(value = IndexConst.TheWar_AttackGrid)
    public void attackGrid(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }

        Position pos = robot.getData().getRobotWarData().getAvailableAttackPos();
        if (pos == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        pos = warRoom.getAvailableAttackPos(robot.getData().getRobotWarData().getCamp(), pos);
        if (pos == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_EnterFight.Builder builder = CS_EnterFight.newBuilder();
        builder.setType(BattleSubTypeEnum.BSTE_TheWar);
        builder.addParamList(String.valueOf(pos.getX()));
        builder.addParamList(String.valueOf(pos.getY()));
        robot.getClient().send(MsgIdEnum.CS_EnterFight_VALUE, builder);
    }

    @Index(value = IndexConst.TheWar_StationTroopsGrid)
    public void troopsGrid(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        String petIdx = "";
        for (WarPetData warPetData : robot.getData().getRobotWarData().getPlayerData().getPlayerPetsMap().values()) {
            if (warPetData.getStationIndex() < 0) {
                petIdx = warPetData.getPetId();
                break;
            }
        }
        WarMapGrid grid = null;
        for (Position pos : robot.getData().getRobotWarData().getPlayerData().getOwnedGridPosList()) {
            grid = warRoom.getWarMap().getMapGridByPos(pos);
            if (grid == null || grid.isBlock()) {
                continue;
            }
            Long ownerId = grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
            if (ownerId != null && ownerId.toString().equals(robot.getData().getBaseInfo().getPlayerId())) {
                break;
            }
        }
        if (grid == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_StationTroops.Builder builder = CS_StationTroops.newBuilder();
        StationTroopsInfo.Builder troopsBuilder = StationTroopsInfo.newBuilder();
        troopsBuilder.setPetIdx(petIdx);
        troopsBuilder.setCellPos(grid.getPos());
        troopsBuilder.setIndex(new Random().nextInt(10));
        builder.addTroopsInfo(troopsBuilder);
        robot.getClient().send(MsgIdEnum.CS_StationTroops_VALUE, builder);
    }

    @Index(value = IndexConst.TheWar_BuyStamina)
    public void buyStamina(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_BuyStamia.Builder builder = CS_BuyStamia.newBuilder();
        robot.getClient().send(MsgIdEnum.CS_BuyStamia_VALUE, builder);
    }

    @Index(value = IndexConst.TheWar_PromoteJobTile)
    public void promoteJobTile(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_PromoteJobTile.Builder builder = CS_PromoteJobTile.newBuilder();
        robot.getClient().send(MsgIdEnum.CS_PromoteJobTile_VALUE, builder);
    }

    @Index(value = IndexConst.TheWar_ClearOwnedGrid)
    public void clearOwnedGrid(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        // 概率清除
        if (new Random().nextInt(10) > 7) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        int index = new Random().nextInt(robot.getData().getRobotWarData().getPlayerData().getOwnedGridPosCount());
        Position pos = robot.getData().getRobotWarData().getPlayerData().getOwnedGridPos(index);
        if (pos == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_ClearOwnedGrid.Builder builder = CS_ClearOwnedGrid.newBuilder();
        builder.setPos(pos);
        robot.getClient().send(MsgIdEnum.CS_ClearOwnedGrid_VALUE, builder);
    }

    @Index(value = IndexConst.TheWar_GainAfkReward)
    public void gainAfkReward(Robot robot) {
        if (robot.getData().getRobotWarData() == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        WarRoom warRoom = WarRoomCache.getInstance().getWarRoomByIdx(robot.getData().getRobotWarData().getWarRoomIdx());
        if (warRoom == null) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (curTime - robot.getData().getRobotWarData().getPlayerData().getLastSettleAfkTime() <= 5 * 60 * 1000) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, rob -> rob.getData().getRobotWarData().getPlayerData().setLastSettleAfkTime(curTime));
        CS_ClaimAfkReward.Builder builder = CS_ClaimAfkReward.newBuilder();
        robot.getClient().send(MsgIdEnum.CS_ClaimAfkReward_VALUE, builder);
    }
}
