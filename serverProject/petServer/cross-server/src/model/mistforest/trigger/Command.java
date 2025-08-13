package model.mistforest.trigger;

import cfg.MistMazeAreaConfig;
import cfg.MistMazeAreaConfigObject;
import cfg.MistSkillConfig;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalData;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.mistforest.MistConst.MistBossKeyState;
import model.mistforest.MistConst.MistBuffInterruptType;
import model.mistforest.MistConst.MistCammondType;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.ai.RobotController;
import model.mistforest.formula.Formula;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.mistobj.MistCage;
import model.mistforest.mistobj.MistDoor;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistGhost;
import model.mistforest.mistobj.MistKey;
import model.mistforest.mistobj.MistLavaBadge;
import model.mistforest.mistobj.MistMonster;
import model.mistforest.mistobj.MistObject;
import model.mistforest.mistobj.MistTrap;
import model.mistforest.mistobj.MistVolcano;
import model.mistforest.mistobj.activityboss.MistActivityBoss;
import model.mistforest.mistobj.rewardobj.MistBaseBox;
import model.mistforest.mistobj.rewardobj.MistCommonRewardObj;
import model.mistforest.mistobj.rewardobj.MistItem;
import model.mistforest.mistobj.rewardobj.MistTreasureBag;
import model.mistforest.room.entity.MistRoom;
import model.mistforest.team.MistTeam;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistTaskTargetType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.SC_UpdateMistActivityBossState;
import protocol.ServerTransfer.CS_GS_LeaveMistForest;
import protocol.ServerTransfer.CS_GS_ReqChangeMistStamina;
import protocol.ServerTransfer.CS_GS_UpdateMistMazeRecord;
import protocol.ServerTransfer.EnumMistPveBattleType;
import protocol.TransServerCommon.MistBornPosInfo;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

public class Command {
    protected ArrayList<Integer> cmdParams = new ArrayList<>();

