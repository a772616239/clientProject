package petrobot.system.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Controller;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.LogUtil;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetBagInit;
import protocol.PrepareWar.CS_BuyTeam;
import protocol.PrepareWar.CS_ChangeTeamName;
import protocol.PrepareWar.CS_ChangeTeamName.Builder;
import protocol.PrepareWar.CS_ChangeUsedTeam;
import protocol.PrepareWar.CS_UpdateTeam;
import protocol.PrepareWar.PositionPetMap;
import protocol.PrepareWar.SC_ClaimTeamsInfo;
import protocol.PrepareWar.SkillMap;
import protocol.PrepareWar.TeamInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;

@Controller
public class TeamManager {

    private static List<Integer> petNums = Stream.of(0, 0, 1, 5, 8, 41, 52, 72, 81, 95, 104, 115, 128, 145, 160).distinct().collect(Collectors.toList());


    @Index(value = IndexConst.CLAIM_TEAM_INFO)
    public void claimTeamsInfo(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_ClaimTeamsInfo_VALUE, SC_ClaimTeamsInfo.newBuilder());
    }

    @Index(value = IndexConst.BUY_TEAM)
    public void buyTeam(Robot robot) {
        if (robot == null) {
            return;
        }
        robot.getClient().send(MsgIdEnum.CS_BuyTeam_VALUE, CS_BuyTeam.newBuilder());
    }

    @Index(value = IndexConst.CHANGE_TEAM_NAME)
    public void changeTeamName(Robot robot) {
        if (robot == null) {
            return;
        }
        Builder builder = CS_ChangeTeamName.newBuilder();
        int random = new Random().nextInt(30);
        builder.setTeamNumValue(TeamTypeEnum.TTE_Common_VALUE);
        builder.setChangeName(String.valueOf(random));
        robot.getClient().send(MsgIdEnum.CS_ChangeTeamName_VALUE, builder);
    }

    //    @Index(value = IndexConst.CHANGE_USED_TEAM)
    public void changeUsedName(Robot robot) {
        if (robot == null) {
            return;
        }
        Random random = new Random();
        int updateIndex = random.nextInt(teamMap.size());
        int index = -1;
        for (TeamTypeEnum typeEnum : teamMap.keySet()) {
            if (typeEnum == TeamTypeEnum.TTE_Null || typeEnum == TeamTypeEnum.UNRECOGNIZED) {
                continue;
            }
            index++;
            if (index != updateIndex) {
                continue;
            }

            CS_ChangeUsedTeam.Builder builder = CS_ChangeUsedTeam.newBuilder();
            List<TeamNumEnum> list = teamMap.get(typeEnum);
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            builder.setUsedTeam(list.get(random.nextInt(list.size())));
            robot.getClient().send(MsgIdEnum.CS_ChangeUsedTeam_VALUE, builder);
            return;
        }
        robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }

    /**
     * 只编辑主线队伍
     *
     * @param robot
     */
    @Index(value = IndexConst.UPDATE_TEAM)
    public void updateTeam(Robot robot) {
        if (robot == null) {
            return;
        }

        SC_PetBagInit petBag = robot.getData().getPetBag();
        if (petBag == null || petBag.getPetCount() <= 0) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            LogUtil.error("robot:" + robot.getLoginName() + " pet size is empty");
            return;
        }

        List<Pet> petList = distinctPetList(petBag.getPetList());

        CS_UpdateTeam.Builder builder = CS_UpdateTeam.newBuilder();
        builder.setTeamNum(TeamNumEnum.TNE_Team_1);

        for (int i = 0; i < Math.min(3, petList.size()); i++) {
            Pet pet = petList.get(i);
            builder.addMaps(PositionPetMap.newBuilder().setPositionValue(i + 1).setPetIdx(pet.getId()).build());
        }
        robot.getClient().send(MsgIdEnum.CS_UpdateTeam_VALUE, builder);
    }

    private List<Pet> distinctPetList(List<Pet> petListData) {
        List<Pet> petList = new ArrayList<>();
        for (Pet pet : petListData) {
            if (petList.stream().noneMatch(pet1 -> pet1.getId().equals(pet.getId()))) {
                petList.add(pet);
            }
        }
        return petList;
    }

    private int setTeamPet(List<Pet> petList, int petIndex, int maxNum, CS_UpdateTeam.Builder builder) {
        for (int i = 0; i < maxNum; i++) {
            PositionPetMap.Builder petBuilder = PositionPetMap.newBuilder();
            petBuilder.setPetIdx(petList.get(petIndex).getId());
            petBuilder.setPositionValue(i);
            builder.addMaps(petBuilder);
            petIndex++;
        }
        return petIndex;
    }

    private void setTeamSkill(CS_UpdateTeam.Builder builder) {
        Random random = new Random();
        Set<Integer> skillIds = new HashSet<>();
        for (int i = 1; i <= 2; i++) {
            SkillMap.Builder skillBuilder = SkillMap.newBuilder();
            int skillId = 0;
            while (skillId == 0 || skillIds.contains(skillId)) {
                skillId = random.nextInt(6) + 1;
            }
            skillIds.add(skillId);
            skillBuilder.setSkillCfgId(skillId);
            skillBuilder.setSkillPositionValue(i);
            builder.addSkillMap(skillBuilder);

        }
    }

    private static Map<TeamTypeEnum, List<TeamNumEnum>> teamMap = new HashMap<>();
    private static final int maxTeamPetNum = 15;

    static {
        for (TeamNumEnum teamNum : TeamNumEnum.values()) {
            if (teamNum == TeamNumEnum.TNE_Team_Null || TeamNumEnum.UNRECOGNIZED == teamNum) {
                continue;
            }
            List<TeamNumEnum> teamNumEnumList = teamMap.get(getTeamType(teamNum));
            teamNumEnumList = teamNumEnumList != null ? teamNumEnumList : new ArrayList<>();
            teamNumEnumList.add(teamNum);
            teamMap.put(getTeamType(teamNum), teamNumEnumList);
        }
    }

    public static TeamTypeEnum getTeamType(TeamNumEnum teamNum) {
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            return TeamTypeEnum.TTE_Null;
        }
        if (teamNum.getNumber() >= TeamNumEnum.TNE_Team_1_VALUE && teamNum.getNumber() <= TeamNumEnum.TNE_Team_5_VALUE) {
            return TeamTypeEnum.TTE_Common;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Courge_VALUE) {
            return TeamTypeEnum.TTE_CourageTrial;
        } else if (teamNum.getNumber() >= TeamNumEnum.TNE_Mine_1_VALUE && teamNum.getNumber() <= TeamNumEnum.TNE_Mine_3_VALUE) {
            return TeamTypeEnum.TTE_Mine;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_FriendHelp_VALUE) {
            return TeamTypeEnum.TTE_FriendHelp;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Patrol_1_VALUE) {
            return TeamTypeEnum.TTE_Patrol;
        } else if (teamNum.getNumber() >= TeamNumEnum.TNE_Arena_Attack_1_VALUE
                && teamNum.getNumber() <= TeamNumEnum.TNE_Arena_Defense_3_VALUE) {
            return TeamTypeEnum.TTE_Arena;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Boss_1_VALUE) {
            return TeamTypeEnum.TTE_Boss;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_MistForest_1_VALUE) {
            return TeamTypeEnum.TTE_MistForest;
        }

        return TeamTypeEnum.TTE_Null;
    }


    private int getMaxPetNumInTeam(int playerLv) {
        for (int i = petNums.size() - 1; i >= 0; i--) {
            if (petNums.get(i) < playerLv) {
                return petNums.size() - i;
            }
        }
        return 1;
    }

    public static boolean addPetToTeamIfEmpty(Robot robot, TeamNumEnum teamNum) {
        if (robot == null || teamNum == null) {
            return false;
        }
        TeamInfo teamInfo = robot.getData().getTeamInfoByTeamNum(teamNum);
        if (teamInfo == null) {
            return false;
        }
        if (teamInfo.getPetMapCount() <= 0) {
            SC_PetBagInit petBag = robot.getData().getPetBag();
            if (petBag == null) {
                return false;
            }
            CS_UpdateTeam.Builder builder = CS_UpdateTeam.newBuilder();
            builder.setTeamNum(teamNum);
            for (int i = 0; i < Math.min(2, petBag.getPetCount()); i++) {
                builder.addMaps(PositionPetMap.newBuilder().setPositionValue(i).setPetIdx(petBag.getPet(i).getId()));
            }
            robot.getClient().send(MsgIdEnum.CS_UpdateTeam_VALUE, builder);
        }
        return true;
    }
}
