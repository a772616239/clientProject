package model.mistforest.mistobj;

import cfg.MistGhostConfig;
import cfg.MistGhostConfigObject;
import common.GameConst.EventType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistGhostState;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.map.AStar.Astar;
import model.mistforest.map.AStar.Coord;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.room.entity.MistGhostBusterRoom.MistGhostBusterRoom;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_MovePath;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistGhostTypeEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.UnitMetadata;
import server.event.Event;
import server.event.EventManager;
import util.LogUtil;

public class MistGhost extends MistObject {
    protected int ghostState;

    private Astar astar;
    private List<MistFighter> aroundPlayerList;
    private MistFighter nearestFighter;
    private List<Coord> pursuePath;
    private Coord escapeCoord;
    private Coord escapeExtCoord;

    private long updateStateTime;
    private long lastMoveTime;

    public MistGhost(MistRoom room, int objType) {
        super(room, objType);
        this.astar = new Astar();
        this.aroundPlayerList = new ArrayList<>();
        this.escapeCoord = new Coord(0, 0);
        this.escapeExtCoord = new Coord(0, 0);
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata.Builder metaData = UnitMetadata.newBuilder();
        metaData.mergeFrom(super.getMetaData(fighter));
        if (pursuePath != null) {
            for (Coord coord : pursuePath) {
                metaData.addMovePath(ProtoVector.newBuilder().setX(coord.x).setY(coord.y));
            }
        }
        return metaData.build();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        room.getObjManager().addGhostTypeCount((int) getAttribute(MistUnitPropTypeEnum.MUPT_GhostType_VALUE));
    }

    @Override
    public void reborn() {
        ghostState = MistGhostState.idle;
        updateStateTime = 0;
        astar.clear();
        aroundPlayerList.clear();
        if (pursuePath != null) {
            pursuePath.clear();
        }
        nearestFighter = null;
        lastMoveTime = 0;
        super.reborn();
        room.getObjManager().addGhostTypeCount((int) getAttribute(MistUnitPropTypeEnum.MUPT_GhostType_VALUE));
    }

