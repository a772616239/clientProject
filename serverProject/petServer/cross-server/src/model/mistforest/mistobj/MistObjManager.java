package model.mistforest.mistobj;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.mistforest.mistobj.activityboss.MistBossGargoyle;
import model.mistforest.mistobj.activityboss.MistBossManEater;
import model.mistforest.mistobj.activityboss.MistBossSkeletonKing;
import model.mistforest.mistobj.activityboss.MistBossSlime;
import model.mistforest.mistobj.activityboss.MistGargoyleMonster;
import model.mistforest.mistobj.activityboss.MistManEaterMonster;
import model.mistforest.mistobj.activityboss.MistManEaterPhantom;
import model.mistforest.mistobj.activityboss.MistSkeletonMonster;
import model.mistforest.mistobj.activityboss.MistSlimeMonster;
import model.mistforest.mistobj.gridobj.MistBlinkGrid;
import model.mistforest.mistobj.gridobj.MistBlockGrid;
import model.mistforest.mistobj.gridobj.MistChaoticGrid;
import model.mistforest.mistobj.gridobj.MistFateDoor;
import model.mistforest.mistobj.gridobj.MistFireCluster;
import model.mistforest.mistobj.gridobj.MistMazeDoor;
import model.mistforest.mistobj.gridobj.MistTransGrid;
import model.mistforest.mistobj.gridobj.MistTreatGrid;
import model.mistforest.mistobj.rewardobj.MistCommonRewardObj;
import model.mistforest.mistobj.rewardobj.MistCrystalBox;
import model.mistforest.mistobj.rewardobj.MistDecipheringBox;
import model.mistforest.mistobj.rewardobj.MistItem;
import model.mistforest.mistobj.rewardobj.MistMagicBox;
import model.mistforest.mistobj.rewardobj.MistMagicCycleBox;
import model.mistforest.mistobj.rewardobj.MistOptionalBox;
import model.mistforest.mistobj.rewardobj.MistSealBox;
import model.mistforest.mistobj.rewardobj.MistTreasureBag;
import model.mistforest.mistobj.rewardobj.MistTreasureBox;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistBoxCountData;
import protocol.MistForest.MistShowData;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.SC_GhostBusterGhostCount;
import protocol.MistForest.SC_UpdateMistShowData;
import util.LogUtil;

public class MistObjManager {
    private static long standardTimeStamp = 1556640000000L;   // 基准时间 2019-05-01 00:00:00
    private static long maxObjCount = 1000000L;   // 基准时间 2019-05-01 00:00:00

    private MistRoom room;
    private Map<Long, MistObject> objMap;
    private Map<Integer, Integer> objCountMap; // 用于id生成

    private Map<Integer, Integer> boxQualityCount; // 显示宝箱数量
    private Map<Integer, Integer> ghostTypeCount; // 显示鬼魂数量

    private Set<Long> fighterIds;

    private Set<Long> showTreasureFighters;
    private Set<Long> needShowObjs;

    public MistObjManager(MistRoom room) {
        this.room = room;
        this.objMap = new ConcurrentHashMap<>();
        this.objCountMap = new ConcurrentHashMap<>();
        this.boxQualityCount = new ConcurrentHashMap<>();
        this.ghostTypeCount = new ConcurrentHashMap<>();
        this.fighterIds = new HashSet<>();
        this.showTreasureFighters = new HashSet<>();
        this.needShowObjs = new HashSet<>();
    }

    public void clear() {
        room = null;
        objMap.clear();
        objCountMap.clear();
        boxQualityCount.clear();
        ghostTypeCount.clear();
        fighterIds.clear();
    }

