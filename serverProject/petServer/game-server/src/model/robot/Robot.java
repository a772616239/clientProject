package model.robot;

import lombok.Getter;
import lombok.Setter;
import model.pet.dbCache.petCache;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.PlayerBaseInfo;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetDisplayInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author huhan
 * @date 2020/03/17
 */
@Getter
@Setter
public class Robot {
    private String idx;
    private int shortId;
    private String name;
    private int avatar;
    private int level;
    private int vipLv;
    /**
     * 竞技场分数推荐区间上限
     **/
    private int upArenaRecommend;
    /**
     * 竞技场分数推荐区间下限
     **/
    private int lowArenaRecommend;
    private final Random random = new Random();

    private int arenaScore;

    List<Pet> pets = new ArrayList<>();
    List<Integer> skills = new ArrayList<>();

    public void addPet(Pet pet) {
        if (pet == null || pets.size() >= 15) {
            return;
        }
        pets.add(pet);
    }

    public void addAllSkill(int[] skill) {
        if (skill == null || skill.length <= 0) {
            return;
        }
        for (int i : skill) {
            skills.add(i);
            if (skills.size() >= 2) {
                return;
            }
        }
    }

//    public OpponentSimpleInfo buildArenaOpponentSimpleInfo() {
//        Builder builder = OpponentSimpleInfo.newBuilder();
//        builder.setIdx(getIdx());
//        builder.setAvatar(getAvatar());
//        builder.setName(getName());
//        builder.setLv(getLevel());
//        builder.setRanking(-1);
//        builder.setScore(randomArenaScore());
//        builder.setFightAbility(fightAbility());
//        return builder.build();
//    }

    public int randomArenaScore() {
        int anInt = random.nextInt(this.upArenaRecommend - this.lowArenaRecommend);
        if (random.nextBoolean()) {
            anInt = -anInt;
        }
        this.arenaScore = anInt + this.lowArenaRecommend;
        return arenaScore;
    }

    public long fightAbility() {
        long sum = 0L;
        for (Pet pet : pets) {
            sum += pet.getAbility();
        }
        return sum;
    }

//    public static Robot createNewRobot(RobotCfgObject cfg) {
//        if (cfg == null) {
//            return null;
//        }
//
//        Robot robot = new Robot();
//        robot.setIdx(String.valueOf(cfg.getId()));
//        robot.setShortId(cfg.getId());
//        robot.setName(ObjUtil.createRandomName(LanguageEnum.LE_SimpleChinese));
//        robot.setAvatar(cfg.getAvatar());
//        robot.setLevel(cfg.getLv());
//        robot.setVipLv(cfg.getViplv());
//        robot.addAllSkill(cfg.getTeamskill());
//        int[] arenaScoreRange = cfg.getArenascorerange();
//        robot.setUpArenaRecommend(ArrayUtil.getMaxInt(arenaScoreRange, 1000));
//        robot.setLowArenaRecommend(ArrayUtil.getMinInt(arenaScoreRange, 1000));
//
//        int[] teamPet = cfg.getTeampet();
//        if (teamPet == null || teamPet.length <= 0) {
//            LogUtil.warn("robot team pet is not cfg, robot id = " + robot.getIdx());
//        }
//
//        for (int robotPetId : teamPet) {
//            RobotPetCfgObject robotPetCfg = RobotPetCfg.getById(robotPetId);
//            if (robotPetCfg == null) {
//                LogUtil.error("robot pet cfg is not exist, robotPetId = " + robotPetId);
//                continue;
//            }
//            PetBasePropertiesObject petCfg = PetBaseProperties.getByPetid(robotPetCfg.getBookid());
//            Pet.Builder builder = petCache.getInstance().getPetBuilder(petCfg, 0);
//            if (builder == null) {
//                continue;
//            }
//
//            builder.setPetLvl(Math.min(petCfg.getMaxlvl(), robotPetCfg.getLv()));
//            builder.setPetStar(Math.min(petCfg.getMaxstar(), robotPetCfg.getStar()));
//            builder.setPetUpLvl(Math.min(petCfg.getMaxuplvl(), robotPetCfg.getAwake()));
//            Pet resultPet = petCache.getInstance().refreshPetData(builder, null).build();
//            robot.addPet(resultPet);
//        }
//        return robot;
//    }

    public List<BattlePetData> buildBattlePetData(BattleSubTypeEnum subType) {
        return petCache.getInstance().buildPlayerPetBattleData(getIdx(), pets, subType);
    }

    public PlayerBaseInfo.Builder getBattleBaseData() {
        PlayerBaseInfo.Builder builder = PlayerBaseInfo.newBuilder();
        builder.setPlayerId(getIdx());
        builder.setPlayerName(getName());
        builder.setLevel(getLevel());
        builder.setAvatar(getAvatar());
        builder.setVipLevel(getVipLv());
        return builder;
    }

/*    public List<PetDisplayInfo> getPetDisMsg() {
        List<PetDisplayInfo> result = new ArrayList<>();
        for (Pet pet : pets) {
            PetDisplayInfo build = PetDisplayInfo.newBuilder().setPetIdx(pet.getId()).setPetBookId(pet.getPetBookId()).setPetEvolveLv(pet.getEvolveLv())
                    .setPetLevel(pet.getPetLvl()).setPetAbility(pet.getPetRarity()).setPetAbility(pet.getAbility()).build();
            result.add(build);
        }
        return result;
    }*/
}
