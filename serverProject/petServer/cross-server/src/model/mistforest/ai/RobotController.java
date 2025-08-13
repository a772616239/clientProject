package model.mistforest.ai;

import java.util.ArrayList;
import java.util.List;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistGhostState;
import model.mistforest.MistConst.MistRobotState;
import model.mistforest.map.AStar.Astar;
import model.mistforest.map.AStar.Coord;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistGhost;
import model.mistforest.mistobj.MistObject;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.BattleCMD_MovePath;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistGhostTypeEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import util.LogUtil;

public class RobotController {
    private MistFighter ownerFighter;
    private Astar astar;
    private List<MistGhost> aroundGhostList;
    private MistGhost firstTarget; // 第一优先级鬼魂
    private List<Coord> movePath;
    private Coord escapeCoord;
    private Coord escapeExtCoord;

    private int robotState;
    private long lastMoveTime;

    public RobotController(MistFighter fighter) {
        ownerFighter = fighter;
        astar = new Astar();
        aroundGhostList = new ArrayList<>();
        escapeCoord = new Coord(0, 0);
        escapeExtCoord = new Coord(0, 0);
    }

    public void clear() {
        ownerFighter = null;
        astar.clear();
        firstTarget = null;
        aroundGhostList.clear();
        if (movePath != null) {
            movePath.clear();
        }
        lastMoveTime = 0;
    }

    public void resetPath() {
        robotState = MistRobotState.idle;
        astar.clear();
        aroundGhostList.clear();
        if (movePath != null) {
            movePath.clear();
        }
        firstTarget = null;
        lastMoveTime = 0;
        addMovePathCmd(true);
    }

    private boolean searchTarget() {
        AoiNode aoiNode = ownerFighter.getRoom().getWorldMap().getAoiNodeById(ownerFighter.getAoiNodeKey());
        if (aoiNode == null) {
            return false;
        }
        aoiNode.getAroundObjByType(aroundGhostList, MistUnitTypeEnum.MUT_Ghost_VALUE);
        if (aroundGhostList.isEmpty()) {
            return false;
        }
        return getFirstTargetGhost();
    }

    protected boolean getFirstTargetGhost() {
        int minDis = 0;
        int maxCheckDisSqr = MistConst.MistRobotPursueDistance * MistConst.MistRobotPursueDistance;
        int firstTargetType = firstTarget != null ?
                (int) firstTarget.getAttribute(MistUnitPropTypeEnum.MUPT_GhostType_VALUE): MistGhostTypeEnum.MGTE_Null_VALUE;
        for (MistGhost ghost : aroundGhostList) {
            if (!ghost.canTouch(ownerFighter, true)) {
                continue;
            }
            int tmpDis = MistConst.getDistanceSqr(ownerFighter.getPos().getX(), ownerFighter.getPos().getY(), ghost.getPos().getX(), ghost.getPos().getY());
            if (maxCheckDisSqr <= tmpDis) {
                continue;
            }
            int ghostType = (int) ghost.getAttribute(MistUnitPropTypeEnum.MUPT_GhostType_VALUE);
            if (ghostType > firstTargetType || (firstTargetType == ghostType && tmpDis <= minDis)) {
                firstTarget = ghost;
                minDis = tmpDis;
                firstTargetType = ghostType;
            }
        }
        aroundGhostList.clear();
        return firstTarget != null;
    }

    private boolean isPathEmpty() {
        return movePath == null || movePath.isEmpty();
    }

    private boolean move(long interval) {
        if (isPathEmpty()) {
            return false;
        }
        Coord curPathNode = movePath.get(0);
        if (!ownerFighter.getRoom().getWorldMap().isPosReachable(curPathNode.x, curPathNode.y)) {
            movePath.clear();
            return false;
        }
        if (curPathNode.equals(ownerFighter.getPos().build())) {
            movePath.remove(0);
            return false;
        }
        boolean posChanged = false;
        long speed = ownerFighter.getAttribute(MistUnitPropTypeEnum.MUPT_Speed_VALUE);
        long disX = Math.abs(speed * curPathNode.toward.getX()) / 10 * interval / 1000; // X轴行进距离 除以10是由于标准向量扩大了10倍
        long disY = Math.abs(speed * curPathNode.toward.getY()) / 10 * interval / 1000; // Y轴行进距离
        int moveMiniX = (int) (ownerFighter.getMiniPos().getX() + disX);
        int moveMiniY = (int) (ownerFighter.getMiniPos().getY() + disY);
        if (moveMiniX / 1000 > 0 || moveMiniY / 1000 > 0) {
            // 单位时间移动位置不能大于1000
            int oldX = ownerFighter.getPos().getX();
            int oldY = ownerFighter.getPos().getY();
            ownerFighter.setPos(curPathNode.x, curPathNode.y);
            ownerFighter.getRoom().getWorldMap().objMove(ownerFighter, oldX, oldY);
            movePath.remove(0);
            posChanged = true;
        }
        LogUtil.debug("RobotFighter Move id={},moveMiniX={},moveMineY={},posChange={},time={}", ownerFighter.getId(), moveMiniX, moveMiniY, posChanged, lastMoveTime);
        if (posChanged) {
            ownerFighter.setMiniPos(0, 0);
        } else {
            ownerFighter.setMiniPos(moveMiniX, moveMiniY);
        }

        return posChanged;
    }