    @Override
    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);

        room.getObjManager().minusGhostTypeCount((int) getAttribute(MistUnitPropTypeEnum.MUPT_GhostType_VALUE));
        super.dead();
    }

    public boolean isGuardGhostType() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_GhostType_VALUE) == MistGhostTypeEnum.MGTE_GuardGhost_VALUE;
    }

    public boolean isPreDead() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_PreDeadState_VALUE) > 0;}

    @Override
    public void beTouch(MistFighter fighter) {
        if (getAttribute(MistUnitPropTypeEnum.MUPT_PreDeadState_VALUE) > 0) {
            return;
        }
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.GhostObjId, getId());
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchGhost, this, params);

        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistGhostConfigObject cfg = MistGhostConfig.getById(cfgId);
        if (cfg == null) {
            LogUtil.error("MistGhost beTouch not found cfgid=" + cfgId);
            return;
        }
        Map<Integer, Integer> rewardMap = MistConst.buildGostConfigReward(cfgId);
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player != null && rewardMap != null) {
            Event event = Event.valueOf(EventType.ET_GainMistCarryReward, room, player);
            event.pushParam(rewardMap, false);
            EventManager.getInstance().dispatchEvent(event);
        }
        if (getRoom().getMistRule() == EnumMistRuleKind.EMRK_GhostBuster_VALUE) {
            MistGhostBusterRoom ghostRoom = (MistGhostBusterRoom) getRoom();
            ghostRoom.addFighterScore(fighter, cfg.getGhosttouchscore());
            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, room.buildMistTips(EnumMistTipsType.EMTT_PlayerTouchGhost_VALUE,
                    fighter, this, (int) getAttribute(MistUnitPropTypeEnum.MUPT_GhostType_VALUE), cfg.getGhosttouchscore()), true);
        }
    }

    private boolean isPathEmpty() {
        return pursuePath == null || pursuePath.isEmpty();
    }

    private boolean checkContinuePursueOrEscape() {
        if (nearestFighter == null) {
            return false;
        }
        if (!canTouch(nearestFighter, true)) {
            return false;
        }
        int pursueDis = (int) getAttribute(MistUnitPropTypeEnum.MUPT_Monster_PursueDis_VALUE);
        return MistConst.checkInDistance( pursueDis, getPos().build(), getInitPos().build());
    }

    public void addMovePathCmd(boolean stop) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_MovePath);
        BattleCMD_MovePath.Builder cmdBuilder = BattleCMD_MovePath.newBuilder();
        cmdBuilder.setTargetId(getId());
        ProtoVector.Builder curPos = ProtoVector.newBuilder();
        curPos.setX(getPos().getX() * 1000 + getMiniPos().getX());
        curPos.setY(getPos().getY() * 1000 + getMiniPos().getY());
        cmdBuilder.setCurrentPos(curPos);

        ProtoVector.Builder nextPos = ProtoVector.newBuilder();
        if (stop || isPathEmpty()) {
            nextPos.setX(-1); // 负数表示停止
            nextPos.setY(-1);
            cmdBuilder.addNextPosList(nextPos);
        } else {
            Coord coord = pursuePath.get(0);
            nextPos.setX(coord.x * 1000);
            nextPos.setY(coord.y * 1000);
            cmdBuilder.addNextPosList(nextPos.build());

            if (pursuePath.size() > 1) {
                coord = pursuePath.get(1);
                nextPos.setX(coord.x * 1000);
                nextPos.setY(coord.y * 1000);
                cmdBuilder.addNextPosList(nextPos.build());
            }
        }

        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    private boolean move(long interval) {
        if (isPathEmpty()) {
            return false;
        }
        Coord curPathNode = pursuePath.get(0);
        if (!room.getWorldMap().isPosReachable(curPathNode.x, curPathNode.y) || room.getWorldMap().isInSafeRegion(curPathNode.x, curPathNode.y)) {
            pursuePath.clear();
            return false;
        }
        if (curPathNode.equals(getPos().build())) {
            pursuePath.remove(0);
            return false;
        }
        boolean posChanged = false;
        long speed = getAttribute(MistUnitPropTypeEnum.MUPT_Speed_VALUE);
        long disX = Math.abs(speed * curPathNode.toward.getX()) / 10 * interval / 1000; // X轴行进距离 除以10是由于标准向量扩大了10倍
        long disY = Math.abs(speed * curPathNode.toward.getY()) / 10 * interval / 1000; // Y轴行进距离
        int moveMiniX = (int) (getMiniPos().getX() + disX);
        int moveMiniY = (int) (getMiniPos().getY() + disY);
        if (moveMiniX / 1000 > 0 || moveMiniY / 1000 > 0) {
            // 单位时间移动位置不能大于1000
            int oldX = getPos().getX();
            int oldY = getPos().getY();
            setPos(curPathNode.x, curPathNode.y);
            room.getWorldMap().objMove(this, oldX, oldY);
            pursuePath.remove(0);
            posChanged = true;
        }
        if (posChanged) {
            setMiniPos(0, 0);
        } else {
            setMiniPos(moveMiniX, moveMiniY);
        }

        return posChanged;
    }

    private MistFighter checkTouch() {
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode == null) {
            return null;
        }
        List<MistObject> mistObjects = new ArrayList<>();
        aoiNode.getAllObjByType(mistObjects, MistUnitTypeEnum.MUT_Player_VALUE);
        MistFighter fighter;
        for (MistObject obj : mistObjects) {
            if (!(obj instanceof MistFighter)) {
                continue;
            }
            fighter = (MistFighter) obj;
            if (!canTouch(fighter, true)) {
                continue;
            }
            if (MistConst.checkSamePos(getPos(), fighter.getPos())) {
                return fighter;
            }
        }
        return null;
    }

    private boolean calcNextEscapePos() {
        if (nearestFighter == null) {
            return false;
        }
        int deltaX = getPos().getX() - nearestFighter.getPos().getX();
        int deltaY = getPos().getY() - nearestFighter.getPos().getY();
        if (deltaX == 0 && deltaY == 0) {
            return false;
        }
        int escapeX = 0;
        int escapeY = 0;
        int escapeTowardX = 0;
        int escapeTowardY = 0;
        if (deltaX == 0) {
            escapeY = deltaY > 0 ? 1 : -1;
            escapeTowardY = 10;
        } else if (deltaY == 0) {
            escapeX = deltaX > 0 ? 1 : -1;
            escapeTowardX = 10;
        } else {
            // 根据tanSeta22.5度和67.5度的函数值估算需要移动的方向
            int tanSeta = deltaY * 10 / deltaX;
            if (tanSeta >= -4 && tanSeta <= 4) {
                escapeX = deltaX > 0 ? 1 : -1;
                escapeTowardX = deltaX > 0 ? 10 : -10;
            } else if (tanSeta > 4 && tanSeta < 24) {
                escapeX = deltaX > 0 ? 1 : -1;
                escapeY = deltaX > 0 ? 1 : -1;
                escapeTowardX = deltaX > 0 ? 7 : -7;
                escapeTowardY = deltaX > 0 ? 7 : -7;
            } else if (tanSeta > -24 && tanSeta < -4) {
                escapeX = deltaY > 0 ? -1 : 1;
                escapeY = deltaY > 0 ? 1 : -1;
                escapeTowardX = deltaX > 0 ? -7 : 7;
                escapeTowardY = deltaX > 0 ? 7 : -7;
            } else if (tanSeta > 24 || tanSeta < -24) {
                escapeY = deltaY > 0 ? 1 : -1;
                escapeTowardY = deltaX > 0 ? 10 : -10;
            }
        }
        int newPosX = getPos().getX() + escapeX;
        int newPosY = getPos().getY() + escapeY;
        if (!room.getWorldMap().isPosReachable(newPosX, newPosY) || room.getWorldMap().isInSafeRegion(newPosX, newPosY)) {
            return false;
        }
        if (pursuePath == null) {
            pursuePath = new ArrayList<>();
        }
        escapeCoord.x = newPosX;
        escapeCoord.y = newPosY;
        escapeCoord.toward.setX(escapeTowardX);
        escapeCoord.toward.setY(escapeTowardY);
        pursuePath.add(escapeCoord);

        escapeExtCoord.x = newPosX + escapeX;
        escapeExtCoord.y = newPosY + escapeY;
        escapeExtCoord.toward.setX(escapeTowardX);
        escapeExtCoord.toward.setY(escapeTowardY);
        pursuePath.add(escapeExtCoord);
        return true;
    }

    public void onIdleState(long curTime) {
        int ghostType = (int) getAttribute(MistUnitPropTypeEnum.MUPT_GhostType_VALUE);
        if (searchTarget()) {
            switch (ghostType) {
                case MistGhostTypeEnum.MGTE_NormalGhost_VALUE:
                case MistGhostTypeEnum.MGTE_EliteGhost_VALUE: {
                    if (calcNextEscapePos()) {
                        ghostState = MistGhostState.escape;
                        updateStateTime = curTime;
                        lastMoveTime = curTime;
                        addMovePathCmd(false);
                    }
                    break;
                }

                case MistGhostTypeEnum.MGTE_GuardGhost_VALUE: {
                    pursuePath = room.getWorldMap().findPath(astar, getPos().build(), nearestFighter.getPos().build());
                    if (!isPathEmpty()) {
                        ghostState = MistGhostState.pursue;
                        updateStateTime = curTime;
                        lastMoveTime = curTime;
                        addMovePathCmd(false);
                    } else {
                        nearestFighter = null;
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    public void onEscapeState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                MistFighter target = checkTouch();
                if (target != null) {
                    target.touchObj(this);
                } else if (searchTarget() && calcNextEscapePos()) {
                    updateStateTime = curTime;
                    addMovePathCmd(false);
                } else {
                    ghostState = MistGhostState.idle;
                    addMovePathCmd(true);
                }
            } else if (isPathEmpty()) {
                ghostState = MistGhostState.idle;
                updateStateTime = curTime;
                addMovePathCmd(true);
            }
            lastMoveTime = curTime;
        }
    }

    public void onPursueState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                MistFighter target = checkTouch();
                if (target != null) {
                    target.touchObj(this);
                } else if (!checkContinuePursueOrEscape()) { // 进判断状态
                    LogUtil.debug("EscapeGhost[" + getId() + "] findPath to endPursue for checkPursue() false");
                    pursuePath = room.getWorldMap().findPath(astar, getPos().build(), getInitPos().build());
                    ghostState = MistGhostState.endPursue;
                    updateStateTime = curTime;
                    nearestFighter = null;
                    addMovePathCmd(false);
                } else {
                    if (isPathEmpty() || !pursuePath.get(pursuePath.size() - 1).equals(nearestFighter.getPos())) {
                        LogUtil.debug("GuardGhost[" + getId() + "] findPath for targetChangePos");
                        pursuePath = room.getWorldMap().findPath(astar, getPos().build(), nearestFighter.getPos().build());
                    }
                    addMovePathCmd(false);
                }
            } else if (isPathEmpty()) {
                pursuePath = room.getWorldMap().findPath(astar, getPos().build(), getInitPos().build());
                ghostState = MistGhostState.endPursue;
                updateStateTime = curTime;
                nearestFighter = null;
                addMovePathCmd(false);
            }
            lastMoveTime = curTime;
        }
    }

    public void onEndPursueState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                MistFighter target = checkTouch();
                if (target != null) {
                    target.touchObj(this);
                } else if (searchTarget()) {
                    pursuePath = room.getWorldMap().findPath(astar, getPos().build(), nearestFighter.getPos().build());
                    if (!isPathEmpty()) {
                        ghostState = MistGhostState.pursue;
                        updateStateTime = curTime;
                        addMovePathCmd(false);
                    } else {
                        nearestFighter = null;
                        pursuePath = room.getWorldMap().findPath(astar, getPos().build(), getInitPos().build());
                    }
                } else if (MistConst.checkSamePos(getPos(), getInitPos())) {
                    nearestFighter = null;
                    ghostState = MistGhostState.idle;
                    updateStateTime = curTime;
                    addMovePathCmd(true);
                } else if (isPathEmpty()) {
                    int oldX = getPos().getX();
                    int oldY = getPos().getY();
                    setPos(getInitPos().build());
                    addChangePosInfoCmd(getPos().build(), getToward().build());
                    room.getWorldMap().objMove(this, oldX, oldY);
                } else {
                    addMovePathCmd(false);
                }
            } else if (isPathEmpty()) {
                if (!MistConst.checkSamePos(getPos(), getInitPos())) {
                    int oldX = getPos().getX();
                    int oldY = getPos().getY();
                    setPos(getInitPos().build());
                    addChangePosInfoCmd(getPos().build(), getToward().build());
                    room.getWorldMap().objMove(this, oldX, oldY);
                }
                nearestFighter = null;
                ghostState = MistGhostState.idle;
                updateStateTime = curTime;
            }
            lastMoveTime = curTime;
        }
    }

    private boolean searchTarget() {
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode == null) {
            return false;
        }
        aoiNode.getAroundObjByType(aroundPlayerList, MistUnitTypeEnum.MUT_Player_VALUE);
        if (aroundPlayerList.isEmpty()) {
            return false;
        }
        return getNearestFighter();
    }

    public boolean canTouch(MistFighter fighter, boolean checkInSafeRegion) {
        if (fighter == null) {
            return false;
        }
        if (checkInSafeRegion && fighter.isInSafeRegion()) {
            return false;
        }
        return true;
    }

    private boolean getNearestFighter() {
        int minDis = 0;
        int pursueDis = (int) getAttribute(MistUnitPropTypeEnum.MUPT_Monster_PursueDis_VALUE);
        for (MistFighter fighter1 : aroundPlayerList) {
            if (!canTouch(fighter1, true)) {
                continue;
            }

            if (isGuardGhostType() && !MistConst.checkInDistance(pursueDis, getInitPos().build(), fighter1.getPos().build())) {
                continue;
            }
            int tmpDis = MistConst.getDistanceSqr(getPos().build(), fighter1.getPos().build());
            if (nearestFighter == null || tmpDis <= minDis) {
                nearestFighter = fighter1;
                minDis = tmpDis;
            }
        }
        aroundPlayerList.clear();
        return nearestFighter != null;
    }

    public void onTick(long curTime) {
        if (isAlive() && !isPreDead()) {
            switch (ghostState) {
                case MistGhostState.idle:
                    onIdleState(curTime);
                    break;
                case MistGhostState.escape:
                    onEscapeState(curTime);
                    break;
                case MistGhostState.pursue:
                    onPursueState(curTime);
                    break;
                case MistGhostState.endPursue:
                    onEndPursueState(curTime);
                    break;
                default:
                    break;
            }
        }
        super.onTick(curTime);
    }
}
