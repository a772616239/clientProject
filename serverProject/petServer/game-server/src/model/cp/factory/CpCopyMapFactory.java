package model.cp.factory;

import cfg.CpTeamCfg;
import cfg.CpTeamFloorCfg;
import cfg.CpTeamFloorCfgObject;
import cfg.MonsterDifficulty;
import cfg.MonsterDifficultyObject;
import common.GameConst;
import common.IdGenerator;
import common.tick.GlobalTick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.cp.CpTeamCache;
import model.cp.entity.CpCopyMap;
import model.cp.entity.CpCopyMapFloor;
import model.cp.entity.CpCopyMapPoint;
import model.cp.entity.CpTeamCopyPlayerProgress;
import model.cp.entity.CpTeamPublish;
import model.pet.PetFactory;
import model.pet.dbCache.petCache;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Battle;
import protocol.CpFunction;
import protocol.PetMessage;
import server.handler.cp.CpFunctionUtil;
import util.LogUtil;

public class CpCopyMapFactory {

    private static final List<Integer> allDifficult = Arrays.asList(1, 2, 3);


    public static CpCopyMap createMap(CpTeamPublish team) {
        CpCopyMap map = new CpCopyMap();
        map.setTeamName(team.getTeamName());
        map.setTeamId(team.getTeamId());
        initAllPointData(map, team.getTeamLv());
        initPlayerData(map, team.getMembers());
        return map;
    }

    private static void initPlayerData(CpCopyMap map, List<String> members) {
        map.setMembers(members);
        map.addAllInitPlayers(members);
        for (String member : members) {
            map.addProgress(new CpTeamCopyPlayerProgress(member, members.get(0)));
            map.updatePlayerState(member, CpFunction.CpCopyPlayerState.CCPS_Survive);
            if (!CpFunctionUtil.isRobot(member)) {
                map.playerOffline(member);
                CpTeamCache.getInstance().saveCopyPlayerOfflineTime(member, GlobalTick.getInstance().getCurrentTime());
            }
        }
    }

    private static CpCopyMap initAllPointData(CpCopyMap map, int teamLv) {
        map.setMapId(IdGenerator.getInstance().generateId());
        map.setExpireTime(GlobalTick.getInstance().getCurrentTime() + CpTeamCfg.getCopyEffectiveTime());
        int maxFloor = CpTeamFloorCfg._ix_id.keySet().stream().max(Integer::compareTo).get();
        for (int floor = 1; floor <= maxFloor; floor++) {
            CpTeamFloorCfgObject cfg = CpTeamFloorCfg.getById(floor);
            if (cfg == null) {
                LogUtil.error("CpCopyFactory createBattleFloor error floor Cfg is null by id:{}", floor);
                return null;
            }
            List<Integer> difficulty = createFloorDifficulty(cfg.getMonstercfg(), teamLv);
            if (CpFunctionUtil.isBattleFloor(floor)) {
                map.addFloor(createBattleFloor(cfg, teamLv,difficulty));
            } else {
                map.addFloor(createEventFloor(cfg, difficulty, teamLv));
            }

        }
        return map;
    }


    private static CpCopyMapFloor createEventFloor(CpTeamFloorCfgObject floorCfg, List<Integer> difficulty, int teamLv) {
        CpCopyMapFloor floorEntity = new CpCopyMapFloor();

        for (int pointIndex = 0; pointIndex < getPointNum(floorCfg, teamLv); pointIndex++) {
            int[] pointCfg = getPointCfg(floorCfg, pointIndex);
            if (pointCfg==null){
                continue;
            }
            floorEntity.addPoint(createEventPoint(pointCfg[1], floorCfg.getId(), pointIndex));
        }
        floorEntity.setFloor(floorCfg.getId());
        return floorEntity;
    }