    public boolean ExecuteCmd(MistRoom room, MistObject user, MistObject target, HashMap<Integer, Long> params) {
        if (room != null && cmdParams != null && !cmdParams.isEmpty()) {
            switch (cmdParams.get(0)) {
                case MistCammondType.ChangeProperty: {
                    if (cmdParams.size() > 8) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int attrType = cmdParams.get(3);
                        int calcType = cmdParams.get(4);
                        int formulaId = cmdParams.get(5);
                        int param1 = cmdParams.get(6);
                        int param2 = cmdParams.get(7);
                        int param3 = cmdParams.get(8);
                        List<Integer> paramList = new ArrayList<>();
                        paramList.add(param1);
                        paramList.add(param2);
                        paramList.add(param3);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            int changeValue = Formula.calculate(formulaId, user, realTarget, paramList);
                            long attrValue = realTarget.getAttribute(attrType);
                            if (calcType == 0) {
                                attrValue = changeValue;
                            } else if (calcType == 1) {
                                attrValue += changeValue;
                            } else if (calcType == 2) {
                                attrValue += attrValue * changeValue / 1000;
                            }
                            realTarget.setAttribute(attrType, attrValue);
                            realTarget.addAttributeChangeCmd(attrType, attrValue);
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeBuff: {
                    if (cmdParams.size() > 4) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeType = cmdParams.get(3);
                        int buffId = cmdParams.get(4);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (changeType == 0) {
                                long buffTime = cmdParams.size() > 5 ? cmdParams.get(5) : 0;
                                if (buffTime > 0) {
                                    if (params == null) {
                                        params = new HashMap<>();
                                    }
                                    params.put(MistTriggerParamType.BuffTime, buffTime);
                                }
                                realTarget.getBufMachine().addBuff(buffId, user, params);
                            } else if (changeType == 1) {
                                realTarget.getBufMachine().removeBuff(buffId);
                            } else if (changeType == 2) {
                                realTarget.getBufMachine().addBuffWithPause(buffId, user, params);
                            } else if (changeType == 3) {
                                realTarget.getBufMachine().pauseBuffById(buffId);
                            } else if (changeType == 4) {
                                realTarget.getBufMachine().resumeBuffById(buffId);
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.CreateTrap: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int trapType = cmdParams.get(3);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject creator : objects) {
                            long camp = creator.getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE);
                            MistTrap newObj = room.getObjManager().createObj(MistUnitTypeEnum.MUT_Trap_VALUE);
                            newObj.setAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE, camp);
                            newObj.setAttribute(MistUnitPropTypeEnum.MUPT_CreatorId_VALUE, creator.getId());
                            newObj.setAttribute(MistUnitPropTypeEnum.MUPT_TrapType_VALUE, trapType);
                            newObj.afterInit(new int[]{creator.getPos().getX(), creator.getPos().getY()}, new int[]{creator.getToward().getX(), creator.getToward().getY()});
                            room.getWorldMap().objFirstEnter(newObj);
                            if (creator instanceof MistFighter) {
                                MistFighter fighter = (MistFighter) creator;
                                fighter.addDropTrap(newObj.getId());
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.RemoveTrap: {
                    if (params != null && params.containsKey(MistTriggerParamType.TrapId)) {
                        long removeObjId = params.get(MistTriggerParamType.TrapId);
                        MistTrap trap = room.getObjManager().getMistObj(removeObjId);
                        if (trap != null) {
                            trap.dead();
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangePos: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changePosType = cmdParams.get(3);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            boolean isPrivateObj = realTarget.getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE) > 0;
                            ProtoVector.Builder newPosBuilder = null;
                            switch (changePosType) {
                                case 0:
                                case 1: {
                                    boolean toSafeRegion = changePosType == 1;
                                    MistBornPosInfo posObj = room.getObjGenerator().getRandomBornPosObj(realTarget.getType(), toSafeRegion, isPrivateObj);
                                    if (posObj != null) {
                                        newPosBuilder = ProtoVector.newBuilder();
                                        newPosBuilder.mergeFrom(posObj.getPos());
                                    }
                                    break;
                                }
                                case 2: {
                                    if (params != null) {
                                        Long posData = params.get(MistTriggerParamType.TranPosData);
                                        if (posData != null) {
                                            newPosBuilder = ProtoVector.newBuilder();
                                            newPosBuilder.setX(GameUtil.getHighLong(posData)).setY(GameUtil.getLowLong(posData));
                                        } else {
                                            MistBornPosInfo posObj = room.getObjGenerator().getRandomBornPosObj(realTarget.getType(), true, isPrivateObj);
                                            if (posObj != null) {
                                                newPosBuilder = ProtoVector.newBuilder();
                                                newPosBuilder.mergeFrom(posObj.getPos());
                                            }
                                        }
                                    }
                                    break;
                                }
                                default:
                                    break;
                            }
                            if (newPosBuilder != null) {
                                int oldX = realTarget.getPos().getX();
                                int oldY = realTarget.getPos().getY();
                                realTarget.setPos(newPosBuilder.build());
                                if (!isPrivateObj && realTarget.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) == 0) {
                                    room.getWorldMap().objMove(realTarget, oldX, oldY);
                                }
                                if (realTarget.getAttribute(MistUnitPropTypeEnum.MUPT_IsRobotPlayer_VALUE) > 0) {
                                    RobotController robotController = ((MistFighter) realTarget).getRobController();
                                    robotController.resetPath();
                                }
                                realTarget.addChangePosInfoCmd(realTarget.getPos().build(), realTarget.getToward().build());
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeSpeedRate: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeRate = cmdParams.get(3);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            int rate = realTarget.getSpeedChangeRate();
                            rate += changeRate;
                            realTarget.setSpeedChangeRate(rate);
                            realTarget.addAttributeChangeCmd(
                                    MistUnitPropTypeEnum.MUPT_Speed_VALUE, realTarget.calcRealSpeed());
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.AddItemSkill: {
                    if (cmdParams.size() > 2 && params != null
                            && params.containsKey(MistTriggerParamType.ItemId)) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        long itemId = params.get(MistTriggerParamType.ItemId);
                        MistItem item = room.getObjManager().getMistObj(itemId);
                        if (item == null) {
                            break;
                        }
                        int itemType = (int) item.getAttribute(MistUnitPropTypeEnum.MUPT_ItemType_VALUE);
                        int itemSkillId = MistSkillConfig.getSkillByItemType(itemType);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter fighter = (MistFighter) realTarget;
                                int index = fighter.getSkillMachine().addItemSkill(itemSkillId);
                                if (index >= 0) {
                                    fighter.sendUpdateItemSkillCmd(true);
                                    fighter.getNpcTask().doNpcTask(MistTaskTargetType.MTTT_GainItem_VALUE, 1, 0);
                                }
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.RemovePickedItem: {
                    if (params != null
                            && params.containsKey(MistTriggerParamType.ItemId)) {
                        long itemId = params.get(MistTriggerParamType.ItemId);
                        MistItem item = room.getObjManager().getMistObj(itemId);
                        if (item != null) {
                            item.dead();
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.EnterPveBattle: {
                    if (user instanceof MistFighter) {
                        MistFighter fighter = (MistFighter) user;
                        if (target instanceof MistMonster) {
                            MistMonster monster = (MistMonster) target;
                            if (fighter.enterPveBattle(EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE, target)) {
                                monster.enterBattle(fighter, GlobalTick.getInstance().getCurrentTime());
                            }
                            break;
                        }
                        fighter.enterPveBattle(EnumMistPveBattleType.EMPBT_BossBattle_VALUE, target);
                        return true;
                    }
                    break;
                }
                case MistCammondType.EnterPvpBattle: {
                    if (user instanceof MistFighter && target instanceof MistFighter) {
                        MistFighter userFighter = (MistFighter) user;
                        MistFighter targetFighter = (MistFighter) target;
                        room.onPlayerEnterPvpBattle(userFighter, targetFighter);
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeMistFrostLevel: {
                    if (user instanceof MistFighter && params != null && params.containsKey(MistTriggerParamType.DoorId)) {
                        MistFighter fighter = (MistFighter) user;
                        long doorId = params.get(MistTriggerParamType.DoorId);
                        MistDoor door = room.getObjManager().getMistObj(doorId);
                        if (door == null) {
                            break;
                        }
                        fighter.sendOpenDoorCmd((int) door.getAttribute(MistUnitPropTypeEnum.MUPT_ExitToLevel_VALUE));
                        return true;
                    }
                    break;
                }
                case MistCammondType.GainTreasureBox: {
                    if (user instanceof MistFighter && params != null && params.containsKey(MistTriggerParamType.TreasureBoxId)) {
                        MistFighter fighter = (MistFighter) user;
                        long boxId = params.get(MistTriggerParamType.TreasureBoxId);
                        MistBaseBox box = room.getObjManager().getMistObj(boxId);
                        if (box == null) {
                            break;
                        }
                        int needStamina = (int) box.getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
                        if (needStamina > 0) {
                            MistPlayer owner = fighter.getOwnerPlayerInSameRoom();
                            if (owner == null || owner.isRobot()) {
                                break;
                            }
                            if (owner.getMistStamina() < needStamina) {
                                break;
                            }
                            CS_GS_ReqChangeMistStamina.Builder builder = CS_GS_ReqChangeMistStamina.newBuilder();
                            builder.setPlayerIdx(owner.getIdx());
                            builder.setChangeValue(-needStamina);
                            GlobalData.getInstance().sendMsgToServer(owner.getServerIndex(), MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE, builder);
                        }
                        box.gainReward(fighter);
                        return true;
                    }
                    break;
                }
                case MistCammondType.RemoveGhost: {
                    if (params != null && params.containsKey(MistTriggerParamType.GhostObjId)) {
                        long ghostId = params.get(MistTriggerParamType.GhostObjId);
                        MistGhost ghost = room.getObjManager().getMistObj(ghostId);
                        if (ghost == null) {
                            break;
                        }
                        ghost.dead();
                        return true;
                    }
                    break;
                }
                case MistCammondType.Blink: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int distance = cmdParams.get(3);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            realTarget.addBlinkCmd(distance);
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeBattleBuff: {
                    if (cmdParams.size() > 4) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeType = cmdParams.get(3);
                        int battleBuffId = cmdParams.get(4);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter fighter = (MistFighter) realTarget;
                                if (changeType > 0) {
                                    fighter.addBattleBuff(battleBuffId);
                                } else if (changeType < 0) {
                                    fighter.removeBattleBuff(battleBuffId);
                                } else {
                                    fighter.clearBattleBuff(battleBuffId);
                                }
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeDefenceRate: {
                    if (cmdParams.size() > 3) {
//                        int cmdObjType = cmdParams.get(1);
//                        int cmdObjParam = cmdParams.get(2);
//                        int changeRate = cmdParams.get(3);
//                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
//                        for (MistObject realTarget : objects) {
//                            MistFighter fighter = (MistFighter) realTarget;
//                            int damageRate = fighter.getDefenceChangeRate();
//                            damageRate += changeRate;
//                            fighter.setDefenceChangeRate(damageRate);
//                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.RemoveKey: {
                    if (params != null && params.containsKey(MistTriggerParamType.KeyId)) {
                        long removeObjId = params.get(MistTriggerParamType.KeyId);
                        MistKey key = room.getObjManager().getMistObj(removeObjId);
                        if (key != null) {
                            key.dead();
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.RemoveBox: {
                    if (params != null && params.containsKey(MistTriggerParamType.TreasureBoxId)) {
                        long removeObjId = params.get(MistTriggerParamType.TreasureBoxId);
                        MistBaseBox box = room.getObjManager().getMistObj(removeObjId);
                        if (box != null) {
                            box.dead();
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.RemoveBag: {
                    if (params != null && params.containsKey(MistTriggerParamType.TreasureBagId)) {
                        long removeObjId = params.get(MistTriggerParamType.TreasureBagId);
                        MistTreasureBag bag = room.getObjManager().getMistObj(removeObjId);
                        if (bag != null) {
                            if (user instanceof MistFighter) {
                                bag.beAbsorbed();
                            } else {
                                bag.dead();
                            }

                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.BroadcastTips: {
                    if (cmdParams.size() > 2) {
                        int broadcastType = cmdParams.get(1);
                        int tipsType = cmdParams.get(2);
                        Object[] paramList = null;
                        if (tipsType == EnumMistTipsType.EMTT_PickupBox_VALUE && params != null && params.containsKey(MistTriggerParamType.TreasureBoxId)) {
                            paramList = new Object[1];
                            long boxId = params.get(MistTriggerParamType.TreasureBoxId);
                            MistBaseBox box = room.getObjManager().getMistObj(boxId);
                            if (box == null) {
                                break;
                            }
                            paramList[0] = box.getAttribute(MistUnitPropTypeEnum.MUPT_BoxRewardId_VALUE);
                        } else if (tipsType == EnumMistTipsType.EMTT_TouchTrap_VALUE && params != null && params.containsKey(MistTriggerParamType.TrapId)) {
                            paramList = new Object[1];
                            long trapId = params.get(MistTriggerParamType.TrapId);
                            MistTrap trap = room.getObjManager().getMistObj(trapId);
                            if (trap == null) {
                                break;
                            }
                            paramList[0] = trap.getAttribute(MistUnitPropTypeEnum.MUPT_TrapType_VALUE);
                        }

                        SC_BattleCmd.Builder builder = room.buildMistTips(tipsType, user, target, paramList);
                        switch (broadcastType) {
                            case 0: // 广播给房间所有玩家
                                room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
                                break;
                            case 1: // 广播给除自己外所有玩家
                                String idx = GameUtil.longToString(user.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), "");
                                MistPlayer player = MistPlayerCache.getInstance().queryObject(idx);
                                room.broadcastMsgExcludePlayer(MsgIdEnum.SC_BattleCmd_VALUE, builder, player, true);
                                break;
                            case 2: // 广播给所有队友
                                if (user instanceof MistFighter) {
                                    MistFighter fighter = (MistFighter) user;
                                    MistTeam team = room.getTeamById(fighter.getTeamId());
                                    if (team != null) {
                                        team.updateMemberCmd(builder, fighter.getId());
                                    }
                                }
                                break;
                            case 3: // 发送给自己
                                if (user instanceof MistFighter) {
                                    idx = GameUtil.longToString(user.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), "");
                                    player = MistPlayerCache.getInstance().queryObject(idx);
                                    if (player != null) {
                                        player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, builder);
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.UseItem: {
                    if (!(user instanceof MistFighter)) {
                        break;
                    }
                    if (params == null || !params.containsKey(MistTriggerParamType.UseItemSkillIndex)) {
                        break;
                    }
                    MistFighter fighter = (MistFighter) user;
                    long index = params.get(MistTriggerParamType.UseItemSkillIndex);
                    int itemCfgId = fighter.getSkillMachine().getItemTypeByIndex((int) index);
                    if (fighter.getSkillMachine().castItemSkill((int) index, params)) {
                        fighter.addUseItemCmd(itemCfgId);
                        fighter.sendUpdateItemSkillCmd(false);

                        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                                room.buildMistTips(EnumMistTipsType.EMTT_ItemUsed_VALUE, user, target, itemCfgId), true);
                        fighter.getNpcTask().doNpcTask(MistTaskTargetType.MTTT_UseItem_VALUE, 1, 0);
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeKeyWaitingBossState: {
                    if (cmdParams.size() > 1 && params != null && params.containsKey(MistTriggerParamType.KeyId)) {
                        int changeType = cmdParams.get(1);
                        long keyId = params.get(MistTriggerParamType.KeyId);
                        MistKey key = room.getObjManager().getMistObj(keyId);
                        if (key == null) {
                            break;
                        }
                        if (changeType > 0) {
                            key.setAttribute(MistUnitPropTypeEnum.MUPT_WaitingBossState_VALUE, 1);
                            key.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_WaitingBossState_VALUE, 1);
                            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildShowBossTimeCmd(MistBossKeyState.keyPicked), true);
                        } else {
                            key.setAttribute(MistUnitPropTypeEnum.MUPT_WaitingBossState_VALUE, 0);
                            key.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_WaitingBossState_VALUE, 0);
                            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildShowBossTimeCmd(MistBossKeyState.keyNotPicked), true);
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeBePickingState: {
                    if (cmdParams.size() > 2 && params != null) {
                        int objType = cmdParams.get(1);
                        int val = cmdParams.get(2);
                        if (objType == 0 && params.containsKey(MistTriggerParamType.TreasureBoxId)) { // 宝箱
                            long boxId = params.get(MistTriggerParamType.TreasureBoxId);
                            MistBaseBox box = room.getObjManager().getMistObj(boxId);
                            if (box == null || box.getAttribute(MistUnitPropTypeEnum.MUPT_IsShareReward_VALUE) > 0) {
                                break;
                            }
                            box.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, val);
                        } else if (objType == 1 && params.containsKey(MistTriggerParamType.TreasureBagId)) { // 资源带
                            long bagId = params.get(MistTriggerParamType.TreasureBagId);
                            MistTreasureBag bag = room.getObjManager().getMistObj(bagId);
                            if (bag == null) {
                                break;
                            }
                            bag.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, val);
                        } else if (objType == 2 && params.containsKey(MistTriggerParamType.ItemId)) { // 道具
                            long itemId = params.get(MistTriggerParamType.ItemId);
                            MistItem item = room.getObjManager().getMistObj(itemId);
                            if (item == null) {
                                break;
                            }
                            item.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, val);
                        } else if (objType == 3 && params.containsKey(MistTriggerParamType.ItemId)) {
                            long objId = params.get(MistTriggerParamType.LavaBadgeId);
                            MistLavaBadge lavaBadge = room.getObjManager().getMistObj(objId);
                            if (lavaBadge == null) {
                                break;
                            }
                            lavaBadge.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, val);
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeStunState: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeType = cmdParams.get(3);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            long isUnderControl = realTarget.getAttribute(MistUnitPropTypeEnum.MUPT_IsUnderControl_VALUE);
                            if (changeType > 0) {
                                ++isUnderControl;
                                realTarget.getBufMachine().interruptBuffByType(MistBuffInterruptType.BeUnderControl);
                            } else if (changeType == 0) {
                                isUnderControl = 0;
                            } else {
                                if (isUnderControl > 0) {
                                    --isUnderControl;
                                } else {
                                    isUnderControl = 0;
                                }
                            }
                            realTarget.getAttributes().put(MistUnitPropTypeEnum.MUPT_IsUnderControl_VALUE, isUnderControl);
                            realTarget.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_IsUnderControl_VALUE, isUnderControl);
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.FlickAway: {
                    if (cmdParams.size() > 5) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int towards = cmdParams.get(3);
                        int initSpeed = cmdParams.get(4);
                        int flickTime = cmdParams.get(5);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            realTarget.addFlickAwayCmd(initSpeed, flickTime, towards);
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.CreateCage: {
                    if (cmdParams.size() > 2) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject creator : objects) {
                            long camp = creator.getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE);
                            MistCage newObj = room.getObjManager().createObj(MistUnitTypeEnum.MUT_Cage_VALUE);
                            newObj.setAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE, camp);
                            newObj.setAttribute(MistUnitPropTypeEnum.MUPT_CreatorId_VALUE, creator.getId());
                            newObj.afterInit(new int[]{creator.getPos().getX(), creator.getPos().getY()}, new int[]{creator.getToward().getX(), creator.getToward().getY()});

                            room.getWorldMap().objFirstEnter(newObj);
                            if (creator instanceof MistFighter) {
                                MistFighter ownerFighter = (MistFighter) creator;
                                ownerFighter.addDropTrap(newObj.getId());
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeInvokerId: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeType = cmdParams.get(3);
                        long invokerId = 0;
                        if (changeType > 0 && params != null && params.containsKey(MistTriggerParamType.TransInvokerId)) {
                            invokerId = params.get(MistTriggerParamType.TransInvokerId);
                        }
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject mistObj : objects) {
                            mistObj.setAttribute(MistUnitPropTypeEnum.MUPT_TeamInvokerId_VALUE, invokerId);
                            mistObj.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_TeamInvokerId_VALUE, invokerId);
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ClearDeBuff: {
                    if (cmdParams.size() > 2) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject mistObj : objects) {
                            mistObj.getBufMachine().clearDeBuff();
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangePlayerHp: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeRate = cmdParams.get(3);
                        if (changeRate == 0 || changeRate > GameConst.PetMaxHpRate || changeRate < -GameConst.PetMaxHpRate) {
                            break;
                        }
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject mistObj : objects) {
                            if (mistObj instanceof MistFighter) {
                                MistPlayer player = ((MistFighter) mistObj).getOwnerPlayerInSameRoom();
                                if (player == null) {
                                    continue;
                                }
                                Event event = Event.valueOf(EventType.ET_ChangePlayerHpRate, room, player);
                                event.pushParam(changeRate);
                                EventManager.getInstance().dispatchEvent(event);
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.PlayEffect: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int effectId = cmdParams.get(3);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject mistObj : objects) {
                            mistObj.addTriggerEffectCmd(effectId);
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.MazeTransPos: {
                    if (cmdParams.size() > 2 && params != null) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        Long levelObj = params.get(MistTriggerParamType.BlinkGridLevel);
                        int level = levelObj != null ? levelObj.intValue() : 0;
                        if (level <= 0) {
                            return true;
                        }
                        Long transLevelObj = params.get(MistTriggerParamType.BlinkGridTransLevel);
                        int transLevel = transLevelObj != null ? transLevelObj.intValue() : 0;
                        int newLevel = level + transLevel;
                        Long towardObj = params.get(MistTriggerParamType.BlinkGridTransToward);
                        int toward = towardObj != null ? towardObj.intValue() : 0;
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        MistMazeAreaConfigObject cfg = MistMazeAreaConfig.getByLevel(newLevel);
                        if (cfg == null || cfg.getEnterpos() == null) {
                            return true;
                        }
                        if (cfg.getEnterpos().length < 2) {
                            return true;
                        }
                        int newPosX = cfg.getEnterpos()[0];
                        int newPosY = cfg.getEnterpos()[1];
                        if (!room.getWorldMap().isPosValid(newPosX, newPosY)) {
                            LogUtil.error("Maze trans Pos error,level=" + level + ",transLevel=" + transLevel + ",posX=" + newPosX + ",poxY=" + newPosY);
                            return true;
                        }
                        for (MistObject mistObj : objects) {
                            if (!(mistObj instanceof MistFighter)) {
                                continue;
                            }
                            MistFighter targetFighter = (MistFighter) mistObj;
                            MistPlayer mistPlayer = targetFighter.getOwnerPlayerInSameRoom();
                            if (mistPlayer == null) {
                                continue;
                            }
                            int oldX = targetFighter.getPos().getX();
                            int oldY = targetFighter.getPos().getY();
                            targetFighter.setPos(newPosX, newPosY);
                            room.getWorldMap().objMove(targetFighter, oldX, oldY);

                            targetFighter.addChangePosInfoCmd(targetFighter.getPos().build(), targetFighter.getToward().build());

                            targetFighter.setAttribute(MistUnitPropTypeEnum.MUPT_MazeAreaLevel_VALUE, newLevel);
                            targetFighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_MazeAreaLevel_VALUE, newLevel);

                            if (transLevel > 0) {
                                CS_GS_UpdateMistMazeRecord.Builder recordBuilder = CS_GS_UpdateMistMazeRecord.newBuilder();
                                recordBuilder.setPlayerIdx(mistPlayer.getIdx());
                                recordBuilder.setLevel(newLevel);
                                recordBuilder.setToward(toward);
                                GlobalData.getInstance().sendMsgToServer(mistPlayer.getServerIndex(), MsgIdEnum.CS_GS_UpdateMistMazeRecord_VALUE, recordBuilder);
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ApplyExitMistRoom: {
                    if (user instanceof MistFighter) {
                        MistFighter fighter = (MistFighter) user;
                        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
                        if (player == null) {
                            break;
                        }
                        MistRetCode retCode = room.onPlayerExit(player, true);
                        if (retCode == MistRetCode.MRC_Success) {
                            CS_GS_LeaveMistForest.Builder retBuilder = CS_GS_LeaveMistForest.newBuilder();
                            retBuilder.setPlayerIdx(player.getIdx());
                            retBuilder.setRetCode(retCode);
                            GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_LeaveMistForest_VALUE, retBuilder);

                            // 必须在CS_GS_LeaveMistForest消息发送之后处理
                            Event event = Event.valueOf(EventType.ET_Logout, room, player);
                            event.pushParam(false);
                            EventManager.getInstance().dispatchEvent(event);
                        }
                    }
                    break;
                }
                case MistCammondType.RecordOrClearOptionalBoxId: {
                    if (cmdParams.size() > 3 && params != null && params.containsKey(MistTriggerParamType.TreasureBoxId)) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        boolean recordFlag = cmdParams.get(3) > 0;
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        long boxId = params.get(MistTriggerParamType.TreasureBoxId);
                        for (MistObject mistObj : objects) {
                            if (mistObj instanceof MistFighter) {
                                if (recordFlag) {
                                    mistObj.setAttribute(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE, boxId);
                                    mistObj.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE, boxId);
                                } else {
                                    mistObj.setAttribute(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE, 0);
                                    mistObj.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE, 0);
                                }
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeJewelryCount: {
                    if (cmdParams.size() > 4) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeType = cmdParams.get(3);
                        int changeVal = cmdParams.get(4);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter realFighter = (MistFighter) realTarget;
                                long value = realFighter.getAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE);
                                if (changeType == 0) {
                                    value = changeVal;
                                } else if (changeType == 1) {
                                    value += changeVal;
                                } else if (changeType == 2) {
                                    value += value * changeVal / 1000;
                                } else if (changeType == 3) {
                                    Long tmpVal = params != null ? params.get(MistTriggerParamType.SettleJewelryCount) : 0l;
                                    changeVal = tmpVal != null ? tmpVal.intValue() : 0;
                                    value += changeVal;
                                }
                                realFighter.changeJewelryCount(value);
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.SummonHiddenEvil: {
                    if (user instanceof MistFighter) {
                        MistFighter fighter = (MistFighter) user;
                        fighter.summonEvilMonster();
                        return true;
                    }
                    break;
                }
                case MistCammondType.ClearHiddenEvilState: {
                    if (user instanceof MistFighter) {
                        MistFighter fighter = (MistFighter) user;
                        fighter.changeHiddenEvilState(null);
                        return true;
                    }
                    break;
                }
                case MistCammondType.BroadCastBossEffect: {
                    if (user instanceof MistActivityBoss && cmdParams.size() > 1) {
                        int state = cmdParams.get(1);
                        SC_UpdateMistActivityBossState.Builder builder = SC_UpdateMistActivityBossState.newBuilder();
                        builder.setBossState(state);
                        builder.setBossTypeValue(user.getType());
                        room.broadcastMsg(MsgIdEnum.SC_UpdateMistActivityBossState_VALUE, builder, true);
                        return true;
                    }
                    break;
                }
                case MistCammondType.RemoveObject: {
                    if (params != null && params.containsKey(MistTriggerParamType.RemoveObjId)) {
                        long removeObjId = params.get(MistTriggerParamType.RemoveObjId);
                        MistObject obj = room.getObjManager().getMistObj(removeObjId);
                        if (null != obj) {
                            obj.dead();
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.FlagBeTouchSnowBallId: {
                    if (cmdParams.size() > 3 && params != null && params.containsKey(MistTriggerParamType.SnowBallId)) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        boolean bAdd = cmdParams.get(3) > 0;
                        long snowBallId = params.get(MistTriggerParamType.SnowBallId);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter realFighter = (MistFighter) realTarget;
                                if (bAdd) {
                                    realFighter.addBeTouchedSnowBall(snowBallId);
                                } else {
                                    realFighter.removeBeTouchedSnowBall(snowBallId);
                                }
                            }
                        }
                    }
                    break;
                }
                case MistCammondType.CreateVolcanoStone: {
                    if (user instanceof MistVolcano) {
                        MistVolcano volcano = (MistVolcano) user;
                        volcano.generateVolcanoStone();
                    }
                    break;
                }
                case MistCammondType.ChangeLavaBadgeCount: {
                    if (cmdParams.size() > 4) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeType = cmdParams.get(3);
                        int changeVal = cmdParams.get(4);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter realFighter = (MistFighter) realTarget;
                                long value = realFighter.getAttribute(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE);
                                if (changeType == 0) {
                                    value = changeVal;
                                } else if (changeType == 1) {
                                    value += changeVal;
                                } else if (changeType == 2) {
                                    value += value * changeVal / 1000;
                                } else if (changeType == 3) {
                                    Long tmpVal = params != null ? params.get(MistTriggerParamType.LavaBadgeCount) : 0l;
                                    changeVal = tmpVal != null ? tmpVal.intValue() : 0;
                                    value += changeVal;
                                }
                                realFighter.changeLavaBadge(value);
                            }
                        }
                    }
                    break;
                }
                case MistCammondType.ChangeShowDataState: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        boolean changeValue = cmdParams.get(3) > 0;
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter realFighter = (MistFighter) realTarget;
                                realFighter.changeSearchShowObjsState(changeValue);
                            }
                        }
                    }
                    break;
                }
                case MistCammondType.FighterWaitingForReborn: {
                    if (cmdParams.size() > 2) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter realFighter = (MistFighter) realTarget;
                                realFighter.changeToWaitingForRebornState();
                            }
                        }
                    }
                    break;
                }
                case MistCammondType.FighterRebornForDefeated: {
                    if (cmdParams.size() > 4) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        boolean toSafeRegion = cmdParams.get(3) > 0;
                        boolean maxHp = cmdParams.get(4) > 0;
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter realFighter = (MistFighter) realTarget;
                                realFighter.rebornForDefeated(toSafeRegion, maxHp);
                            }
                        }
                    }
                    break;
                }
                case MistCammondType.ChangePlayerStamina: {
                    if (cmdParams.size() > 4) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int changeType = cmdParams.get(3);
                        int changeCount = cmdParams.get(4);
                        if (changeType == 2 && (params == null || !params.containsKey(MistTriggerParamType.ChangeStaminaVal))) {
                            break;
                        }
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter realFighter = (MistFighter) realTarget;
                                MistPlayer player = realFighter.getOwnerPlayerInSameRoom();
                                if (player == null) {
                                    continue;
                                }
                                int tmpCount = 0;
                                if (changeType == 0) {
                                    tmpCount = Math.max(-player.getMistStamina(), -changeCount);
                                } else if (changeType == 1) {
                                    tmpCount = changeCount;
                                } else if (changeType == 2) {
                                    long needStamina = params.get(MistTriggerParamType.ChangeStaminaVal);
                                    tmpCount = Math.max(-player.getMistStamina(), (int) -needStamina);
                                }
                                if (tmpCount == 0) {
                                    continue;
                                }
                                CS_GS_ReqChangeMistStamina.Builder builder = CS_GS_ReqChangeMistStamina.newBuilder();
                                builder.setPlayerIdx(player.getIdx());
                                builder.setChangeValue(tmpCount);
                                GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE, builder);
                            }
                        }
                    }
                    break;
                }
                case MistCammondType.GainCommonRewardObjReward: {
                    if (user instanceof MistFighter && params != null && params.containsKey(MistTriggerParamType.CommonRewardObjId)) {
                        MistFighter fighter = (MistFighter) user;
                        long rewardObjId = params.get(MistTriggerParamType.CommonRewardObjId);
                        MistCommonRewardObj rewardObj = room.getObjManager().getMistObj(rewardObjId);
                        if (rewardObj == null) {
                            break;
                        }
                        int needStamina = (int) rewardObj.getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
                        if (needStamina > 0) {
                            MistPlayer owner = fighter.getOwnerPlayerInSameRoom();
                            if (owner == null || owner.isRobot()) {
                                break;
                            }
                            if (owner.getMistStamina() < needStamina) {
                                break;
                            }
                            CS_GS_ReqChangeMistStamina.Builder builder = CS_GS_ReqChangeMistStamina.newBuilder();
                            builder.setPlayerIdx(owner.getIdx());
                            builder.setChangeValue(-needStamina);
                            GlobalData.getInstance().sendMsgToServer(owner.getServerIndex(), MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE, builder);
                        }
                        rewardObj.gainReward(fighter);
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeRecoverHpIntervalRate: {
                    if (cmdParams.size() > 3) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int addRate = cmdParams.get(3);
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject realTarget : objects) {
                            if (realTarget instanceof MistFighter) {
                                MistFighter fighter = (MistFighter) realTarget;
                                MistPlayer player = fighter.getOwnerPlayerInSameRoom();
                                if (player == null) {
                                    continue;
                                }
                                Event event = Event.valueOf(EventType.ET_ChangeRecoverHpIntervalRate, room, player);
                                event.pushParam(addRate);
                                EventManager.getInstance().dispatchEvent(event);
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MistCammondType.ChangeAdditionBuffRate: {
                    if (cmdParams.size() > 4) {
                        int cmdObjType = cmdParams.get(1);
                        int cmdObjParam = cmdParams.get(2);
                        int additionType = cmdParams.get(3);
                        int changeRate = cmdParams.get(4);
                        if (changeRate == 0) {
                            break;
                        }
                        List<MistObject> objects = getRealTargets(room, user, target, cmdObjType, cmdObjParam);
                        for (MistObject mistObj : objects) {
                            if (mistObj instanceof MistFighter) {
                                MistFighter fighter = ((MistFighter) mistObj);
                                fighter.addAdditionBuffData(additionType, changeRate);
                            }
                        }
                        return true;
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return false;
    }

    private static List<MistObject> getRealTargets(MistRoom room, MistObject user, MistObject target, int targetType, int param) {
        List<MistObject> targets = new ArrayList<>();
        if (targetType == 0 && user != null) {
            targets.add(user);
        } else if (targetType == 1 && target != null) {
            targets.add(target);
        } else if (targetType == 2 && user != null) {
            AoiNode aoiNode = room.getWorldMap().getAoiNodeById(user.getAoiNodeKey());
            if (aoiNode != null) {
                aoiNode.getAllAroundNearObjByType(targets, user, MistUnitTypeEnum.MUT_Player_VALUE, param, true);
            }
        } else if (targetType == 3 && user != null) {
            AoiNode aoiNode = room.getWorldMap().getAoiNodeById(user.getAoiNodeKey());
            if (aoiNode != null) {
                aoiNode.getAllAroundNearObjByType(targets, user, MistUnitTypeEnum.MUT_Player_VALUE, param, false);
            }
        } else if (targetType == 4 && user instanceof MistFighter) {
            MistFighter fighter = (MistFighter) user;
            MistTeam mistTeam = room.getTeamById(fighter.getTeamId());
            if (mistTeam != null) {
                for (MistFighter teamMember : mistTeam.getAllMembers().values()) {
                    if (teamMember.getId() == fighter.getId()) {
                        continue;
                    }
                    targets.add(teamMember);
                }
            }
        } else if (targetType == 5 && user instanceof MistFighter) {
            MistFighter fighter = (MistFighter) user;
            MistTeam mistTeam = room.getTeamById(fighter.getTeamId());
            if (mistTeam != null) {
                for (MistFighter teamMember : mistTeam.getAllMembers().values()) {
                    targets.add(teamMember);
                }
            } else {
                targets.add(fighter);
            }
        }
        return targets;
    }
}
