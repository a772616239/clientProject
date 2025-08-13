package model.mistforest.mistobj.activityboss;

import java.util.List;
import model.mistforest.MistConst;
import model.mistforest.map.AStar.Astar;
import model.mistforest.map.AStar.Coord;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MistForest.BattleCMD_MovePath;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.ServerTransfer.EnumMistPveBattleType;

public class MistSlimeMonster extends MistObject {
    private Astar astar;
    private List<Coord> movePath;
    private long lastMoveTime;

    public MistSlimeMonster(MistRoom room, int objType) {
        super(room, objType);
        astar = new Astar();
    }

    public void initByMaster(MistObject obj) {
        super.initByMaster(obj);
        movePath = room.getWorldMap().findPath(astar, getPos().build(), obj.getPos().build());
    }

    private boolean move(long interval) {
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
            room.getWorldMap().objMove(this, oldX, oldY);
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
        battleCmdList.addCMDList(builder);
    }

    public void checkTouchMaster() {
        MistBossSlime bossSlime = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
        if (null == bossSlime){
            dead();
            return;
        }
        if (!MistConst.checkSamePos(getPos(), bossSlime.getPos())) {
            return;
        }
        touchMaster(bossSlime);
    }

    public void touchMaster(MistBossSlime bossSlime) {
        if (!isAlive()) {
            return;
        }
        long curHp = bossSlime.getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
        long maxHp = bossSlime.getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
        curHp = Math.min(curHp + maxHp * 20 / 1000, maxHp);
        bossSlime.setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        bossSlime.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        dead();
    }

    public void moveToMaster(long curTime) {
        if (!isAlive()) {
            return;
        }
        if (CollectionUtils.isEmpty(movePath)) {
            return;
        }
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean stopFlag = true;
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                if (!CollectionUtils.isEmpty(movePath)) {
                    Coord coord = movePath.get(movePath.size() - 1);
                    if (!MistConst.checkSamePos(getPos().getX(), getPos().getY(), coord.x,coord.y)) {
                        stopFlag = false;
                        addMovePathCmd(false);
                    }
                }
            } else if (!CollectionUtils.isEmpty(movePath)) {
                stopFlag = false;
            }
            if (stopFlag) {
                addMovePathCmd(true);
            }
            lastMoveTime = curTime;
        }
    }

    public void beTouch(MistFighter fighter) {
        if (fighter.isBattling()) {
            return;
        }
        if (!isAlive()) {
            return;
        }
        fighter.enterPveBattle(EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE, this);
    }

    public void settleDamage(long damage) {
        if (!isAlive()) {
            return;
        }
        long curHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
        curHp = Math.max(0, curHp - damage);
        setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, curHp);
        if (curHp > 0) {
            return;
        }
        dead();
    }

    @Override
    public void onTick(long curTime) {
        checkTouchMaster();
        moveToMaster(curTime);
        super.onTick(curTime);
    }
}
