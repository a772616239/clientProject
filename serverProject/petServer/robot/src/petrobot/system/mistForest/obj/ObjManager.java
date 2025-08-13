package petrobot.system.mistForest.obj;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import petrobot.system.mistForest.MistConst;
import petrobot.system.mistForest.RobotMistForest;
import petrobot.system.mistForest.obj.dynamicobj.MistDamageMonster;
import petrobot.system.mistForest.obj.dynamicobj.MistDynamicObj;
import petrobot.system.mistForest.obj.dynamicobj.MistFighter;
import petrobot.system.mistForest.obj.dynamicobj.MistMonster;
import petrobot.system.mistForest.obj.dynamicobj.MistMoveObj;
import petrobot.system.mistForest.obj.staticobj.MistBag;
import petrobot.system.mistForest.obj.staticobj.MistBox;
import petrobot.system.mistForest.obj.staticobj.MistCage;
import petrobot.system.mistForest.obj.staticobj.MistCrystalBox;
import petrobot.system.mistForest.obj.staticobj.MistDoor;
import petrobot.system.mistForest.obj.staticobj.MistGrid;
import petrobot.system.mistForest.obj.staticobj.MistItem;
import petrobot.system.mistForest.obj.staticobj.MistRewardObj;
import petrobot.system.mistForest.obj.staticobj.MistStaticObj;
import petrobot.system.mistForest.obj.staticobj.MistTrap;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.UnitMetadata;

@Getter
@Setter
public class ObjManager {
    protected RobotMistForest robotMistForest;

    protected Map<MistUnitTypeEnum, MistObjPool> objPoolMap;

    protected Map<Long, MistObj> totalObjMap;

    protected Map<Long, MistStaticObj> staticObjMap;
    protected Map<Long, MistDynamicObj> dynamicObjMap;

    public ObjManager(RobotMistForest robotMistForest) {
        this.robotMistForest = robotMistForest;
        staticObjMap = new HashMap<>();
        totalObjMap = new HashMap<>();
        dynamicObjMap = new HashMap<>();
        objPoolMap = new HashMap<>();
    }

    public void clear() {
        totalObjMap.clear();
        staticObjMap.clear();
        dynamicObjMap.clear();
    }

