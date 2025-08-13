package model.mistforest.mistobj;

import cfg.MistMonsterDropItem;
import common.GlobalData;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.List;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistMonsterState;
import model.mistforest.map.AStar.Astar;
import model.mistforest.map.AStar.Coord;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.mistobj.rewardobj.MistItem;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.BattleCMD_MovePath;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.UnitMetadata;
import protocol.ServerTransfer.EnumMistPveBattleType;
import util.LogUtil;

public class MistMonster extends MistObject {
    private Astar astar;
    private long updateStateTime;
    private List<MistFighter> aroundPlayerList;
    private MistFighter attackTarget;
    private List<Coord> pursuePath;
    private long lastMoveTime;

    private int monsterState;

    public MistMonster(MistRoom room, int objType) {
        super(room, objType);
        this.astar = new Astar();
        this.aroundPlayerList = new ArrayList<>();
    }

    @Override
    public void clear() {
        super.clear();
        this.aroundPlayerList.clear();
        this.attackTarget = null;
        if (null != this.pursuePath) {
            this.pursuePath.clear();
        }
        this.astar.clear();
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
    public void beTouch(MistFighter toucher) {
        if (canAttack(toucher) && toucher.enterPveBattle(EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE, this)) {
            enterBattle(toucher, GlobalTick.getInstance().getCurrentTime());
        }
    }

    public void reborn() {
        monsterState = MistMonsterState.idle;
        updateStateTime = 0;
        astar.clear();
        aroundPlayerList.clear();
        if (pursuePath != null) {
            pursuePath.clear();
        }
        attackTarget = null;
        lastMoveTime = 0;
        super.reborn();
    }

    @Override
    public void dead() {
        super.dead();
        if (isDailyObj()) {
            room.getObjGenerator().decreaseDailyMonster(getId());
        }
    }

    protected void generateReward(MistFighter fighter) {
        if (fighter == null) {
            return;
        }
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode == null) {
            return;
        }
        ProtoVector.Builder newPos = aoiNode.getAvailablePos(getPos());
        if (newPos == null) {
            return;
        }
        int dropItemGroupId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_DropItemGroupId_VALUE);
        int dropItemType = MistMonsterDropItem.getDropItemType(dropItemGroupId);
        if (dropItemType > 0) {
            MistItem item = room.getObjManager().createObj(MistUnitTypeEnum.MUT_Item_VALUE);

            item.setAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, fighter.getId());
            item.setAttribute(MistUnitPropTypeEnum.MUPT_ItemType_VALUE, dropItemType);
            item.setPos(newPos.build());
            item.setAttribute(MistUnitPropTypeEnum.MUPT_RebornTime_VALUE, 0); // 掉落的道具不能重生
            item.afterInit(new int[]{newPos.getX(), newPos.getY()}, new int[]{getToward().getX(), getToward().getY()});

            item.addCreateObjCmd();
            fighter.addSelfVisibleTarget(item.getId());
            LogUtil.info("monster drop item:" + item.getId() + ",itemType=" + dropItemType);
        }
