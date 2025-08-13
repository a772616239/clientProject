package model.patrol.entity;

import cfg.MonsterDifficulty;
import cfg.PatrolConfig;
import cfg.PatrolConfigObject;
import cfg.PatrolMap;
import cfg.PatrolMapLine;
import cfg.PatrolMapObject;
import cfg.PatrolMissionConfig;
import cfg.PatrolMissionConfigObject;
import cfg.PetBaseProperties;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.springframework.util.CollectionUtils;
import protocol.Common;
import protocol.PetMessage;
import util.LogUtil;
import util.PatrolUtil;
import util.RandomUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatrolMapHelper {

    //<PointId,pointType>
    private final Map<Integer, Integer> presetPointMap = new HashMap<>();
    //<pointId,Point>
    private final Map<Integer, PatrolTree> allPoints = new HashMap<>();

    //主线点
    private final List<PatrolTree> mainPoints = new ArrayList<>();

    //分支末端点
    private final List<Integer> branchEndPointList = new ArrayList<>();

    //已使用点
    private final List<Integer> usedPoint = new ArrayList<>();

    //玩家巡逻队数据
    private PlayerPatrolChamberData playerPatrolData;

    private int curPointId = 1;

    private final List<Integer> treasureGreedConfigData = new ArrayList<>();

    private PatrolConfigObject patrolConfig;


    /**
     * 按指定配置随机获得一个巡逻队初始地图
     *
     * @param playerId 玩家id
     * @param cfgId    patrolConfigId
     * @return 地图
     */
    public PatrolMapInitResult createPatrolTree(String playerId, int cfgId, int mapId) {
        this.patrolConfig = PatrolConfig.getById(cfgId);
        if (patrolConfig == null) {
            LogUtil.error("createPatrolTree error ,patrolConfig is null by cfgId:{}", cfgId);
        }
        PatrolMapObject mapObject = PatrolMap.getByMapid(mapId);
        LogUtil.info("patrol map init:map id=" + mapObject.getMapid());
        final PatrolTree init = new PatrolTree();
        //构成主线
        creteMainLine(mapObject, init);

        // 构成支线线路
        createBranch(mapObject, init);

        playerPatrolData = new PlayerPatrolChamberData(playerId);

        //触发巡逻队任务事件
        triggerPatrolTask(playerId);

        // 为所有点生成事件
        initAllPointEvent();

        PatrolMapInitResult result = new PatrolMapInitResult();
        result.setInitPoint(init);
        result.setMapId(mapObject.getMapid());
        result.setRuneList(playerPatrolData.getRuneList());

        playerEntity player = playerCache.getByIdx(playerId);
        if (player!=null){
            result.setArtifactAddition(player.getDb_data().getGlobalAddition().getArtifactAdditionMap());
            result.setArtifacts(player.getSimpleArtifact());
            result.setTitleIds(player.getPlayerAllTitleIds());
        }

        return result;
    }

    private void createBranch(PatrolMapObject mapObject, PatrolTree init) {
        for (int branchId : mapObject.getBranch()) {
            int[][] pointList = PatrolMapLine.getPointList(branchId);
            dealBranch(pointList);
            init.setDegree(init.getDegree() + 1);
        }
    }

    private void creteMainLine(PatrolMapObject mapObject, PatrolTree init) {
        init.setDegree(1);
        PatrolTree temp = init;
        // 构成主线线路
        int[][] mainLine = PatrolMapLine.getPointList(mapObject.getMain());
        for (int i = 0; i < mainLine.length; i++) {
            temp.setX(mainLine[i][0]);
            temp.setY(mainLine[i][1]);
            temp.setExplored(0);
            // 1是主线
            temp.setMain(1);
            temp.setId(curPointId);
            if (i == 0) {
                // 初始点设置为探索过
                temp.setExplored(1);
                putPresetPointId(temp.getId(), PatrolTree.EVENT_BEGIN_DEFAULT);
            }
            allPoints.put(curPointId, temp);
            mainPoints.add(temp);
            curPointId++;
            if (i != mainLine.length - 1) {
                if (temp.getChildList() == null) {
                    temp.setChildList(new ArrayList<>());
                }
                PatrolTree childTemp = new PatrolTree();
                childTemp.setParentPoint(temp);
                temp.getChildList().add(childTemp);
                temp = childTemp;

            } else {
                putPresetPointId(temp.getId(), PatrolTree.EVENT_BOSS);
            }

        }
    }

    public void putPresetPointId(int pointId, int pointType) {
        presetPointMap.put(pointId, pointType);
        usedPoint.add(pointId);
    }


    private void initAllPointEvent() {

        randomBranchEndPoint();

        randomSaleManPoint();

        randomBattlePoint();

        setAllPointEvent();

    }


    /**
     * 处理分支路线
     *
     * @param branch 分支配置坐标信息
     */
    public void dealBranch(int[][] branch) {
        if (CollectionUtils.isEmpty(mainPoints)) {
            LogUtil.error("mainPoint not init");
            return;
        }
        Optional<PatrolTree> branchStartPoint = mainPoints.stream().filter(mainPoint -> samePosition(branch[0], mainPoint)).findAny();
        if (!branchStartPoint.isPresent()) {
            LogUtil.error("error in RandomUtil,method dealBranch(),can not find main point for branch" + "\n");
            return;
        }
        PatrolTree temp = branchStartPoint.get();
        for (int i = 1; i < branch.length; i++) {
            temp = getChild(temp, branch[i][0], branch[i][1], i == branch.length - 1, curPointId);
            this.allPoints.put(temp.getId(), temp);
            curPointId++;
        }
        branchEndPointList.add(curPointId - 1);
    }

    private boolean samePosition(int[] branch, PatrolTree mainPoint) {
        return mainPoint.getX() == branch[0] && mainPoint.getY() == branch[1];
    }

    /**
     * 设置所有点位事件
     */
    private void setAllPointEvent() {

        for (PatrolTree patrolTree : allPoints.values()) {
            if (presetPointMap.containsKey(patrolTree.getId())) {
                setTreeDataByType(patrolTree, presetPointMap.get(patrolTree.getId()));
            } else {
                setTreeDataByType(patrolTree, PatrolUtil.getRandomPatrolType(patrolConfig));
            }
        }

    }

    /**
     * 关联传入信息与父节点
     *
     * @param father  父节点信息
     * @param x       子节点x坐标
     * @param y       子节点y坐标
     * @param lastOne 是否是最后一个子节点
     * @param id      当前点标记
     * @return 新的父节点引用
     */
    private static PatrolTree getChild(PatrolTree father, int x, int y, boolean lastOne, int id) {
        PatrolTree temp = new PatrolTree();
        temp.setX(x);
        temp.setY(y);
        temp.setExplored(0);
        // 0是支线
        temp.setMain(0);
        temp.setId(id);
        temp.setParentPoint(father);
        father.getChildList().add(temp);
        if (!lastOne) {
            temp.setChildList(new ArrayList<>());
        }
        return temp;
    }

    /**
     * 随机战斗点位
     */
    private void randomBattlePoint() {
        int count = (int) presetPointMap.values().stream().filter(type -> type == PatrolTree.EVENT_BASTARD).count();
        int battleCount = RandomUtil.getRandomValue(patrolConfig.getBattlepoint()[0], patrolConfig.getBattlepoint()[1]);
        int needCount = battleCount - count;
        // 随机战斗点位置
        for (int i = 0; i < needCount; i++) {
            int randomPoint = randomCanUsePoint();
            putPresetPointId(randomPoint, PatrolTree.EVENT_BASTARD);
        }
    }

    /**
     * 随机商人点位
     */
    private void randomSaleManPoint() {
        int index = randomCanUsePoint();
        putPresetPointId(index, PatrolTree.TRAVELING_SALESMAN);
    }

    private int randomCanUsePoint() {
        if (allPoints.size() <= 2) {
            return -1;
        }
        int index;
        do {
            index = RandomUtil.randomInScope(2, allPoints.size());
        } while (usedPoint.contains(index));
        return index;
    }

    /**
     * 随机分支末端点位类型
     */
    private void randomBranchEndPoint() {
        if (CollectionUtils.isEmpty(branchEndPointList)) {
            return;
        }

        int branchTreasureNum = patrolConfig.getBranchtreasurnum();

        List<Integer> branchTreasurePoints = RandomUtil.batchRandomFromList(branchEndPointList, branchTreasureNum, false);
        branchTreasurePoints.forEach(pointId -> putPresetPointId(pointId, PatrolTree.EVENT_TREASURE));

        List<Integer> remainPoints = branchEndPointList.stream().filter(point -> !branchTreasurePoints.contains(point)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(remainPoints)) {
            return;
        }

        Integer bossKeyPoint = RandomUtil.randomOneFromList(remainPoints);
        putPresetPointId(bossKeyPoint, PatrolTree.BOSS_KEY);
        remainPoints.remove(bossKeyPoint);

        if (CollectionUtils.isEmpty(remainPoints)) {
            return;
        }

        remainPoints.forEach(pointId -> putPresetPointId(pointId, PatrolUtil.getRandomBranchEndPatrolType()));
    }


    private void triggerPatrolTask(String playerId) {
        if (RandomUtil.getRandom1000() >= patrolConfig.getTaskprobability()) {
            return;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (target == null) {
            return;
        }
        PatrolMissionConfigObject missionConfig = PatrolMissionConfig.randomOneMission();

        SyncExecuteFunction.executeConsumer(target, cacheTemp -> {
            protocol.TargetSystemDB.DB_PatrolMission.Builder patrolMissionBuilder = target.getDb_Builder().getPatrolMissionBuilder().clear();
            assert missionConfig != null;
            protocol.TargetSystem.TargetMission.Builder mission = protocol.TargetSystem.TargetMission.newBuilder().setCfgId(missionConfig.getMissionid());
            if (missionConfig.getLimittime() > 0) {
                patrolMissionBuilder.setEndTime(GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_MIN * missionConfig.getLimittime());
            }
            target.getDb_Builder().getPatrolMissionBuilder().setMission(mission).setRewardUp(missionConfig.getRewarduprate());
        });
    }

    private void setTreeDataByType(PatrolTree tree, int patrolType) {
        tree.setPointType(patrolType);
        switch (patrolType) {
            case PatrolTree.EVENT_TREASURE:
                if (branchEndPointList.contains(tree.getId())) {
                    tree.setTreasureGreedConfig(RandomUtil.randomTreasureGreedConfig(treasureGreedConfigData));
                }
                break;

            case PatrolTree.EVENT_BASTARD:
                tree.setFightMakeId(RandomUtil.getRandomCfgBy2(MonsterDifficulty.getById(playerPatrolData.getMainLinePoint()).getPatrolconfig())[0]);
                break;
            case PatrolTree.EVENT_EXPLORE:
                // 随机三项效果放入list
                int[] statusList = new int[3];
                // 第一栏效果
                statusList[0] = RandomUtil.getRandomCfgBy2(patrolConfig.getExpolre1())[0];
                // 第二栏效果
                statusList[1] = RandomUtil.getRandomCfgBy2(patrolConfig.getExpolre2())[0];
                // 第三栏效果
                statusList[2] = RandomUtil.getRandomCfgBy2(patrolConfig.getExpolre3())[0];
                // 效果存入地图点
                tree.setExploreStatus(statusList);
                break;
            case PatrolTree.EVENT_CHAMBER:
                // 取玩家最高品质、最高等级、最高觉醒等级随机宠物
                List<PetMessage.Pet> petList = getVirtualPet(playerPatrolData.getMaxPetRarity(),
                        playerPatrolData.getMaxPetLvl(), playerPatrolData.getMaxPetUpLvl(), 4, playerPatrolData.getRuneList());
                List<PatrolPet> patrolPetList = new ArrayList<>();
                for (PetMessage.Pet pet : petList) {
                    patrolPetList.add(toPatrolPet(pet));
                }
                tree.setPetList(patrolPetList);
                break;
            case PatrolTree.EVENT_BOSS:
                // 预设bossMakeId
                tree.setFightMakeId(RandomUtil.getRandomCfgBy2(MonsterDifficulty.getById(playerPatrolData.getMainLinePoint()).getPatrolbosscfg())[0]);
                break;
            case PatrolTree.BOSS_KEY:
            case PatrolTree.EVENT_BEGIN_DEFAULT:
            case PatrolTree.TRAVELING_SALESMAN:
                break;

            default:
                LogUtil.error("setTreeDataByType error by error patrolType:{}", patrolType);
        }
    }


    public List<PetMessage.Pet> getVirtualPet(int rarity, int lvl, int upLvl, int amount, List<PetMessage.Rune> runeList) {
        List<PetMessage.Pet> result = new ArrayList<>();

        // 随机宠物 ,随机id
        List<Integer> bookIdList = new ArrayList<>();
        while (bookIdList.size() < amount) {
            int bookId = RandomUtil.getRandomHelpPet(rarity);
            if (!bookIdList.contains(bookId) && PatrolUtil.isChamberPets(bookId)) {
                bookIdList.add(bookId);
            }
        }

        for (Integer bookId : bookIdList) {
            PetMessage.Pet.Builder pet = petCache.getInstance().getPetBuilder(PetBaseProperties.getByPetid(bookId), Common.RewardSourceEnum.RSE_Patrol_VALUE);
            // 修改等级
            pet.setPetLvl(lvl).setPetRarity(rarity).setPetUpLvl(upLvl).setGemId(playerPatrolData.selectGemForChamberPet(pet.getPetBookId())).build();
            // 刷新属性
            petCache.getInstance().refreshPetData(pet, runeList, playerPatrolData.getPlayerId(), false,playerPatrolData.getPropertyAddition(),null,false);
            //神器战力加成
            pet.setAbility(pet.getAbility() + playerPatrolData.getPetAbilityAddition());
            result.add(pet.build());
        }
        return result;
    }

    private PatrolPet toPatrolPet(protocol.PetMessage.Pet pet) {
        PatrolPet patrolPet = new PatrolPet();
        patrolPet.setId(pet.getId());
        patrolPet.setPetBookId(pet.getPetBookId());
        patrolPet.setPetLvl(pet.getPetLvl());
        patrolPet.setPetRarity(pet.getPetRarity());
        patrolPet.setPetUpLvl(pet.getPetUpLvl());
        patrolPet.setAbility(pet.getAbility());
        List<PatrolPet.PetProperty> propertyList = new ArrayList<>();
        protocol.PetMessage.PetProperties petProperty = pet.getPetProperty();
        for (protocol.PetMessage.PetPropertyEntity petPropertyEntity : petProperty.getPropertyList()) {
            PatrolPet.PetProperty property = new PatrolPet.PetProperty(petPropertyEntity.getPropertyType(), petPropertyEntity.getPropertyValue());
            propertyList.add(property);
        }
        patrolPet.setGemId(pet.getGemId());
        patrolPet.setPetPropertyList(propertyList);
        return patrolPet;
    }

}
