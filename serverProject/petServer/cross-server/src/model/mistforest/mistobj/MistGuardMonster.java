package model.mistforest.mistobj;

import cfg.MistGuardMonsterPosConfig;
import cfg.MistGuardMonsterPosConfigObject;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistGuardMonsterState;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.map.AStar.Astar;
import model.mistforest.map.AStar.Coord;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MistForest.BattleCMD_MovePath;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.ProtoVector;
import util.TimeUtil;

public class MistGuardMonster extends MistObject {
    protected Astar astar;
    protected List<ProtoVector> posList;
    protected int moveIndex;
    protected List<Coord> patrolPath;
    protected long lastMoveTime;
    protected long updateStateTime;
    protected int guardState;

    public MistGuardMonster(MistRoom room, int objType) {
        super(room, objType);
        this.astar = new Astar();
        this.posList = new ArrayList<>();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);

        int pathCfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_GuardMonsterPathCfgId_VALUE);
        MistGuardMonsterPosConfigObject cfg = MistGuardMonsterPosConfig.getById(pathCfgId);
        if (null != cfg && null != cfg.getPatrolposlist()) {
            posList.addAll(cfg.getPatrolposlist());
        }
    }

    public void reborn() {
        guardState = MistGuardMonsterState.idle;
        updateStateTime = 0;
        astar.clear();
        posList.clear();
        if (patrolPath != null) {
            patrolPath.clear();
        }
        lastMoveTime = 0;
        super.reborn();
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

        if (stop || CollectionUtils.isEmpty(patrolPath)) {
            ProtoVector.Builder nextPos = ProtoVector.newBuilder();
            nextPos.setX(-1); // 负数表示停止
            nextPos.setY(-1);
            cmdBuilder.addNextPosList(nextPos);
        } else {
            ProtoVector.Builder nextPos1 = ProtoVector.newBuilder();
            Coord coord1 = patrolPath.get(0);
            nextPos1.setX(coord1.x * 1000);
            nextPos1.setY(coord1.y * 1000);
            cmdBuilder.addNextPosList(nextPos1);
            if (patrolPath.size() > 1) {
                ProtoVector.Builder nextPos2 = ProtoVector.newBuilder();
                Coord coord2 = patrolPath.get(1);
                nextPos2.setX(coord2.x * 1000);
                nextPos2.setY(coord2.y * 1000);
                cmdBuilder.addNextPosList(nextPos2);
            }
        }
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void onIdleState(long curTime) {
        if (updateStateTime + 3 * TimeUtil.MS_IN_A_S > curTime) {
            return;
        }
        if (moveIndex < 0 || moveIndex >= posList.size()) {
            moveIndex = 0;
        }
        ProtoVector pos = posList.get(moveIndex++);
        if (MistConst.checkSamePos(getPos().getX(), getPos().getY(), pos.getX(), pos.getY())) {
//            setToward(-getToward().getX(), -getToward().getY());
//            addChangePosInfoCmd(getPos().build(), getToward().build());
            updateStateTime = curTime;
        } else {
            patrolPath = room.getWorldMap().findPath(astar, getPos().build(), pos);
            guardState = MistGuardMonsterState.patrol;
            lastMoveTime = curTime;
            updateStateTime = curTime;
            addMovePathCmd(false);
        }
    }

    private boolean move(long interval) {
        if (CollectionUtils.isEmpty(patrolPath)) {
            return false;
        }
        Coord curPathNode = patrolPath.get(0);
        if (curPathNode.equals(getPos().build())) {
            patrolPath.remove(0);
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
            patrolPath.remove(0);
            posChanged = true;
        }
        if (posChanged) {
            setMiniPos(0, 0);
        } else {
            setMiniPos(moveMiniX, moveMiniY);
        }

        return posChanged;
    }

    protected void onPatrolState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean stopFlag = true;
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                if (!CollectionUtils.isEmpty(patrolPath)) {
                    Coord coord = patrolPath.get(patrolPath.size() - 1);
                    if (!MistConst.checkSamePos(getPos().getX(), getPos().getY(), coord.x,coord.y)) {
                        stopFlag = false;
                        addMovePathCmd(false);
                    }
                }
            } else if (!CollectionUtils.isEmpty(patrolPath)) {
                stopFlag = false;
            }
            if (stopFlag) {
                guardState = MistGuardMonsterState.idle;
                updateStateTime = curTime;
                addMovePathCmd(true);
            }

            lastMoveTime = curTime;
        }
    }

    protected void onArrestState(long curTime) {
        long arrestStateTime = getAttribute(MistUnitPropTypeEnum.MUPT_GuardMonsterArrestTime_VALUE);
        if (updateStateTime + arrestStateTime > curTime) {
            return;
        }
        int buffId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_GuardMonsterBuffId_VALUE);
        getBufMachine().removeBuff(buffId);
        guardState = MistGuardMonsterState.idle;
        updateStateTime = curTime;
    }

    public void beTouch(MistFighter fighter) {
        Long posData = MistConst.buildComboRebornPos((int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE));
        HashMap<Integer, Long> param = new HashMap<>();
        param.put(MistTriggerParamType.TranPosData, posData);
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.ArrestedByGuardMonster, this, param);

        if (guardState != MistGuardMonsterState.arrest) {
            if (guardState == MistGuardMonsterState.patrol) {
                addMovePathCmd(true);
            }
            guardState = MistGuardMonsterState.arrest;
            updateStateTime = GlobalTick.getInstance().getCurrentTime();
        }
        int buffId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_GuardMonsterBuffId_VALUE);
        if (buffId > 0 && bufMachine.getBuff(buffId) == null) {
            bufMachine.addBuff(buffId,this, null);
        }
    }

    public void updateGuardState(long curTime) {
        if (CollectionUtils.isEmpty(posList)) {
            return;
        }
        switch (guardState) {
            case MistGuardMonsterState.idle: {
                onIdleState(curTime);
                break;
            }
            case MistGuardMonsterState.patrol:{
                onPatrolState(curTime);
                break;
            }
            case MistGuardMonsterState.arrest:{
                onArrestState(curTime);
                break;
            }
            default:
                break;
        }
    }

    protected void checkMasterAlive(long curTime) {
        if (isAlive()) {
            long masterId = getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE);
            MistObject masterObj = getRoom().getObjManager().getMistObj(masterId);
            if (null == masterObj || !masterObj.isAlive()) {
                dead();
            } else {
                updateGuardState(curTime);
            }
        }
    }

    @Override
    public void onTick(long curTime) {
        checkMasterAlive(curTime);
        super.onTick(curTime);
    }
}
