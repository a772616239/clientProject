package model.mistforest.enmity;

import java.util.ArrayList;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistEnmityState;
import model.mistforest.map.AStar.Coord;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.mistobj.MistDeer;
import model.mistforest.mistobj.MistFighter;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;

public class MistEscapeEnmity extends MistEnmity {

    public MistEscapeEnmity(MistDeer obj) {
        super(obj);
    }

    @Override
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
            int disSqr = MistConst.getDistanceSqr(getOwner().getPos().getX(), getOwner().getPos().getY(), fighter.getPos().getX(), fighter.getPos().getY());
            if (disSqr >= checkDis) {
                continue;
            }
            if (tmpDisSqr < 0 || tmpDisSqr > disSqr) {
                dangerousFighter = fighter;
                tmpDisSqr = disSqr;
            }
        }
        return tmpDisSqr < 0 ? MistEnmityState.normal : MistEnmityState.warning;
    }

    private boolean calcNextEscapePos() {
        if (dangerousFighter == null) {
            return false;
        }
        int deltaX = owner.getPos().getX() - dangerousFighter.getPos().getX();
        int deltaY = owner.getPos().getY() - dangerousFighter.getPos().getY();
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
        int newPosX = owner.getPos().getX();
        int newPosY = owner.getPos().getY();
        if (movePath == null) {
            movePath = new ArrayList<>();
        }
        for (int i = 0; i < 5; i++) {
            newPosX += escapeX;
            newPosY += escapeY;
            if (!owner.getRoom().getWorldMap().isPosValid(newPosX, newPosY)) {
                break;
            }
            Coord coord = new Coord(newPosX, newPosY);
            coord.toward.setX(escapeTowardX);
            coord.toward.setY(escapeTowardY);
            movePath.add(coord);
        }
        return true;
    }

    protected void calcMovePath() {
        calcNextEscapePos();
    }

    protected void escape(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime, owner.getAttribute(MistUnitPropTypeEnum.MUPT_WolfPursueSpeed_VALUE));
            if (posChanged) {
                updateStateTime = curTime;
                addMovePathCmd(false);
            } else if (isPathEmpty()) {
                updateStateTime = curTime;
                addMovePathCmd(true);
                owner.dead();
            }
            lastMoveTime = curTime;
        }
    }
    
    @Override
    protected void onAttackState(long curTime) {
        escape(curTime);
    }
}
