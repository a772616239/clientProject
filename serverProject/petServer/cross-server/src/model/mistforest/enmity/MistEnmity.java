package model.mistforest.enmity;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistEnmityState;
import model.mistforest.map.AStar.Astar;
import model.mistforest.map.AStar.Coord;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MistForest.BattleCMD_MovePath;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;

@Getter
@Setter
public class MistEnmity {
    protected MistObject owner;

    protected Astar astar;
    protected List<Coord> movePath;

    protected int enmityState;
    protected MistFighter dangerousFighter;
    protected List<MistFighter> aroundFighters;
    protected long updateStateTime;
    protected long lastMoveTime;


    public MistEnmity(MistObject obj) {
        this.owner = obj;
        this.aroundFighters = new ArrayList<>();
        this.astar = new Astar();
    }

    public void clear() {
        this.owner = null;
        this.enmityState = 0;
        this.dangerousFighter = null;
        this.astar.clear();
        if (aroundFighters != null) {
            aroundFighters.clear();
        }
        if (movePath != null) {
            movePath.clear();
        }
    }

    protected void faceToDangerousTarget() {
        if (dangerousFighter == null) {
            return;
        }
        int deltaY = 0;
        int deltaX = dangerousFighter.getPos().getX() > owner.getPos().getX() ? 10 : -10;
        if (deltaX == owner.getToward().getX() && deltaY == owner.getToward().getY()) {
            return;
        }
        owner.getToward().setX(deltaX).setY(deltaY).build();
        owner.addChangePosInfoCmd(owner.getPos().build(), owner.getToward().build());
    }

