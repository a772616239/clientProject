package model.arena;

import cfg.ArenaDan;
import cfg.ArenaDanObject;
import cfg.ArenaRobotConfig;
import cfg.ArenaRobotConfigObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetRarityConfig;
import common.IdGenerator;
import java.time.Instant;
import model.arena.entity.ArenaTotalInfo;
import model.arena.util.ArenaPetUtil;
import model.arena.util.ArenaUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Arena.ArenaPlayerTeamInfo;
import protocol.ArenaDB.DB_ArenaDefinedTeamsInfo;
import protocol.ArenaDB.DB_ArenaPlayerBaseInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.Battle;
import protocol.Battle.BattlePetData;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import protocol.PrepareWar.TeamNumEnum;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author huhan
 * @date 2020/05/14
 */
public class ArenaRobotManager {
    private static ArenaRobotManager instance;

    public static ArenaRobotManager getInstance() {
        if (instance == null) {
            synchronized (ArenaRobotManager.class) {
                if (instance == null) {
                    instance = new ArenaRobotManager();
                }
            }
        }
        return instance;
    }

    /**
     * 新房间创建时初始化的机器人比例
     */
    private static final float ROOM_ROBOT_INIT_RATE = 0.5F;

    private ArenaRobotManager() {
    }

    /**
     * 此方法用于新房间创建时调用创建机器人
     * 机器人需要保存状态
     *
     * @param dan
     * @return
     */
    public List<ArenaTotalInfo> initRoomRobot(String roomId, int dan) {
        long initStart = Instant.now().toEpochMilli();

        List<ArenaRobotConfigObject> robotCfgList = ArenaUtil.getRobotCfgByDanId(dan);
        if (CollectionUtils.isEmpty(robotCfgList)) {
            LogUtil.error("ArenaRobotManager.initRoomRobot, can not find dan robot cfg, dan:" + dan);
            return null;
        }

        List<ArenaTotalInfo> result = new ArrayList<>();
        //先生成部分机器人
        for (ArenaRobotConfigObject robotCfg : robotCfgList) {
            int needCount = (int) (robotCfg.getNeedcount() * ROOM_ROBOT_INIT_RATE);
            List<ArenaTotalInfo> robots = createRobot(roomId, robotCfg, needCount);
            if (CollectionUtils.isEmpty(robots)) {
                LogUtil.error("ArenaRobotManager.initRoomRobot, can not create robot, cfg:" + robotCfg + ", need count:" + needCount);
                continue;
            }
            result.addAll(robots);
        }

        LogUtil.debug("model.arena.ArenaRobotManager.initRoomRobot, use time:" + (Instant.now().toEpochMilli() - initStart));
        return result;
    }

    private ArenaTotalInfo createRobot(String roomId, ArenaRobotConfigObject robotCfg) {
        if (robotCfg == null || StringUtils.isBlank(roomId)) {
            LogUtil.error("ArenaRobotManager.createRobot, params error, roomId :" + roomId + ",robotConfig:" + robotCfg);
            return null;
        }

        PetBasePropertiesObject avatarAndDisPetCfg = randomGetPet(robotCfg.getDan());
        if (avatarAndDisPetCfg == null) {
            LogUtil.error("random get avatarAndDisPetCfg failed, dan:" + robotCfg.getDan());
            return null;
        }

        Map<Integer, ArenaPlayerTeamInfo> teamInfoMap = initTeamsInfo(robotCfg);
        if (MapUtils.isEmpty(teamInfoMap)) {
            LogUtil.error("ArenaRobotManager.createRobot, init teams info failed:" + robotCfg.toString());
            return null;
        }

        //初始化机器人等级,取宠物的最高等级
        long fightAbility = 0;
        int maxPetLv = 0;
        for (ArenaPlayerTeamInfo value : teamInfoMap.values()) {
            for (BattlePetData battlePetData : value.getPetsList()) {
                fightAbility += battlePetData.getAbility();
                if (battlePetData.getPetLevel() > maxPetLv) {
                    maxPetLv = battlePetData.getPetLevel();
                }
            }
        }

        String playerIdx = IdGenerator.getInstance().generateId();

        DB_ArenaPlayerInfo.Builder builder = DB_ArenaPlayerInfo.newBuilder();
        DB_ArenaPlayerBaseInfo.Builder baseInfo = DB_ArenaPlayerBaseInfo.newBuilder();
        baseInfo.setPlayerIdx(playerIdx);
        baseInfo.setAvatar(avatarAndDisPetCfg.getUnlockhead());
        baseInfo.setName(robotCfg.getName());
        baseInfo.setLevel(maxPetLv);
        baseInfo.setFightAbility(fightAbility);
        baseInfo.setShowPetId(avatarAndDisPetCfg.getPetid());
        baseInfo.setTitleId(ArenaUtil.getDanLinkTitleId(robotCfg.getDan()));
        builder.setBaseInfo(baseInfo);

        builder.setRobotCfgId(robotCfg.getId());
        builder.setDan(robotCfg.getDan());
        builder.setScore(ArenaUtil.randomInScope(robotCfg.getStartscore(), robotCfg.getEndscore()));
        builder.setRoomId(roomId);


        DB_ArenaDefinedTeamsInfo.Builder teamsBuilder = DB_ArenaDefinedTeamsInfo.newBuilder();
        teamsBuilder.setPlayerIdx(playerIdx);
        teamsBuilder.putAllDefinedTeams(teamInfoMap);

        ArenaTotalInfo totalInfo = new ArenaTotalInfo();
        totalInfo.setArenaPlayerInfo(builder.build());
        totalInfo.setDefinedTeams(teamsBuilder.build());
        return totalInfo;
    }