    public MistObjPool generateObjPool(MistUnitTypeEnum unitType) {
        if (objPoolMap.containsKey(unitType)) {
            return null;
        }
        MistObjPool objPool = null;
        switch (unitType.getNumber()) {
            case MistUnitTypeEnum.MUT_Player_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistFighter createObj() {
                        return new MistFighter();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_Building_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistDoor createObj() {
                        return new MistDoor();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_Item_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistItem createObj() {
                        return new MistItem();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_Monster_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistMonster createObj() {
                        return new MistMonster();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_TreasureBox_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistBox createObj() {
                        return new MistBox();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_TreasureBag_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistBag createObj() {
                        return new MistBag();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_Trap_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistTrap createObj() {
                        return new MistTrap();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_Cage_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistCage createObj() {
                        return new MistCage();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_TransGrid_VALUE:
            case MistUnitTypeEnum.MUT_ChaoticGrid_VALUE:
            case MistUnitTypeEnum.MUT_BlockGrid_VALUE:
            case MistUnitTypeEnum.MUT_TreatGrid_VALUE:
            case MistUnitTypeEnum.MUT_BlinkGrid_VALUE:
            case MistUnitTypeEnum.MUT_Cave_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistGrid createObj() {
                        return new MistGrid();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_EliteMonster_VALUE:
            case MistUnitTypeEnum.MUT_MagicGuard_VALUE:
            case MistUnitTypeEnum.MUT_Boss_Slime_VALUE:
            case MistUnitTypeEnum.MUT_SlimeMonster_VALUE:
            case MistUnitTypeEnum.MUT_GuardMonster_VALUE:
            case MistUnitTypeEnum.MUT_Boss_Gargoyle_VALUE:
            case MistUnitTypeEnum.MUT_Boss_SkeletonKing_VALUE:
            case MistUnitTypeEnum.MUT_Boss_ManEater_VALUE:
            case MistUnitTypeEnum.MUT_SkeletonMonster_VALUE:
            case MistUnitTypeEnum.MUT_ManEaterMonster_VALUE:
            case MistUnitTypeEnum.MUT_ManEaterPhantom_VALUE:
            case MistUnitTypeEnum.MUT_EliteDoorKeeper_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistDamageMonster createObj() {
                        return new MistDamageMonster();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_Ghost_VALUE:
            case MistUnitTypeEnum.MUT_Wolf_VALUE:
            case MistUnitTypeEnum.MUT_Typhoon_VALUE:
            case MistUnitTypeEnum.MUT_VolcanoStone_VALUE:
            case MistUnitTypeEnum.MUT_LavaLord_VALUE:
            case MistUnitTypeEnum.MUT_DriftSand_VALUE:
            case MistUnitTypeEnum.MUT_WindSand_VALUE:
            case MistUnitTypeEnum.MUT_Goblin_VALUE:
            case MistUnitTypeEnum.MUT_GargoyleMonster_VALUE:
            case MistUnitTypeEnum.MUT_Deer_VALUE:
            case MistUnitTypeEnum.MUT_SnowBall_VALUE:
            case MistUnitTypeEnum.MUT_DustStorm_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistMoveObj createObj() {
                        return new MistMoveObj();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_CrystalBox_VALUE:
            case MistUnitTypeEnum.MUT_DecipheringBox_VALUE:
            case MistUnitTypeEnum.MUT_SealBox_VALUE:
            case MistUnitTypeEnum.MUT_MagicBox_VALUE:
            case MistUnitTypeEnum.MUT_MagicCycleBox_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistCrystalBox createObj() {
                        return new MistCrystalBox();
                    }
                });
                break;
            case MistUnitTypeEnum.MUT_Cactus_VALUE:
            case MistUnitTypeEnum.MUT_Oasis_VALUE:
            case MistUnitTypeEnum.MUT_Npc_VALUE:
            case MistUnitTypeEnum.MUT_LavaBadge_VALUE:
            case MistUnitTypeEnum.MUT_DecipheringColumn_VALUE:
            case MistUnitTypeEnum.MUT_SelfChooseBox_VALUE:
            case MistUnitTypeEnum.MUT_StrangeGrassCluster_VALUE:
            case MistUnitTypeEnum.MUT_FireCluster_VALUE:
            case MistUnitTypeEnum.MUT_SnowMan_VALUE:
            case MistUnitTypeEnum.MUT_SealColumn_VALUE:
            case MistUnitTypeEnum.MUT_BusinessMan_VALUE:
            case MistUnitTypeEnum.MUT_EventNpc_VALUE:
            case MistUnitTypeEnum.MUT_MagicCycle_VALUE:
            case MistUnitTypeEnum.MUT_MazeDoor_VALUE:
            case MistUnitTypeEnum.MUT_FateDoor_VALUE:
            case MistUnitTypeEnum.MUT_ClientUnit_VALUE:
            case MistUnitTypeEnum.MUT_CommonRewardObj_VALUE:
                objPool = new MistObjPool(new ObjCreator() {
                    @Override
                    public MistRewardObj createObj() {
                        return new MistRewardObj();
                    }
                });
                break;
        }
        if (objPool != null) {
            objPoolMap.put(unitType, objPool);
        }
        return objPool;
    }

    public MistObj addNewObj(UnitMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        long unitId = MistConst.parsePropertyLongValue(metadata.getProperties(), MistUnitPropTypeEnum.MUPT_UnitID);
        MistObjPool objPool = objPoolMap.get(metadata.getUnitType());
        if (objPool == null) {
            objPool = generateObjPool(metadata.getUnitType());
        }
        switch (metadata.getUnitTypeValue()) {
            case MistUnitTypeEnum.MUT_Player_VALUE:
                MistFighter newFighter = objPool.createObj(unitId);
                if (newFighter == null) {
                    return null;
                }
                newFighter.init(metadata);
                if (robotMistForest.getFighter() == null) {
                    long playerId = newFighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId);
                    String playerIdx = String.valueOf(playerId);
                    if (robotMistForest.getOwner().getData().getBaseInfo().getPlayerId().equals(playerIdx)) {
                        robotMistForest.setFighter(newFighter);
                    }
                }
                totalObjMap.put(unitId, newFighter);
                dynamicObjMap.put(unitId, newFighter);
                return newFighter;
            case MistUnitTypeEnum.MUT_Monster_VALUE:
                MistMonster monster = objPool.createObj(unitId);
                if (monster == null) {
                    return null;
                }
                monster.init(metadata);
                totalObjMap.put(unitId, monster);
                dynamicObjMap.put(unitId, monster);
                return monster;
            default:
                MistObj obj = objPool.createObj(unitId);
                if (obj == null) {
                    return null;
                }
                obj.init(metadata);
                totalObjMap.put(unitId, obj);
                if (obj instanceof MistDynamicObj) {
                    dynamicObjMap.put(unitId, (MistDynamicObj) obj);
                } else if (obj instanceof MistStaticObj) {
                    staticObjMap.put(unitId, (MistStaticObj) obj);
                }
                return obj;
        }
    }

    public MistObj removeObj(long id) {
        MistObj removeObj = totalObjMap.get(id);
        if (removeObj == null) {
            return null;
        }
        if (removeObj instanceof MistDynamicObj) {
            dynamicObjMap.remove(id);
        } else if (removeObj instanceof MistStaticObj) {
            staticObjMap.remove(id);
        }
        totalObjMap.remove(id);
        MistObjPool objPool = objPoolMap.get(removeObj.getUnitType());
        if (objPool != null) {
            objPool.release(removeObj);
        }
        return removeObj;
    }

    public <T extends MistObj> T getObjById(long id) {
        return (T) totalObjMap.get(id);
    }

    public <T extends MistStaticObj> T getStaticObj(long id) {
        return (T) staticObjMap.get(id);
    }

    public <T extends MistDynamicObj> T getDynamicObj(long id) {
        return (T) dynamicObjMap.get(id);
    }
}