//        int dropItemGroupId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_DropBoxGroupId_VALUE);
//        MistMonsterDropBoxObject cfg = MistMonsterDropBox.getByGroupid(dropItemGroupId);
//        if (cfg == null) {
//            return;
//        }
//        int unitCfgId = MistMonsterDropBox.getDropBoxUnitCfgId(cfg);
//        if (unitCfgId <= 0) {
//            return;
//        }
//        MistOptionalBox optionalBox = room.getObjManager().createObj(MistUnitTypeEnum.MUT_SelfChooseBox_VALUE);
//        optionalBox.setAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, fighter.getId());
//        optionalBox.setAttribute(MistUnitPropTypeEnum.MUPT_SubBoxType_VALUE, 2); // 可选宝箱
//        optionalBox.setAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE, unitCfgId);
//        optionalBox.setAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE, cfg.getExisttime());
//        optionalBox.setAttribute(MistUnitPropTypeEnum.MUPT_RebornTime_VALUE, 0); // 掉落的道具不能重生
//        optionalBox.afterInit(new int[]{newPos.getX(), newPos.getY()}, new int[]{getToward().getX(), getToward().getY()});
//
//        optionalBox.addCreateObjCmd();
    }

    public void forceDead() {
        super.dead(); // 在循环中调用故不在此处移除dailyMonsterList列表
    }

    private boolean searchTarget() {
        if (getAttribute(MistUnitPropTypeEnum.MUPT_Speed_VALUE) <= 0) {
            return false;
        }
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

    public boolean canAttack(MistFighter fighter) {
        if (fighter == null) {
            return false;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE) > 0) {
            return false;
        }
        if (fighter.isBattling()) {
            return false;
        }
        if (fighter.isInSafeRegion()) {
            return false;
        }
        if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0) {
            return false;
        }
        if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE) > 0) {
            return false;
        }
        if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
            return false;
        }
        if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_SilentState_VALUE) > 0) {
            return false;
        }
        long grassGroup = getAttribute(MistUnitPropTypeEnum.MUPT_GrassGroup_VALUE);
        if (grassGroup > 0) {
            if (grassGroup != fighter.getAttribute(MistUnitPropTypeEnum.MUPT_GrassGroup_VALUE)) {
                return false;
            }
        } else {
            if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_GrassGroup_VALUE) > 0) {
                return false;
            }
        }
        MistPlayer owner = fighter.getOwnerPlayerInSameRoom();
        if (owner == null || GlobalData.getInstance().getServerChannel(owner.getServerIndex()) == null) {
            return false;
        }
        return true;
    }

    private boolean getNearestFighter() {
        int minDis = 0;
        int warnDis = (int) getAttribute(MistUnitPropTypeEnum.MUPT_Monster_WarnDis_VALUE);
        warnDis *= warnDis;
        for (MistFighter fighter1 : aroundPlayerList) {
            if (!canAttack(fighter1)) {
                continue;
            }
            int tmpDis = MistConst.getDistanceSqr(getPos().build(), fighter1.getPos().build());
            if (tmpDis > warnDis) {
                continue;
            }
            if (attackTarget == null || tmpDis <= minDis) {
                attackTarget = fighter1;
                minDis = tmpDis;
            }
        }
        aroundPlayerList.clear();
        return attackTarget != null;
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
            if (!canAttack(fighter)) {
                continue;
            }
            if (MistConst.checkSamePos(getPos(), fighter.getPos())) {
                return fighter;
            }
        }
        return null;
    }

    private boolean move(long interval) {
        if (isPathEmpty()) {
            return false;
        }
        Coord curPathNode = pursuePath.get(0);
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

    public void enterBattle(MistObject target, long curTime) {
        long battlePos = MistConst.protoPosToLongPos(target.getPos().build());
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, target.getId());
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, battlePos);

        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, battlePos);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, target.getId());

        addMovePathCmd(true);
        monsterState = MistMonsterState.attack;
        updateStateTime = curTime;
    }

    public void settleBattle(MistFighter fighter, boolean isWinner) {
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, 0);
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, 0);

        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, 0);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, 0);

        if (isWinner) {
            long curTime = GlobalTick.getInstance().getCurrentTime();
            pursuePath = room.getWorldMap().findPath(astar, getPos().build(), getInitPos().build());
            lastMoveTime = curTime;
            monsterState = MistMonsterState.endPursue;
            updateStateTime = curTime;
            addMovePathCmd(false);
        } else {
            generateReward(fighter);
            dead();
        }
    }

    private boolean isPathEmpty() {
        return pursuePath == null || pursuePath.isEmpty();
    }

    private boolean checkContinuePursue() {
        if (attackTarget == null) {
            return false;
        }
        if (!canAttack(attackTarget)) {
            return false;
        }
        int pursueDis = (int) getAttribute(MistUnitPropTypeEnum.MUPT_Monster_PursueDis_VALUE);
        return MistConst.checkInDistance( pursueDis, getPos().build(), getInitPos().build());
    }

    public void onIdleState(long curTime) {
        if (searchTarget()) {
            LogUtil.debug("monster[" + getId() + "] findPath for searchTarget id = " + attackTarget.getId());
            pursuePath = room.getWorldMap().findPath(astar, getPos().build(), attackTarget.getPos().build());
            if (!isPathEmpty()) {
                monsterState = MistMonsterState.pursue;
                updateStateTime = curTime;
                lastMoveTime = curTime;
                addMovePathCmd(false);
            } else {
                attackTarget = null;
            }
        }
    }

    public void onPursueState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                MistFighter target = checkTouch();
                if (target != null && target.enterPveBattle(EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE, this)) {
                    enterBattle(target, curTime);
                } else if (!checkContinuePursue()) {
                    LogUtil.debug("monster[" + getId() + "] findPath to init pos for cannotContinuePursue");
                    pursuePath = room.getWorldMap().findPath(astar, getPos().build(), getInitPos().build());
                    monsterState = MistMonsterState.endPursue;
                    updateStateTime = curTime;
                    addMovePathCmd(false);
                } else {
                    if (isPathEmpty() || !pursuePath.get(pursuePath.size() - 1).equals(attackTarget.getPos())) {
                        LogUtil.debug("monster[" + getId() + "] findPath to init pos for targetChangePos");
                        pursuePath = room.getWorldMap().findPath(astar, getPos().build(), attackTarget.getPos().build());
                    }
                    addMovePathCmd(false);
                }
            } else if (isPathEmpty()) {
                pursuePath = room.getWorldMap().findPath(astar, getPos().build(), getInitPos().build());
                monsterState = MistMonsterState.endPursue;
                updateStateTime = curTime;
                addMovePathCmd(false);
            }
            lastMoveTime = curTime;
        }
    }

    public void onAttackState(long curTime) {
        if (updateStateTime + 310000 < curTime) {
            settleBattle(null, true);
            LogUtil.debug("monster[" + getId() + "] findPath to init pos for battleTimeOut");
            pursuePath = room.getWorldMap().findPath(astar, getPos().build(), getInitPos().build());
            addMovePathCmd(false);
            monsterState = MistMonsterState.endPursue;
            lastMoveTime = curTime;
        }
    }

    public void onEndPursueState(long curTime) {
        if (lastMoveTime + MistConst.MistObjMoveInterval < curTime) {
            boolean posChanged = move(curTime - lastMoveTime);
            if (posChanged) {
                MistFighter target = checkTouch();
                if (target != null && target.enterPveBattle(EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE, this)) {
                    enterBattle(target, curTime);
                } else if (MistConst.checkSamePos(getPos(), getInitPos())) {
                    attackTarget = null;
                    monsterState = MistMonsterState.idle;
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
                attackTarget = null;
                monsterState = MistMonsterState.idle;
                updateStateTime = curTime;
            }
            lastMoveTime = curTime;
        }
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

        if (stop || isPathEmpty()) {
            ProtoVector.Builder nextPos = ProtoVector.newBuilder();
            nextPos.setX(-1); // 负数表示停止
            nextPos.setY(-1);
            cmdBuilder.addNextPosList(nextPos);
        } else {
            ProtoVector.Builder nextPos1 = ProtoVector.newBuilder();
            Coord coord1 = pursuePath.get(0);
            nextPos1.setX(coord1.x * 1000);
            nextPos1.setY(coord1.y * 1000);
            cmdBuilder.addNextPosList(nextPos1);
            if (pursuePath.size() > 1) {
                ProtoVector.Builder nextPos2 = ProtoVector.newBuilder();
                Coord coord2 = pursuePath.get(1);
                nextPos2.setX(coord2.x * 1000);
                nextPos2.setY(coord2.y * 1000);
                cmdBuilder.addNextPosList(nextPos2);
            }
        }
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    @Override
    public void onTick(long curTime) {
        if (isAlive()) {
            switch (monsterState) {
                case MistMonsterState.idle:
                    onIdleState(curTime);
                    break;
                case MistMonsterState.pursue:
                    onPursueState(curTime);
                    break;
                case MistMonsterState.attack:
                    onAttackState(curTime);
                    break;
                case MistMonsterState.endPursue:
                    onEndPursueState(curTime);
                    break;
                default:
                    break;
            }
        }
        super.onTick(curTime);
    }
}
