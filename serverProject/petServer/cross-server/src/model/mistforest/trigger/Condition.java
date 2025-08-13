package model.mistforest.trigger;

import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashMap;
import model.mistforest.MistConst.MistConditionType;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.buff.Buff;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.mistobj.rewardobj.MistBaseBox;
import model.mistforest.mistobj.rewardobj.MistItem;
import model.mistforest.mistobj.rewardobj.MistTreasureBag;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.MistUnitPropTypeEnum;

public class Condition {
    protected ArrayList<Integer> condParams = new ArrayList<>();

    public boolean check(MistRoom room, MistObject user, MistObject target, HashMap<Integer, Long> params) {
        if (room != null && condParams != null && !condParams.isEmpty()) {
            switch (condParams.get(0)) {
                case MistConditionType.CheckProperty:
                    if (condParams.size() > 4) {
                        int targetType = condParams.get(1);
                        int attrType = condParams.get(2);
                        int checkType = condParams.get(3);
                        long value = condParams.get(4);
                        MistObject checkObj = targetType == 0 ? user : target;
                        if (checkObj != null) {
                            long attrValue = checkObj.getAttribute(attrType);
                            return compareValue(checkType, attrValue, value);
                        }
                    }
                    break;
                case MistConditionType.CheckObjType:
                    if (condParams.size() > 3) {
                        int targetType = condParams.get(1);
                        int mistObjType = condParams.get(2);
                        boolean checkFlag = condParams.get(3) > 0;
                        MistObject checkObj = targetType == 0 ? user : target;
                        if (checkObj != null) {
                            boolean isObjType = checkObj.getType() == mistObjType;
                            return checkFlag == isObjType;
                        }
                    }
                    break;
                case MistConditionType.CheckOwnedBuff:
                    if (condParams.size() > 3) {
                        int targetType = condParams.get(1);
                        int buffId = condParams.get(2);
                        boolean checkFlag = condParams.get(3) > 0;
                        MistObject checkObj = targetType == 0 ? user : target;
                        if (checkObj != null) {
                            Buff buff = checkObj.getBufMachine().getBuff(buffId);
                            boolean hasBuff = buff != null && !buff.isBuffExpired(GlobalTick.getInstance().getCurrentTime());
                            return checkFlag == hasBuff;
                        }
                    }
                    break;
                case MistConditionType.CheckGroup:
                    if (user != null && target != null && condParams.size() > 2) {
                        int campType = condParams.get(1);
                        boolean checkFlag = condParams.get(2) > 0;
                        long userCamp = user.getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE);
                        long targetCamp = target.getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE);
                        return checkFlag == compareCamp(campType, userCamp, targetCamp);
                    }
                    break;
                case MistConditionType.CheckTargetPos:
                    if (user != null && target != null && condParams.size() > 2) {
                        int checkType = condParams.get(1);
                        int checkDistance = condParams.get(2);
                        long xDistance = user.getPos().getX() - target.getPos().getX();
                        long yDistance = user.getPos().getY() - target.getPos().getY();
                        long distance = xDistance * xDistance + yDistance * yDistance;
                        return compareValue(checkType, distance * distance, checkDistance * checkDistance);
                    }
                    break;
                case MistConditionType.CheckFixPos:
                    if (condParams.size() > 5) {
                        int targetType = condParams.get(1);
                        long posX = condParams.get(2);
                        long posY = condParams.get(3);
                        int checkType = condParams.get(4);
                        long checkDistance = condParams.get(5);

                        MistObject checkObj = targetType == 0 ? user : target;
                        if (checkObj != null) {
                            long xDistance = checkObj.getPos().getX() - posX;
                            long yDistance = checkObj.getPos().getY() - posY;
                            long distance = xDistance * xDistance + yDistance * yDistance;
                            return compareValue(checkType, distance * distance, checkDistance * checkDistance);
                        }
                    }
                    break;
                case MistConditionType.CheckItemType:
                    if (condParams.size() > 3) {
                        int targetType = condParams.get(1);
                        int itemType = condParams.get(2);
                        boolean checkFlag = condParams.get(3) > 0;
                        MistObject checkObj = targetType == 0 ? user : target;
                        if (checkObj instanceof MistItem) {
                            boolean isObjType = checkObj.getType() == itemType;
                            return checkFlag == isObjType;
                        }
                    }
                    break;
                case MistConditionType.CheckItemSkillFull:
                    if (condParams.size() > 2) {
                        int targetType = condParams.get(1);
                        boolean checkFlag = condParams.get(2) > 0;
                        MistObject checkObj = targetType == 0 ? user : target;
                        if (checkObj instanceof MistFighter) {
                            MistFighter fighter = (MistFighter) checkObj;
                            return checkFlag == fighter.getSkillMachine().isItemSkillFull();
                        }
                    }
                    break;
                case MistConditionType.CheckBeingPicking:
                    if (condParams.size() > 2 && params != null) {
                        int objType = condParams.get(1);
                        boolean checkFlag = condParams.get(2) > 0;
                        if (objType == 0 && params.containsKey(MistTriggerParamType.TreasureBoxId)) { // 宝箱
                            long boxId = params.get(MistTriggerParamType.TreasureBoxId);
                            MistBaseBox box = room.getObjManager().getMistObj(boxId);
                            if (box == null) {
                                break;
                            }
                            return checkFlag == (box.getAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE) > 0);
                        } else if (objType == 1 && params.containsKey(MistTriggerParamType.TreasureBagId)) { // 资源带
                            long bagId = params.get(MistTriggerParamType.TreasureBagId);
                            MistTreasureBag bag = room.getObjManager().getMistObj(bagId);
                            if (bag == null) {
                                break;
                            }
                            return checkFlag == (bag.getAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE) > 0);
                        } else if (objType == 2 && params.containsKey(MistTriggerParamType.ItemId)) { // 道具
                            long itemId = params.get(MistTriggerParamType.ItemId);
                            MistItem item = room.getObjManager().getMistObj(itemId);
                            if (item == null) {
                                break;
                            }
                            return checkFlag == (item.getAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE) > 0);
                        }
                    }
                    break;
                case MistConditionType.CheckProbability:
                    if (condParams.size() > 1) {
                        int rate = condParams.get(1);
                        return RandomUtils.nextInt(1000) < rate;
                    }
                    break;
                case MistConditionType.CheckImmune: {
                    if (condParams.size() > 2) {
                        int targetType = condParams.get(1);
                        boolean checkFlag = condParams.get(2) > 0;
                        MistObject checkObj = targetType == 0 ? user : target;
                        if (checkObj != null) {
                            boolean isObjType = checkObj.getAttribute(MistUnitPropTypeEnum.MUPT_IsImmuneState_VALUE) > 0;
                            return checkFlag == isObjType;
                        }
                    }
                    break;
                }
                case MistConditionType.CheckPlayerMode: {
                    if (condParams.size() > 3) {
                        int targetType = condParams.get(1);
                        int checkMode = condParams.get(2);
                        boolean checkFlag = condParams.get(3) > 0;
                        MistObject checkObj = targetType == 0 ? user : target;
                        if (checkObj instanceof MistFighter) {
                            MistFighter fighter = (MistFighter) checkObj;
                            int attackMode = (int) fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE);
                            return checkFlag ? checkMode == attackMode : checkMode != checkMode;
                        }
                    }
                    break;
                }
                case MistConditionType.CheckBeatWantedPlayerBattle: {
                    if (condParams.size() > 1) {
                        boolean checkFlag = condParams.get(1) > 0;
                        boolean ret = false;
                        if (params != null && params.containsKey(MistTriggerParamType.BeatWantedPlayerFlag)) {
                            ret = params.get(MistTriggerParamType.BeatWantedPlayerFlag) > 0;
                        }
                        return checkFlag ? ret : !ret;
                    }
                    break;
                }
                case MistConditionType.CheckIsSharedBox: {
                    if (condParams.size() > 1) {
                        boolean checkFlag = condParams.get(1) > 0;
                        boolean ret = false;
                        if (params != null && params.containsKey(MistTriggerParamType.TreasureBoxId)) {
                            long boxId = params.get(MistTriggerParamType.TreasureBoxId);
                            MistBaseBox box = room.getObjManager().getMistObj(boxId);
                            if (null == box) {
                                return false;
                            }
                            ret = box.getAttribute(MistUnitPropTypeEnum.MUPT_IsShareReward_VALUE) > 0;
                        }
                        return checkFlag ? ret : !ret;
                    }
                    break;
                }
                case MistConditionType.CheckJewelrySettle: {
                    if (condParams.size() > 1) {
                        boolean checkFlag = condParams.get(1) > 0;
                        boolean ret = false;
                        if (params != null && params.containsKey(MistTriggerParamType.SettleJewelryCount)) {
                            long jewelryCount = params.get(MistTriggerParamType.SettleJewelryCount);
                            ret = jewelryCount > 0;
                        }
                        return checkFlag ? ret : !ret;
                    }
                    break;
                }
                case MistConditionType.CheckLavaBadgeSettle: {
                    if (condParams.size() > 1) {
                        boolean checkFlag = condParams.get(1) > 0;
                        boolean ret = false;
                        if (params != null && params.containsKey(MistTriggerParamType.LavaBadgeCount)) {
                            long lavaBadgeCount = params.get(MistTriggerParamType.LavaBadgeCount);
                            ret = lavaBadgeCount > 0;
                        }
                        return checkFlag ? ret : !ret;
                    }
                    break;
                }
				case MistConditionType.CheckInScheduleSection: {
                    if (condParams.size() > 3) {
                        int targetType = condParams.get(1);
                        int scheduleType = condParams.get(2);
                        boolean checkFlag = condParams.get(3) > 0;
                        MistObject checkObj = targetType == 0 ? user : target;
                        boolean ret = room.getScheduleManager() != null && room.getScheduleManager().isFighterInClosedSection(scheduleType, checkObj.getPos().getX(), checkObj.getPos().getY());
                        return checkFlag ? ret : ! ret;
                    }
                    break;
                }
                case MistConditionType.CheckVipSkillType: {
                    if (condParams.size() > 2 && params != null && params.containsKey(MistTriggerParamType.VipSkillType)) {
                        int checkSkillType = condParams.get(1);
                        boolean checkFlag = condParams.get(2) > 0;
                        long vipSkillType = params.get(MistTriggerParamType.VipSkillType);
                        boolean ret = checkSkillType == vipSkillType;
                        return checkFlag ? ret : ! ret;
                    }
                    break;
                }
                case MistConditionType.CheckDirectSettleBattle: {
                    if (condParams.size() > 1 && params != null && params.containsKey(MistTriggerParamType.DirectSettleBattleFlag)) {
                        boolean checkFlag = condParams.get(1) > 0;
                        boolean ret = params.get(MistTriggerParamType.DirectSettleBattleFlag) > 0;
                        return checkFlag ? ret : ! ret;
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return false;
    }

    private boolean compareValue(int checkType, long destVal, long srcVal) {
        switch (checkType) {
            case 0:
                return destVal == srcVal;
            case 1:
                return destVal > srcVal;
            case 2:
                return destVal >= srcVal;
            case 3:
                return destVal < srcVal;
            case 4:
                return destVal <= srcVal;
            default:
                return false;
        }
    }

    private boolean compareCamp(int campType, long userCamp, long targetCamp) {
        if (campType == 0) {
            return userCamp != targetCamp; // 是否是敌对方
        } else if (campType == 1) {
            return userCamp == targetCamp; // 是否是友方
        } else if (campType == 2) {
            return userCamp == 0 || targetCamp == 0;
        }
        return false;
    }
}