    /**
     * 根据参数随机机器人随机机器人
     *
     * @return
     */
    public Map<Integer, ArenaPlayerTeamInfo> createRobotTeamsInfo(int robotCfgId) {
        return initTeamsInfo(ArenaRobotConfig.getById(robotCfgId));
    }

    /**
     * 根据参数随机机器人随机机器人
     *
     * @param roomId
     * @param needCount
     * @return
     */
    public List<ArenaTotalInfo> createRobot(String roomId, ArenaRobotConfigObject robotCfg, int needCount) {
        if (StringUtils.isBlank(roomId) || robotCfg == null || needCount <= 0) {
            LogUtil.error("ArenaRobotManager.createRobot, error params, roomId:" + roomId + ",robotCfg:" + robotCfg + ", needCount:" + needCount);
            return null;
        }

        List<ArenaTotalInfo> result = new ArrayList<>();
        for (int i = 0; i < needCount; i++) {
//            long startTime = Instant.now().toEpochMilli();
            ArenaTotalInfo robot = createRobot(roomId, robotCfg);
//            LogUtil.debug("create one robot time :" + (Instant.now().toEpochMilli() - startTime));
            if (robot != null) {
                result.add(robot);
            }
        }
        return result;
    }

    /**
     * 初始化消毒信息
     *
     * @return
     */
    private Map<Integer, ArenaPlayerTeamInfo> initTeamsInfo(ArenaRobotConfigObject robotCfg) {
        if (robotCfg == null) {
            return null;
        }

        //首先查询需要初始化的小队
        List<Integer> teams = ArenaUtil.getDanUseDefinedTeams(robotCfg.getDan());
        if (CollectionUtils.isEmpty(teams)) {
            LogUtil.error("dan use defined team is cfg error, empty, dan:" + robotCfg.getDan());
            return null;
        }

        Map<Integer, ArenaPlayerTeamInfo> result = new HashMap<>();
        for (Integer team : teams) {
            ArenaPlayerTeamInfo.Builder builder = ArenaPlayerTeamInfo.newBuilder();
            builder.setTeanNumValue(team);

            List<Pet> teamsPet = new ArrayList<>();
            //初始化宠物 第一位是品质 第二位是需要数量
            for (int[] ints : robotCfg.getPetcount()) {
                if (ints.length < 2) {
                    LogUtil.error("ust pet cfg error, dan:" + robotCfg.getDan() + Arrays.toString(ints));
                    continue;
                }

                for (int i = 0; i < ints[1]; i++) {
                    Pet.Builder petBuilder = initRobotPet(robotCfg, ints[0], team);
                    if (petBuilder != null) {
                        teamsPet.add(petBuilder.build());
                    }
                }
            }

            List<BattlePetData> battlePetData = ArenaPetUtil.buildPetBattleData(Battle.BattleSubTypeEnum.BSTE_Arena,teamsPet,false);
            if (CollectionUtils.isNotEmpty(battlePetData)) {
                builder.addAllPets(battlePetData);
            }

            result.put(builder.getTeanNumValue(), builder.build());
        }
        return result;
    }

    private Pet.Builder initRobotPet(ArenaRobotConfigObject robotCfg, int petQuality, int teamIndex) {
        if (robotCfg == null) {
            return null;
        }
        int petCfgId = ArenaManager.getInstance().randomPet(robotCfg.getDan(), petQuality);
        Builder petBuilder = ArenaPetUtil.getPetBuilder(petCfgId);
        if (petBuilder == null) {
            LogUtil.error("can not create pet, pet book id :" + petCfgId);
            return null;
        }
        petBuilder.setPetLvl(randomRobotLv(robotCfg, teamIndex));
        petBuilder.setPetRarity(PetRarityConfig.getRarity(petCfgId, petBuilder.getPetLvl()));
        petBuilder.setPetUpLvl(ArenaUtil.randomInScope(robotCfg.getPetwakeuprange()));
        return ArenaPetUtil.refreshPetDataWithoutRune(petBuilder);
    }

    private int randomRobotLv(ArenaRobotConfigObject robotCfg, int teamIndex) {
        if (robotCfg == null) {
            return 1;
        }
        int[] randomRange = robotCfg.getPetlvrange();
        if (teamIndex == TeamNumEnum.TNE_Arena_Defense_2_VALUE) {
            randomRange = robotCfg.getPetlvrange2();
        } else if (teamIndex == TeamNumEnum.TNE_Arena_Defense_3_VALUE) {
            randomRange = robotCfg.getPetlvrange3();
        }
        return ArenaUtil.randomInScope(randomRange);
    }


    /**
     * 根据段位配置随机获得一个宠物配置
     *
     * @param dan
     * @return
     */
    private PetBasePropertiesObject randomGetPet(int dan) {
        ArenaDanObject danCfg = ArenaDan.getById(dan);
        if (danCfg == null) {
            LogUtil.error("ArenaRobotManager.randomGetPet, dan cfg is not exist, dan:" + dan);
            return null;
        }
        int[] petPool = danCfg.getRobotpetpool();
        if (petPool.length <= 0) {
            return null;
        }
        return PetBaseProperties.getByPetid(petPool[new Random().nextInt(petPool.length)]);
    }
}