    private static CpCopyMapPoint createEventPoint(int difficulty, int floor, int pointIndex) {
        CpCopyMapPoint cpCopyMapPoint = new CpCopyMapPoint();
        cpCopyMapPoint.setPointType(GameConst.CpCopyMapPointType.Random.getCode());
        cpCopyMapPoint.setId(CpFunctionUtil.generatePointId(floor, pointIndex));
        cpCopyMapPoint.setDifficulty(difficulty);
        cpCopyMapPoint.setParentId(CpFunctionUtil.generatePointId(floor - 1, pointIndex));
        return cpCopyMapPoint;

    }


    private static CpCopyMapFloor createBattleFloor(CpTeamFloorCfgObject floorCfg, int teamLv, List<Integer> difficulty) {
        CpCopyMapFloor floorEntity = new CpCopyMapFloor();
        for (int pointIndex = 0; pointIndex < getPointNum(floorCfg, teamLv); pointIndex++) {
            int[] cfg = getPointCfg(floorCfg, pointIndex);
            if (cfg == null) {
                continue;
            }
            floorEntity.addPoint(createBattlePoint(floorCfg, pointIndex, cfg,difficulty.get(pointIndex)));
        }
        floorEntity.setFloor(floorCfg.getId());
        return floorEntity;
    }

    private static int[] getPointCfg(CpTeamFloorCfgObject floorCfg, int pointIndex) {
        for (int[] ints : floorCfg.getMonstercfg()) {
            if (pointIndex < ints[2]) {
                return ints;
            }
            pointIndex -= ints[2];
        }
        LogUtil.error("CpCopyMapFactory getPointCfg error,can`t get point cfg,cfgId:{},point Index:{}", floorCfg.getId(), pointIndex);
        return null;
    }

    private static int getPointNum(CpTeamFloorCfgObject floorCfg, int teamLv) {
        int num = 0;
        for (int[] ints : floorCfg.getMonstercfg()) {
            if (ints[0] == teamLv) {
                num += ints[2];
            }
        }
        return num;
    }

    private static List<Integer> createFloorDifficulty(int[][] monsterCfg, int teamLv) {
        List<Integer> result = new ArrayList<>();
        for (int[] ints : monsterCfg) {
            if (ints[0] == teamLv) {
                int len =ints[2];
                for (int i = 0; i < len; i++) {
                    result.add(ints[1]);
                }
            }
        }
        return result;
    }

    private static Integer randomOneBattleDifficulty() {
        return allDifficult.get(RandomUtils.nextInt(allDifficult.size()));
    }

    private static CpCopyMapPoint createBattlePoint(CpTeamFloorCfgObject floorCfg, int pointIndex, int[] monsterCfg, int diffict) {
        CpCopyMapPoint cpCopyMapPoint = new CpCopyMapPoint();
        cpCopyMapPoint.setPointType(GameConst.CpCopyMapPointType.Battle.getCode());
        cpCopyMapPoint.setId(CpFunctionUtil.generatePointId(floorCfg.getId(), pointIndex));
        cpCopyMapPoint.setMonsters(createMonster(monsterCfg));
        cpCopyMapPoint.setDifficulty(diffict);
        List<Battle.BattlePetData> battlePetData = petCache.getInstance().buildPetBattleData(null, cpCopyMapPoint.getMonsters(),
                Battle.BattleSubTypeEnum.BSTE_LTCpTeam, false);
        long ability = battlePetData.stream().mapToLong(Battle.BattlePetData::getAbility).sum();
        cpCopyMapPoint.setAbility(ability);
        return cpCopyMapPoint;
    }

    private static List<PetMessage.Pet> createMonster(int[] monsterCfg) {
        return PetFactory.createPetList(monsterCfgToPetNeed(monsterCfg), monsterCfg[3]);
    }

    private static int[][] monsterCfgToPetNeed(int[] monsterCfg) {
        int arrayLength = (monsterCfg.length - 4) / 2;
        int[][] result = new int[arrayLength][2];
        for (int i = 4; i < monsterCfg.length; i++) {
            result[(i - 4) / 2][i % 2] = monsterCfg[i];
        }
        return result;
    }


}