    public <T extends MistObject> T createObj(int objType) {
        try {
            T obj = (T) createObjByType(objType, this.room);
            if (obj == null) {
                return null;
            }
            obj.init();
            obj.setAttribute(MistUnitPropTypeEnum.MUPT_UnitID_VALUE, obj.getId());
            objMap.put(obj.getId(), obj);
            if (objCountMap.containsKey(obj.getType())) {
                int count = objCountMap.get(obj.getType());
                objCountMap.put(obj.getType(), ++count);
            } else {
                objCountMap.put(obj.getType(), 1);
            }
            if (objType == MistUnitTypeEnum.MUT_Player_VALUE) {
                fighterIds.add(obj.getId());
            }
            return obj;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public long generateId(int type) {
        int count = 1;
        Integer countObj;
        countObj = objCountMap.get(type);
        if (countObj != null) {
            count = countObj + 1;
            if (count >= maxObjCount) {
                count = 1;
            }
        }

        /**
         * 最高位固定为0
         * +------------------------+---------------------------+-----------------+
         * | 39bits timestamp in ms |   16bits count  |    8bits type   |
         * +------------------------+---------------------------+-----------------+
         * @return
         */
        long timeStamp = System.currentTimeMillis() - standardTimeStamp;
        return (timeStamp << 24) + (count << 8) + type;
    }

    public void removeObj(long id) {
        MistObject obj = objMap.get(id);
        if (obj != null) {
            objMap.remove(id);
            if (fighterIds.contains(id)) {
                fighterIds.remove(id);
            }
            if (needShowObjs.contains(id)) {
                needShowObjs.remove(id);
            }
        }
    }

    public <T extends MistObject> T getMistObj(long id) {
        try {
            return objMap.containsKey(id) ? (T) objMap.get(id) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public <T extends MistObject> List<T> getMistObjList(int type) {
        List<T> objList = new ArrayList<>();
        try {
            for (MistObject obj : objMap.values()) {
                if (obj.getType() == type) {
                    objList.add((T) obj);
                }
            }

        } catch (Exception e) {

        }
        return objList;
    }

    public Set<Long> getFighterIds() {
        return fighterIds;
    }

    public List<MistFighter> getAllFighters() {
        List<MistFighter> fighterList = new ArrayList<>();
        for (Long id : fighterIds) {
            MistFighter fighter = getMistObj(id);
            if (fighter == null) {
                continue;
            }
            fighterList.add(fighter);
        }
        return fighterList;
    }

    private MistObject createObjByType(int objType, MistRoom room) {
        switch (objType) {
            case MistUnitTypeEnum.MUT_Player_VALUE:
                return new MistFighter(room, objType);
            case MistUnitTypeEnum.MUT_Building_VALUE:
                return new MistDoor(room, objType);
            case MistUnitTypeEnum.MUT_Item_VALUE:
                return new MistItem(room, objType);
            case MistUnitTypeEnum.MUT_Monster_VALUE:
                return new MistMonster(room, objType);
            case MistUnitTypeEnum.MUT_Key_VALUE:
                return new MistKey(room, objType);
            case MistUnitTypeEnum.MUT_TreasureBox_VALUE:
                return new MistTreasureBox(room, objType);
            case MistUnitTypeEnum.MUT_TreasureBag_VALUE:
                return new MistTreasureBag(room, objType);
            case MistUnitTypeEnum.MUT_Trap_VALUE:
                return new MistTrap(room, objType);
            case MistUnitTypeEnum.MUT_PosObj_VALUE:
                return new MistPosObj(room, objType);
            case MistUnitTypeEnum.MUT_Cage_VALUE:
                return new MistCage(room, objType);
            case MistUnitTypeEnum.MUT_TransGrid_VALUE:
                return new MistTransGrid(room, objType);
            case MistUnitTypeEnum.MUT_ChaoticGrid_VALUE:
                return new MistChaoticGrid(room, objType);
            case MistUnitTypeEnum.MUT_BlockGrid_VALUE:
                return new MistBlockGrid(room, objType);
            case MistUnitTypeEnum.MUT_TreatGrid_VALUE:
                return new MistTreatGrid(room, objType);
            case MistUnitTypeEnum.MUT_BlinkGrid_VALUE:
                return new MistBlinkGrid(room, objType);
            case MistUnitTypeEnum.MUT_Ghost_VALUE:
                return new MistGhost(room, objType);
            case MistUnitTypeEnum.MUT_SelfChooseBox_VALUE:
                return new MistOptionalBox(room, objType);
            case MistUnitTypeEnum.MUT_Goblin_VALUE:
                return new MistGoblin(room, objType);
            case MistUnitTypeEnum.MUT_EliteMonster_VALUE:
                return new MistEliteMonster(room, objType);
            case MistUnitTypeEnum.MUT_MagicGuard_VALUE:
                return new MistMagicGuard(room, objType);
            case MistUnitTypeEnum.MUT_GuardMonster_VALUE:
                return new MistGuardMonster(room, objType);
            case MistUnitTypeEnum.MUT_Boss_Slime_VALUE:
                return new MistBossSlime(room, objType);
            case MistUnitTypeEnum.MUT_SlimeMonster_VALUE:
                return new MistSlimeMonster(room, objType);
            case MistUnitTypeEnum.MUT_Boss_Gargoyle_VALUE:
                return new MistBossGargoyle(room, objType);
            case MistUnitTypeEnum.MUT_GargoyleMonster_VALUE:
                return new MistGargoyleMonster(room, objType);
            case MistUnitTypeEnum.MUT_Boss_SkeletonKing_VALUE:
                return new MistBossSkeletonKing(room, objType);
            case MistUnitTypeEnum.MUT_SkeletonMonster_VALUE:
                return new MistSkeletonMonster(room, objType);
            case MistUnitTypeEnum.MUT_Boss_ManEater_VALUE:
                return new MistBossManEater(room, objType);
            case MistUnitTypeEnum.MUT_ManEaterMonster_VALUE:
                return new MistManEaterMonster(room, objType);
            case MistUnitTypeEnum.MUT_ManEaterPhantom_VALUE:
                return new MistManEaterPhantom(room, objType);
            case MistUnitTypeEnum.MUT_EliteDoorKeeper_VALUE:
                return new MistEliteDoorKeeper(room, objType);
            case MistUnitTypeEnum.MUT_PoisonousMushroom_VALUE:
                return new MistPoisonMushroom(room, objType);
            case MistUnitTypeEnum.MUT_Cave_VALUE:
                return new MistCave(room, objType);
            case MistUnitTypeEnum.MUT_StrangeGrassCluster_VALUE:
                return new MistStrangeGrass(room, objType);
            case MistUnitTypeEnum.MUT_DriftSand_VALUE:
                return new MistDriftSand(room, objType);
            case MistUnitTypeEnum.MUT_FireCluster_VALUE:
                return new MistFireCluster(room, objType);
            case MistUnitTypeEnum.MUT_SnowBall_VALUE:
                return new MistSnowBall(room, objType);
            case MistUnitTypeEnum.MUT_Typhoon_VALUE:
                return new MistTyphoon(room, objType);
            case MistUnitTypeEnum.MUT_Wolf_VALUE:
                return new MistWolf(room, objType);
            case MistUnitTypeEnum.MUT_Deer_VALUE:
                return new MistDeer(room, objType);
            case MistUnitTypeEnum.MUT_Cactus_VALUE:
                return new MistCactus(room, objType);
            case MistUnitTypeEnum.MUT_Oasis_VALUE:
                return new MistOasis(room, objType);
            case MistUnitTypeEnum.MUT_Npc_VALUE:
                return new MistNpc(room, objType);
            case MistUnitTypeEnum.MUT_WindSand_VALUE:
                return new MistWindSand(room, objType);
            case MistUnitTypeEnum.MUT_LavaBadge_VALUE:
                return new MistLavaBadge(room, objType);
            case MistUnitTypeEnum.MUT_Volcano_VALUE:
                return new MistVolcano(room, objType);
            case MistUnitTypeEnum.MUT_VolcanoStone_VALUE:
                return new MistVolcanoStone(room, objType);
            case MistUnitTypeEnum.MUT_LavaLord_VALUE:
                return new MistLavaLord(room, objType);
            case MistUnitTypeEnum.MUT_Mushroom_VALUE:
                return new MistMushroom(room, objType);
            case MistUnitTypeEnum.MUT_LavaMonster_VALUE:
                return new MistLavaMonster(room, objType);
            case MistUnitTypeEnum.MUT_DecipheringBox_VALUE:
                return new MistDecipheringBox(room, objType);
            case MistUnitTypeEnum.MUT_DecipheringColumn_VALUE:
                return new MistDecipheringColumn(room, objType);
            case MistUnitTypeEnum.MUT_SealBox_VALUE:
                return new MistSealBox(room, objType);
            case MistUnitTypeEnum.MUT_SealColumn_VALUE:
                return new MistSealColumn(room, objType);
            case MistUnitTypeEnum.MUT_MagicBox_VALUE:
                return new MistMagicBox(room, objType);
            case MistUnitTypeEnum.MUT_BusinessMan_VALUE:
                return new MistBusinessMan(room, objType);
            case MistUnitTypeEnum.MUT_CrystalBox_VALUE:
                return new MistCrystalBox(room, objType);
            case MistUnitTypeEnum.MUT_MagicCycle_VALUE:
                return new MistMagicCycle(room, objType);
            case MistUnitTypeEnum.MUT_MagicCycleBox_VALUE:
                return new MistMagicCycleBox(room, objType);
            case MistUnitTypeEnum.MUT_DustStorm_VALUE:
                return new MistDustStorm(room, objType);
            case MistUnitTypeEnum.MUT_MazeDoor_VALUE:
                return new MistMazeDoor(room, objType);
            case MistUnitTypeEnum.MUT_ClientUnit_VALUE:
                return new MistClientUnit(room, objType);
            case MistUnitTypeEnum.MUT_FateDoor_VALUE:
                return new MistFateDoor(room, objType);
            case MistUnitTypeEnum.MUT_CommonRewardObj_VALUE:
                return new MistCommonRewardObj(room, objType);
            default:
                break;
        }
        return null;
    }

    public void addBoxQualityCount(int boxQuality) {
        if (boxQuality <= 0) {
            return;
        }
        boxQualityCount.merge(boxQuality, 1, (oldVal, newVal) -> oldVal + newVal);
//        SC_UpdateBoxRemainCount.Builder builder = SC_UpdateBoxRemainCount.newBuilder();
//        builder.setBoxCountData(getMistBoxQualityCount());
//        room.broadcastMsg(MsgIdEnum.SC_UpdateBoxRemainCount_VALUE, builder, true);
    }

    public void minusBoxQualityCount(int boxQuality) {
        if (boxQuality <= 0) {
            return;
        }
        boxQualityCount.merge(boxQuality, 1, (oldVal, newVal) -> {
            int val = oldVal - newVal;
            return val > 0 ? val : null;
        });
//        SC_UpdateBoxRemainCount.Builder builder = SC_UpdateBoxRemainCount.newBuilder();
//        builder.setBoxCountData(getMistBoxQualityCount());
//        room.broadcastMsg(MsgIdEnum.SC_UpdateBoxRemainCount_VALUE, builder, true);
    }

    public MistBoxCountData.Builder getMistBoxQualityCount() {
        MistBoxCountData.Builder builder = MistBoxCountData.newBuilder();
        boxQualityCount.forEach((quality, count)->{
            builder.addQuality(quality);
            builder.addCount(count);
        });
        return builder;
    }

    public void addGhostTypeCount(int ghostType) {
        if (ghostType <= 0 || room.getMistRule() != EnumMistRuleKind.EMRK_GhostBuster_VALUE) {
            return;
        }
        ghostTypeCount.merge(ghostType, 1, (oldVal, newVal) -> oldVal + newVal);
        room.broadcastMsg(MsgIdEnum.SC_GhostBusterGhostCount_VALUE, getGhostTypeCount(), true);
    }

    public void minusGhostTypeCount(int ghostType) {
        if (ghostType <= 0  || room.getMistRule() != EnumMistRuleKind.EMRK_GhostBuster_VALUE) {
            return;
        }
        ghostTypeCount.merge(ghostType, 1, (oldVal, newVal) -> {
            int val = oldVal - newVal;
            return val > 0 ? val : null;
        });
        room.broadcastMsg(MsgIdEnum.SC_GhostBusterGhostCount_VALUE, getGhostTypeCount(), true);
    }

    public SC_GhostBusterGhostCount.Builder getGhostTypeCount() {
        SC_GhostBusterGhostCount.Builder builder = SC_GhostBusterGhostCount.newBuilder();
        ghostTypeCount.forEach((quality, count)->{
            builder.addGhostType(quality);
            builder.addGhostCount(count);
        });
        return builder;
    }

    public void addShowTreasureFighter(long fighterId) {
        if (!showTreasureFighters.contains(fighterId)) {
            showTreasureFighters.add(fighterId);
        }
    }

    public void removeShowTreasureFighter(long fighterId) {
        if (showTreasureFighters.contains(fighterId)) {
            showTreasureFighters.remove(fighterId);
        }
    }

    public List<MistShowData> getAllShowObjs() {
        if (CollectionUtils.isEmpty(needShowObjs)) {
            return null;
        }
        MistObject obj;
        MistShowData.Builder builder = MistShowData.newBuilder();
        List<MistShowData> dataList = new ArrayList<>();
        for (Long objId : needShowObjs) {
            obj = getMistObj(objId);
            if (obj == null || !obj.isAlive()) {
                continue;
            }
            builder.setTargetId(objId);
            builder.setTargetType(obj.getType());
            builder.setPos(obj.getPos());
            dataList.add(builder.build());
        }
        return dataList;
    }

    public void addNeedShowObj(MistObject obj) {
        needShowObjs.add(obj.getId());
        if (!needShowObjs.isEmpty()) {
            SC_UpdateMistShowData.Builder builder = SC_UpdateMistShowData.newBuilder();
            builder.setUpdateType(1); // 增量更新
            MistShowData.Builder showData = MistShowData.newBuilder();
            showData.setTargetId(obj.getId());
            showData.setTargetType(obj.getType());
            showData.setPos(obj.getPos());
            builder.addShowData(showData);
            room.broadcastToPartFighters(MsgIdEnum.SC_UpdateMistShowData_VALUE, builder, showTreasureFighters,true);
        }
    }

    public void removeNeedShowObj(long objId) {
        if (needShowObjs.contains(objId)) {
            needShowObjs.remove(objId);

            if (!showTreasureFighters.isEmpty()) {
                SC_UpdateMistShowData.Builder builder = SC_UpdateMistShowData.newBuilder();
                builder.setUpdateType(2); // 移除
                MistShowData.Builder showData = MistShowData.newBuilder();
                showData.setTargetId(objId);
                builder.addShowData(showData);
                room.broadcastToPartFighters(MsgIdEnum.SC_UpdateMistShowData_VALUE, builder, showTreasureFighters,true);
            }
        }
    }

    public void onTick(long curTime) {
        for (MistObject obj : objMap.values()) {
            obj.onTick(curTime);
        }
    }
}