    public void addMovePathCmd(boolean stop) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_MovePath);
        BattleCMD_MovePath.Builder cmdBuilder = BattleCMD_MovePath.newBuilder();
        cmdBuilder.setTargetId(ownerFighter.getId());
        ProtoVector.Builder curPos = ProtoVector.newBuilder();
        curPos.setX(ownerFighter.getPos().getX() * 1000 + ownerFighter.getMiniPos().getX());
        curPos.setY(ownerFighter.getPos().getY() * 1000 + ownerFighter.getMiniPos().getY());
        cmdBuilder.setCurrentPos(curPos);

        if (stop || isPathEmpty()) {
            ProtoVector.Builder nextPos = ProtoVector.newBuilder();
            nextPos.setX(-1); // 负数表示停止
            nextPos.setY(-1);
            cmdBuilder.addNextPosList(nextPos);
        } else {
            ProtoVector.Builder nextPos1 = ProtoVector.newBuilder();
            Coord coord1 = movePath.get(0);
            nextPos1.setX(coord1.x * 1000);
            nextPos1.setY(coord1.y * 1000);
            cmdBuilder.addNextPosList(nextPos1);
            if (movePath.size() > 1) {
                ProtoVector.Builder nextPos2 = ProtoVector.newBuilder();
                Coord coord2 = movePath.get(1);
                nextPos2.setX(coord2.x * 1000);
                nextPos2.setY(coord2.y * 1000);
                cmdBuilder.addNextPosList(nextPos2);
            }
        }
        builder.setCMDContent(cmdBuilder.build().toByteString());
        ownerFighter.getBattleCmdList().addCMDList(builder);
    }

    private boolean calcNextEscapePos() {
        if (firstTarget == null || !firstTarget.isGuardGhostType()) {
            return false;
        }
        int deltaX = ownerFighter.getPos().getX() - firstTarget.getPos().getX();
        int deltaY = ownerFighter.getPos().getY() - firstTarget.getPos().getY();
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
        int newPosX = ownerFighter.getPos().getX() + escapeX;
        int newPosY = ownerFighter.getPos().getY() + escapeY;
        if (!ownerFighter.getRoom().getWorldMap().isPosReachable(newPosX, newPosY)) {
            return false;
        }
        if (movePath == null) {
            movePath = new ArrayList<>();
        }
        escapeCoord.x = newPosX;
        escapeCoord.y = newPosY;
        escapeCoord.toward.setX(escapeTowardX);
        escapeCoord.toward.setY(escapeTowardY);
        movePath.add(escapeCoord);

        escapeExtCoord.x = newPosX + escapeX;
        escapeExtCoord.y = newPosY + escapeY;
        escapeExtCoord.toward.setX(escapeTowardX);
        escapeExtCoord.toward.setY(escapeTowardY);
        movePath.add(escapeExtCoord);
        return true;
    }

    private ProtoVector.Builder randToward(ProtoVector.Builder deltaSpeed) {
        ProtoVector.Builder toward = ProtoVector.newBuilder();
        int rand = RandomUtils.nextInt(8);
        switch (rand) {
            case 0:
                toward.setX(1).setY(0);
                deltaSpeed.setX(10).setY(0);
                break;
            case 1:
                toward.setX(1).setY(1);
                deltaSpeed.setX(7).setY(7);
                break;
            case 2:
                toward.setX(0).setY(1);
                deltaSpeed.setX(0).setY(10);
                break;
            case 3:
                toward.setX(-1).setY(1);
                deltaSpeed.setX(-7).setY(7);
                break;
            case 4:
                toward.setX(-1).setY(0);
                deltaSpeed.setX(-10).setY(0);
                break;
            case 5:
                toward.setX(-1).setY(-1);
                deltaSpeed.setX(-7).setY(-7);
                break;
            case 6:
                toward.setX(0).setY(-1);
                deltaSpeed.setX(0).setY(-10);
                break;
            case 7:
                toward.setX(1).setY(-1);
                deltaSpeed.setX(7).setY(-7);
                break;
            default:
                LogUtil.error("build rand route error");
                break;
        }
        return toward;
    }

    private void buildRandRoute() {
        int rand = 5 + RandomUtils.nextInt(5);
        ProtoVector.Builder deltaSpeed = ProtoVector.newBuilder();
        ProtoVector.Builder randToward = randToward(deltaSpeed);
        for (int i = 0; i < rand; i++) {
            Coord coord = new Coord(ownerFighter.getPos().getX() + i * randToward.getX(), ownerFighter.getPos().getY() + i * randToward.getY());
            coord.toward.setX(deltaSpeed.getX()).setY(deltaSpeed.getY());
            if (movePath == null) {
                movePath = new ArrayList<>();
            }
            movePath.add(coord);
        }
    }

    private MistGhost checkTouch() {
        AoiNode aoiNode = ownerFighter.getRoom().getWorldMap().getAoiNodeById(ownerFighter.getAoiNodeKey());
        if (aoiNode == null) {
            return null;
        }
        List<MistObject> mistObjects = new ArrayList<>();
        aoiNode.getAllObjByType(mistObjects, MistUnitTypeEnum.MUT_Ghost_VALUE);
        MistGhost ghost;
        for (MistObject obj : mistObjects) {
            if (!(obj instanceof MistGhost)) {
                continue;
            }
            ghost = (MistGhost) obj;
            if (!ghost.canTouch(ownerFighter, true)) {
                continue;
            }
            if (MistConst.checkSamePos(ownerFighter.getPos(), ghost.getPos())) {
                return ghost;
            }
        }
        return null;
    }

    private boolean checkContinuePursue() {
        if (firstTarget == null) {
            return false;
        }
        if (!firstTarget.canTouch(ownerFighter, false)) {
            return false;
        }
        return MistConst.checkInDistance(MistConst.MistRobotPursueDistance,
                ownerFighter.getPos().getX(), ownerFighter.getPos().getY(), firstTarget.getPos().getX(), firstTarget.getPos().getY());
    }

    private void onIdleState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            if (searchTarget()) {
                if (firstTarget.isGuardGhostType()) {
                    if (calcNextEscapePos()) {
                        robotState = MistGhostState.escape;
                        addMovePathCmd(false);
                    }
                } else {
                    movePath = ownerFighter.getRoom().getWorldMap().findPath(astar, ownerFighter. getPos().build(), firstTarget.getPos().build());
                    robotState = MistGhostState.pursue;
                    addMovePathCmd(false);
                }
            } else {
                boolean moveRet = move(curTime - lastMoveTime);
                if (moveRet) {
                    MistGhost target = checkTouch();
                    if (target != null) {
                        addMovePathCmd(false);
                        ownerFighter.touchObj(target);
                        addMovePathCmd(true);
                    } else {
                        if (isPathEmpty()) {
                            addMovePathCmd(true);
                            buildRandRoute();
                        }
                        addMovePathCmd(false);
                    }
                } else if (isPathEmpty()) {
                    addMovePathCmd(true);
                    buildRandRoute();
                    addMovePathCmd(false);
                }
            }
            lastMoveTime = curTime;
        }
    }

    private void onEscapeState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                MistGhost target = checkTouch();
                if (target != null) {
                    ownerFighter.touchObj(target);
                } else if (searchTarget()) {
                    if (firstTarget.isGuardGhostType()) {
                        if (calcNextEscapePos()) {
                            addMovePathCmd(false);
                        }
                    } else {
                        movePath = ownerFighter.getRoom().getWorldMap().findPath(astar, ownerFighter. getPos().build(), firstTarget.getPos().build());
                        robotState = MistGhostState.pursue;
                        addMovePathCmd(false);
                    }
                    addMovePathCmd(false);
                } else if (isPathEmpty()) {
                    robotState = MistGhostState.idle;
                    addMovePathCmd(true);
                }
            } else if (isPathEmpty()) {
                robotState = MistGhostState.idle;
                addMovePathCmd(true);
            }
            lastMoveTime = curTime;
        }
    }

    private void onPursueState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                MistGhost target = checkTouch();
                if (target != null) {
                    ownerFighter.touchObj(target);
                    robotState = MistGhostState.idle;
                    firstTarget = null;
                    addMovePathCmd(true);
                } else if (!checkContinuePursue()) { // 进判断状态
                    robotState = MistGhostState.idle;
                    firstTarget = null;
                    addMovePathCmd(true);
                } else {
                    if (isPathEmpty() || !movePath.get(movePath.size() - 1).equals(firstTarget.getPos())) {
                        LogUtil.debug("GuardGhost[" + ownerFighter.getId() + "] findPath for targetChangePos");
                        movePath = ownerFighter.getRoom().getWorldMap().findPath(astar, ownerFighter.getPos().build(), firstTarget.getPos().build());
                    }
                    addMovePathCmd(false);
                }
            } else if (isPathEmpty()) {
                robotState = MistGhostState.idle;
                firstTarget = null;
                addMovePathCmd(true);
            }
            lastMoveTime = curTime;
        }
    }

    protected boolean tickCond() {
        if (ownerFighter == null) {
            return false;
        }
        if (ownerFighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsPunishing_VALUE) > 0) {
            return false;
        }
        return true;
    }

    public void onTick(long curTime) {
        if (!tickCond()) {
            return;
        }
        switch (robotState) {
            case MistRobotState.idle:
                onIdleState(curTime);
                break;
            case MistRobotState.escape:
                onEscapeState(curTime);
                break;
            case MistRobotState.pursue:
                onPursueState(curTime);
                break;
            default:
                break;
        }
    }
}