    protected int checkDangerousTarget(long checkDis) {
        AoiNode aoiNode = getOwner().getRoom().getWorldMap().getAoiNodeById(getOwner().getAoiNodeKey());
        if (aoiNode == null) {
            return MistEnmityState.normal;
        }
        aoiNode.getAroundObjByType(aroundFighters, MistUnitTypeEnum.MUT_Player_VALUE);
        if (CollectionUtils.isEmpty(aroundFighters)) {
            return MistEnmityState.normal;
        }

        int tmpDisSqr = -1;
        checkDis *= checkDis;
        dangerousFighter = null;
        for (MistFighter fighter : aroundFighters ) {
            if (!fighter.isMoving()) {
                continue;
            }
            if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
                continue;
            }
            int disSqr = MistConst.getDistanceSqr(getOwner().getPos().getX(), getOwner().getPos().getY(), fighter.getPos().getX(), fighter.getPos().getY());
            if (disSqr >= checkDis) {
                continue;
            }
            if (tmpDisSqr < 0 || tmpDisSqr > disSqr) {
                dangerousFighter = fighter;
                tmpDisSqr = disSqr;
            }
        }
        long attackDis = owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfAttackDis_VALUE);
        attackDis *= attackDis;
        if (tmpDisSqr < 0) {
            return MistEnmityState.normal;
        } else if (tmpDisSqr <=  attackDis) {
            return MistEnmityState.attack;
        } else {
            return MistEnmityState.warning;
        }
    }

    protected void calcMovePath() {
        movePath = getOwner().getRoom().getWorldMap().findPath(astar, dangerousFighter.getPos().build(), getOwner().getPos().build());
    }

    public boolean move(long interval, long speed) {
        if (CollectionUtils.isEmpty(movePath)) {
            return false;
        }
        Coord curPathNode = movePath.get(0);
        if (curPathNode.equals(owner.getPos().build())) {
            movePath.remove(0);
            return false;
        }
        boolean posChanged = false;
        long disX = Math.abs(speed * curPathNode.toward.getX()) / 10 * interval / 1000; // X轴行进距离 除以10是由于标准向量扩大了10倍
        long disY = Math.abs(speed * curPathNode.toward.getY()) / 10 * interval / 1000; // Y轴行进距离
        int moveMiniX = (int) (owner.getMiniPos().getX() + disX);
        int moveMiniY = (int) (owner.getMiniPos().getY() + disY);
        if (moveMiniX / 1000 > 0 || moveMiniY / 1000 > 0) {
            // 单位时间移动位置不能大于1000
            int oldX = owner.getPos().getX();
            int oldY = owner.getPos().getY();
            owner.setPos(curPathNode.x, curPathNode.y);
            owner.setToward(curPathNode.toward.getX() * 1000, curPathNode.toward.getY() * 1000);
            owner.getRoom().getWorldMap().objMove(owner, oldX, oldY);
            movePath.remove(0);
            posChanged = true;
        }
        if (posChanged) {
            owner.setMiniPos(0, 0);
        } else {
            owner.setMiniPos(moveMiniX, moveMiniY);
        }
        return posChanged;
    }

    protected void addMovePathCmd(boolean stop) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_MovePath);
        BattleCMD_MovePath.Builder cmdBuilder = BattleCMD_MovePath.newBuilder();
        cmdBuilder.setTargetId(owner.getId());
        ProtoVector.Builder curPos = ProtoVector.newBuilder();
        curPos.setX(owner.getPos().getX() * 1000 + owner.getMiniPos().getX());
        curPos.setY(owner.getPos().getY() * 1000 + owner.getMiniPos().getY());
        cmdBuilder.setCurrentPos(curPos);

        if (stop || CollectionUtils.isEmpty(movePath)) {
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
        owner.getBattleCmdList().addCMDList(builder);
    }

    protected boolean isPathEmpty() {
        return movePath == null || movePath.isEmpty();
    }

    protected boolean checkContinuePursue() {
        if (dangerousFighter == null) {
            return false;
        }
//        if (!canAttack(attackTarget)) {
//            return false;
//        }
        int pursueDis = (int) owner.getAttribute(MistUnitPropTypeEnum.MUPT_Monster_PursueDis_VALUE);
        return MistConst.checkInDistance(pursueDis, owner.getPos().build(), owner.getInitPos().build());
    }

    protected boolean checkTouchTargets() {
        AoiNode aoiNode = owner.getRoom().getWorldMap().getAoiNodeById(owner.getAoiNodeKey());
        if (aoiNode == null) {
            return false;
        }
        aoiNode.getAllObjByType(aroundFighters, MistUnitTypeEnum.MUT_Player_VALUE);
        if (aroundFighters.isEmpty()) {
            return false;
        }
        boolean touchPursueTarget = false;
        for (MistFighter fighter : aroundFighters) {
            if (MistConst.checkSamePos(fighter.getPos(), owner.getPos())) {
                owner.beTouch(fighter);
                if (dangerousFighter != null && fighter.getId() == dangerousFighter.getId()) {
                    touchPursueTarget = true;
                }
            }
        }
        return touchPursueTarget;
    }

    protected void pursueTarget(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime, owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfPursueSpeed_VALUE));
            if (posChanged) {
                boolean touchPursueTarget = checkTouchTargets();
                if (touchPursueTarget || !checkContinuePursue()) {
                    movePath = owner.getRoom().getWorldMap().findPath(astar, owner.getPos().build(), owner.getInitPos().build());
                    dangerousFighter = null;
                    setEnmityState(MistEnmityState.reback);
                    updateStateTime = curTime;
                    addMovePathCmd(false);
                } else {
                    if (isPathEmpty() || !movePath.get(movePath.size() - 1).equals(dangerousFighter.getPos())) {
                        movePath = owner.getRoom().getWorldMap().findPath(astar, owner.getPos().build(), dangerousFighter.getPos().build());
                    }
                    addMovePathCmd(false);
                }
            } else if (isPathEmpty()) {
                movePath = owner.getRoom().getWorldMap().findPath(astar, owner.getPos().build(), owner.getInitPos().build());
                dangerousFighter = null;
                setEnmityState(MistEnmityState.reback);
                updateStateTime = curTime;
                addMovePathCmd(false);
            }
            lastMoveTime = curTime;
        }
    }

    protected void onNormalState(long curTime) {
        int enmityState = checkDangerousTarget(owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfWarningDis_VALUE));
        if (dangerousFighter != null) {
            if (enmityState == MistEnmityState.attack) {
                setEnmityState(enmityState);
                owner.setAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.attack);
                owner.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.attack);
                calcMovePath();
                updateStateTime = curTime;
            } else {
                setEnmityState(MistEnmityState.warning);
                owner.setAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.warning);
                owner.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.warning);
                faceToDangerousTarget();
                updateStateTime = curTime;
            }
        } else if (owner.getAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE) != MistEnmityState.normal) {
            owner.setAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.normal);
            owner.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.normal);
        }
    }

    protected void onWarningState(long curTime) {
        int enmityState = checkDangerousTarget(owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfWarningDis_VALUE));
        if (dangerousFighter == null) {
            owner.setAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.normal);
            owner.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.normal);
            setEnmityState(MistEnmityState.normal);
            updateStateTime = 0;
        } else if (enmityState == MistEnmityState.attack) {
            setEnmityState(MistEnmityState.attack);
            owner.setAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.attack);
            owner.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.attack);
            calcMovePath();
            updateStateTime = curTime;
        } else {
            if (updateStateTime > 0 && updateStateTime + owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfWarningTime_VALUE) <= curTime) {
                setEnmityState(MistEnmityState.fury);
                owner.setAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.fury);
                owner.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.fury);
                updateStateTime = curTime;
            }
            faceToDangerousTarget();
        }
    }

    protected void onFuryState(long curTime) {
        int enmityState = checkDangerousTarget(owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfWarningDis_VALUE));
        if (dangerousFighter == null) {
            owner.setAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.normal);
            owner.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.normal);
            setEnmityState(MistEnmityState.normal);
            updateStateTime = 0;
        } else {
            if (enmityState == MistEnmityState.attack || updateStateTime > 0 && updateStateTime + owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfFuryTime_VALUE) <= curTime) {
                setEnmityState(MistEnmityState.attack);
                owner.setAttribute(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.attack);
                owner.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_EnmityState_VALUE, MistEnmityState.attack);
                calcMovePath();
                updateStateTime = curTime;
            } else {
                faceToDangerousTarget();
            }
        }
    }

    protected void onAttackState(long curTime) {
        if (dangerousFighter == null) {
            movePath = owner.getRoom().getWorldMap().findPath(astar, owner.getPos().build(), owner.getInitPos().build());
            setEnmityState(MistEnmityState.reback);
            updateStateTime = curTime;
        } else {
            pursueTarget(curTime);
        }
    }

    protected void onRebackState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime, owner.getAttribute(MistUnitPropTypeEnum.MUPT_Speed_VALUE));
            if (posChanged) {
                checkTouchTargets();
                if (MistConst.checkSamePos(owner.getPos(), owner.getInitPos())) {
                    setEnmityState(MistEnmityState.normal);
                    updateStateTime = curTime;
                    addMovePathCmd(true);
                } else if (isPathEmpty()) {
                    int oldX = owner.getPos().getX();
                    int oldY = owner.getPos().getY();
                    owner.setPos(owner.getInitPos().build());
                    owner.addChangePosInfoCmd(owner.getPos().build(), owner.getToward().build());
                    owner.getRoom().getWorldMap().objMove(owner, oldX, oldY);
                } else {
                    addMovePathCmd(false);
                }
            } else if (isPathEmpty()) {
                if (!MistConst.checkSamePos(owner.getPos(), owner.getInitPos())) {
                    int oldX = owner.getPos().getX();
                    int oldY = owner.getPos().getY();
                    owner.setPos(owner.getInitPos().build());
                    owner.addChangePosInfoCmd(owner.getPos().build(), owner.getToward().build());
                    owner.getRoom().getWorldMap().objMove(owner, oldX, oldY);
                }
                setEnmityState(MistEnmityState.normal);
                updateStateTime = curTime;
            }
            lastMoveTime = curTime;
        }
    }

    public void onTick(long curTime) {
        switch (enmityState) {
            case MistEnmityState.normal : {
                onNormalState(curTime);
                break;
            }
            case MistEnmityState.warning : {
                onWarningState(curTime);
                break;
            }
            case MistEnmityState.fury : {
                onFuryState(curTime);
                break;
            }
            case MistEnmityState.attack : {
                onAttackState(curTime);
                break;
            }
            case MistEnmityState.reback : {
                onRebackState(curTime);
                break;
            }
        }
    }
}
