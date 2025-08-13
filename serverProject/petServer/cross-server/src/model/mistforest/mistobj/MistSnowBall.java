package model.mistforest.mistobj;

import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.map.AStar.Coord;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.BattleCMD_MovePath;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;

public class MistSnowBall extends MistObject{
    protected ProtoVector.Builder deltaPos;
    protected List<Coord> movePath;
    protected long lastMoveTime;
    protected List<MistFighter> aroundFighters;

    public MistSnowBall(MistRoom room, int objType) {
        super(room, objType);
        deltaPos = ProtoVector.newBuilder();
        movePath = new ArrayList<>();
        aroundFighters = new ArrayList<>();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        calcMoveToward();
        generateMovePath();
    }

    @Override
    public void reborn() {
        super.reborn();
        calcMoveToward();
        generateMovePath();
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        if (fighter.checkBeTouchedSnowBall(getId())) {
            return;
        }
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.SnowBallId, getId());
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchSnowBall, this, params);
    }

    protected void calcMoveToward() {
        int rand = RandomUtils.nextInt(8);
        switch (rand) {
            case 0:{
                toward.setX(10);
                toward.setY(0);
                deltaPos.setX(1);
                deltaPos.setY(0);
                break;
            }
            case 1:{
                toward.setX(7);
                toward.setY(7);
                deltaPos.setX(1);
                deltaPos.setY(1);
                break;
            }
            case 2:{
                toward.setX(0);
                toward.setY(10);
                deltaPos.setX(0);
                deltaPos.setY(1);
                break;
            }
            case 3:{
                toward.setX(-7);
                toward.setY(7);
                deltaPos.setX(-1);
                deltaPos.setY(1);
                break;
            }
            case 4:{
                toward.setX(-10);
                toward.setY(0);
                deltaPos.setX(-1);
                deltaPos.setY(0);
                break;
            }
            case 5:{
                toward.setX(-7);
                toward.setY(-7);
                deltaPos.setX(-1);
                deltaPos.setY(-1);
                break;
            }
            case 6:{
                toward.setX(0);
                toward.setY(-10);
                deltaPos.setX(0);
                deltaPos.setY(-1);
                break;
            }
            case 7:{
                toward.setX(7);
                toward.setY(-7);
                deltaPos.setX(1);
                deltaPos.setY(-1);
                break;
            }
        }
    }

    protected void generateMovePath() {
        int posX = getPos().getX();
        int posY = getPos().getY();
        for (int i = 0; i < getRoom().getWorldMap().getMaxLength(); i++) {
            posX += deltaPos.getX();
            posY += deltaPos.getY();
            if (!getRoom().getWorldMap().isPosReachable(posX, posY)) {
                break;
            }
            Coord coord = new Coord(posX, posY);
            coord.toward.setX(toward.getX()).setY(toward.getY());
            movePath.add(coord);
        }
        addMovePathCmd(false);
        lastMoveTime = GlobalTick.getInstance().getCurrentTime();
    }

    public boolean move(long interval) {
        if (CollectionUtils.isEmpty(movePath)) {
            return false;
        }
        Coord curPathNode = movePath.get(0);
        if (curPathNode.equals(getPos().build())) {
            movePath.remove(0);
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
            setToward(curPathNode.toward.getX() * 1000, curPathNode.toward.getY() * 1000);
            getRoom().getWorldMap().objMove(this, oldX, oldY);
            movePath.remove(0);
            posChanged = true;
        }
        if (posChanged) {
            setMiniPos(0, 0);
        } else {
            setMiniPos(moveMiniX, moveMiniY);
        }
        return posChanged;
    }

    protected void addMovePathCmd(boolean stop) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_MovePath);
        BattleCMD_MovePath.Builder cmdBuilder = BattleCMD_MovePath.newBuilder();
        cmdBuilder.setTargetId(getId());
        ProtoVector.Builder curPos = ProtoVector.newBuilder();
        curPos.setX(getPos().getX() * 1000 + getMiniPos().getX());
        curPos.setY(getPos().getY() * 1000 + getMiniPos().getY());
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
        getBattleCmdList().addCMDList(builder);
    }

    protected boolean isPathEmpty() {
        return movePath == null || movePath.isEmpty();
    }

    protected boolean checkNexPosValid() {
        if (isPathEmpty()) {
            return false;
        }
        Coord coord = movePath.get(0);
        if (coord == null) {
            return false;
        }
        return getRoom().getWorldMap().isPosReachable(coord.x, coord.y);
    }

    protected void checkTouchPlayers() {
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode == null) {
            return;
        }
        aoiNode.getAllObjByType(aroundFighters, MistUnitTypeEnum.MUT_Player_VALUE);
        if (aroundFighters.isEmpty()) {
            return;
        }
        for (MistFighter fighter : aroundFighters) {
            if (MistConst.checkSamePos(fighter.getPos(), getPos())) {
                beTouch(fighter);
            }
        }
        aroundFighters.clear();
    }

    protected void onSnowBallRoll(long curTime) {
        if (!isAlive()) {
            return;
        }
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime);
            lastMoveTime = curTime;
            if (posChanged) {
                checkTouchPlayers();
                if (!checkNexPosValid()) {
                    addMovePathCmd(true);
                    dead();
                } else {
                    addMovePathCmd(false);
                }
            } else if (isPathEmpty()) {
                addMovePathCmd(true);
                dead();
            }
        }
    }

    @Override
    public void onTick(long curTime) {
        onSnowBallRoll(curTime);
        super.onTick(curTime);
    }
}
