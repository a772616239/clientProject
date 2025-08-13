package model.mistforest.mistobj;

import cfg.MistGuardMonsterPosConfig;
import cfg.MistGuardMonsterPosConfigObject;
import java.util.List;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistNormalState;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.map.AStar.Astar;
import model.mistforest.map.AStar.Coord;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_MovePath;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import util.TimeUtil;

public class MistTyphoon extends MistObject {
    protected Astar astar;
    protected List<Coord> movePath;
    protected long updateStateTime;
    protected int moveIndex;
    protected long lastMoveTime;
    protected List<MistFighter> aroundFighters;
    protected List<ProtoVector> posList;
    protected int state;

    public MistTyphoon(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        int posCfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_PatrolPosCfgId_VALUE);
        MistGuardMonsterPosConfigObject cfg = MistGuardMonsterPosConfig.getById(posCfgId);
        if (null != cfg && null != cfg.getPatrolposlist()) {
            posList.addAll(cfg.getPatrolposlist());
        }
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchDriftSand, fighter, null);
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                room.buildMistTips(EnumMistTipsType.EMTT_UnitTriggered_VALUE, fighter, this, getType()), true);
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

    protected void onIdleState(long curTime) {
        if (updateStateTime + 1 * TimeUtil.MS_IN_A_S > curTime) {
            return;
        }
        if (moveIndex < 0 || moveIndex >= posList.size()) {
            moveIndex = 0;
        }
        ProtoVector pos = posList.get(moveIndex++);
        if (!MistConst.checkSamePos(getPos().getX(), getPos().getY(), pos.getX(), pos.getY())) {
            movePath = room.getWorldMap().findPath(astar, getPos().build(), pos);
            state = MistNormalState.patrol;
            lastMoveTime = curTime;
            updateStateTime = curTime;
            addMovePathCmd(false);
        }
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

    protected void onPatrolState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean stopFlag = true;
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                checkTouchPlayers();
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
                state = MistNormalState.idle;
                updateStateTime = curTime;
                addMovePathCmd(true);
            }

            lastMoveTime = curTime;
        }
    }

    protected void randMove(long curTime) {
        if (CollectionUtils.isEmpty(posList)) {
            return;
        }
        switch (state) {
            case MistNormalState.idle: {
                onIdleState(curTime);
                break;
            }
            case MistNormalState.patrol:{
                onPatrolState(curTime);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onTick(long curTime) {
        randMove(curTime);
        super.onTick(curTime);
    }
}
