package model.mistforest.mistobj;

import cfg.MistComboBornPosConfigObject;
import cfg.MistDropObjConfig;
import cfg.MistDropObjConfigObject;
import cfg.MistObjDefaultProp;
import cfg.MistObjDefaultPropObject;
import cfg.MistRebornChangeProp;
import cfg.MistRebornChangePropObject;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistBuffInterruptType;
import model.mistforest.buff.Buff;
import model.mistforest.buff.BuffMachine;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_AddBuff;
import protocol.MistForest.BattleCMD_AddObj;
import protocol.MistForest.BattleCMD_Blink;
import protocol.MistForest.BattleCMD_BroacastTips;
import protocol.MistForest.BattleCMD_ChangePos;
import protocol.MistForest.BattleCMD_FlickAway;
import protocol.MistForest.BattleCMD_PropertyChange;
import protocol.MistForest.BattleCMD_RemoveBuff;
import protocol.MistForest.BattleCMD_RemoveObj;
import protocol.MistForest.BattleCMD_TriggerEffect;
import protocol.MistForest.BattleCMD_UpdateBuff;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.PropertyDict;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.UnitBuffData;
import protocol.MistForest.UnitMetadata;
import protocol.MistForest.UnitSnapShot;
import protocol.TransServerCommon.MistBornPosInfo;
import util.LogUtil;
import util.TimeUtil;

public class MistObject {
    protected long id;
    protected int type;
    protected MistRoom room;

    protected ProtoVector.Builder initPos;
    protected ProtoVector.Builder initToward;

    protected int aoiNodeKey;

    protected boolean isMoving;
    protected ProtoVector.Builder pos;
    protected ProtoVector.Builder miniPos;
    protected ProtoVector.Builder toward;

    protected int speedChangeRate;

    protected long createTimeStamp;
//    protected long deadTimeStamp;
    protected int rebornTime;

    protected Map<Integer, Long> attributes;
    protected BuffMachine bufMachine;

    protected SC_BattleCmd.Builder battleCmdList;

    protected List<Integer> rebornChangePropList;

    protected boolean isDailyObj; // 是否来源于每日对象创建

    protected List<Long> slaveObjList;

    protected boolean removeMsgFlag; // 发送移除对象消息标识

    protected int scheduleCfgId;
    protected int scheduleObjCfgId;

    public MistObject(MistRoom room, int objType) {
        this.room = room;
        this.attributes = new HashMap<>();
        this.bufMachine = new BuffMachine(this);
        this.initPos = ProtoVector.newBuilder().setX(-1).setY(-1);
        this.initToward = ProtoVector.newBuilder();
        this.pos = ProtoVector.newBuilder();
        this.miniPos = ProtoVector.newBuilder();
        this.toward = ProtoVector.newBuilder();
        this.battleCmdList = SC_BattleCmd.newBuilder();
        this.createTimeStamp = GlobalTick.getInstance().getCurrentTime();
        this.rebornChangePropList = new ArrayList<>();
        this.type = objType;
        this.id = room.getObjManager().generateId(objType);
    }

    public void init() {
        initProp();
    }

    public void initByMaster(MistObject obj) {
        setAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE, obj.getId());
    }

    public void afterInit(int[] initialPos, int[] initialToward) {
        initPos(initialPos, initialPos);
        initRebornTime();
        long lifeTime = getAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE);
        if (lifeTime > 0) {
            setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime() + lifeTime * TimeUtil.MS_IN_A_S);
        }
        recoverHp(true);

        if (getAttribute(MistUnitPropTypeEnum.MUPT_ShowInVipMiniMap_VALUE) > 0) {
            getRoom().getObjManager().addNeedShowObj(this);
        }
    }

    public void clear() {
        initPos.clear();
        initToward.clear();
        pos.clear();
        miniPos.clear();
        attributes.clear();
        bufMachine.clear();
        battleCmdList.clear();
        rebornChangePropList.clear();
        if (null != slaveObjList) {
            slaveObjList.clear();
        }
        removeMsgFlag = false;
    }

    public void initProp() {
        MistObjDefaultPropObject propCfg = MistObjDefaultProp.getInstance().getByType(getType());
        if (propCfg == null) {
            return;
        }
        setInitPos(propCfg.getInitpos()[0] / 1000, propCfg.getInitpos()[1] / 1000);
        setInitToward(propCfg.getInittoward()[0], propCfg.getInittoward()[1]);
        addAttributes(propCfg.getDefaultprop());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MistRoom getRoom() {
        return room;
    }

    public void setRoom(MistRoom room) {
        this.room = room;
    }

    public BuffMachine getBufMachine() {
        return bufMachine;
    }

    public void setBufMachine(BuffMachine bufMachine) {
        this.bufMachine = bufMachine;
    }

    public ProtoVector.Builder getInitPos() {
        return this.initPos;
    }

    public void setInitPos(int x, int y) {
        if (!room.getWorldMap().isPosValid(x, y)) {
            return;
        }
        this.initPos.setX(x);
        this.initPos.setY(y);
    }

    public ProtoVector.Builder getInitToward() {
        return this.initToward;
    }

    public void setInitToward(int towardX, int towardY) {
        this.initToward.setX(towardX);
        this.initToward.setY(towardY);
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public ProtoVector.Builder getPos() {
        return this.pos;
    }

    public void setPos(ProtoVector pos) {
        setPos(pos.getX(), pos.getY());
    }

    public void setPos(int x, int y) {
        if (!room.getWorldMap().isPosValid(x, y)) {
            return;
        }
        this.pos.setX(x);
        this.pos.setY(y);
    }

    public ProtoVector.Builder getMiniPos() {
        return miniPos;
    }

    public void setMiniPos(ProtoVector miniPos) {
        setMiniPos(miniPos.getX(), miniPos.getY());
    }

    public void setMiniPos(int x, int y) {
        if (x >= 1000 || x <= -1000 || y >= 1000 || y <= -1000) {
            return;
        }
        this.miniPos.setX(x);
        this.miniPos.setY(y);
    }

    public ProtoVector.Builder getToward() {
        return this.toward;
    }

    public void setToward(ProtoVector toward) {
        this.toward.setX(toward.getX());
        this.toward.setY(toward.getY());
    }

    public void setToward(int towardX, int towardY) {
        this.toward.setX(towardX);
        this.toward.setY(towardY);
    }

    public int getAoiNodeKey() {
        return aoiNodeKey;
    }

    public void setAoiNodeKey(int aoiNodeKey) {
        this.aoiNodeKey = aoiNodeKey;
    }

    public int getSpeedChangeRate() {
        return speedChangeRate;
    }

    public void setSpeedChangeRate(int speedChangeRate) {
        this.speedChangeRate = speedChangeRate;
    }

    public long calcRealSpeed() {
        long speed = getAttribute(MistUnitPropTypeEnum.MUPT_Speed_VALUE);
        return speed + speed * speedChangeRate / 1000;
    }

    public long getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }

    public long getDeadTimeStamp() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_DeadTimeStamp_VALUE);
    }

    public void setDeadTimeStamp(long deadTimeStamp) {
        setAttribute(MistUnitPropTypeEnum.MUPT_DeadTimeStamp_VALUE, deadTimeStamp);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_DeadTimeStamp_VALUE, deadTimeStamp);
    }

    public int getRebornTime() {
        return rebornTime;
    }

    public void setRebornTime(int rebornTime) {
        this.rebornTime = rebornTime;
    }

    public void initRebornTime() {
        int rebornTime = (int) getAttribute(MistUnitPropTypeEnum.MUPT_RebornTime_VALUE);
        if (rebornTime > 0) {
            rebornTime = Math.max(MistConst.MistDelayRemoveTime, rebornTime);
        }
        setRebornTime(rebornTime);
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsShowInMiniMap_VALUE) <= 0) {
            setAttribute(MistUnitPropTypeEnum.MUPT_RebornTime_VALUE, 0l); // 通用对象不同步rebornTime
        }
    }

    public Map<Integer, Long> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<Integer, Long> attributes) {
        this.attributes = attributes;
    }

    public void addAttributes(Map<Integer, Long> attributes) {
        this.attributes.putAll(attributes);
    }

    public void addAttributes(int[][] attributes) {
        if (attributes == null) {
            return;
        }
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i] == null || attributes[i].length < 2) {
                continue;
            }
            setAttribute(attributes[i][0], attributes[i][1]);
        }
    }

    public long getAttribute(int type) {
        return attributes.containsKey(type) ? attributes.get(type) : 0l;
    }

    public void setAttribute(int type, long value) {
        attributes.put(type, value);
    }

    public boolean isDailyObj() {
        return isDailyObj;
    }

    public void setDailyObj(boolean dailyObj) {
        isDailyObj = dailyObj;
    }

    public int getScheduleCfgId() {
        return scheduleCfgId;
    }

    public void setScheduleCfgId(int scheduleCfgId) {
        this.scheduleCfgId = scheduleCfgId;
    }

    public int getScheduleObjCfgId() {
        return scheduleObjCfgId;
    }

    public void setScheduleObjCfgId(int scheduleObjCfgId) {
        this.scheduleObjCfgId = scheduleObjCfgId;
    }

    public SC_BattleCmd.Builder getBattleCmdList() {
        return battleCmdList;
    }

    public void addAllRebornChangeProps(int[] props) {
        if (props == null || props.length <= 0) {
            return;
        }
        for (int prop : props) {
            rebornChangePropList.add(prop);
        }
    }
    public void addAllRebornChangeProps(List<Integer> props) {
        if (props == null || props.isEmpty()) {
            return;
        }
        rebornChangePropList.addAll(props);
    }

    public void initPos(int[] initialPos, int[] initialToward) {
        long complxPosId = getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        if (complxPosId > 0) {
            initComboPos();
        } else {
            if (initialPos != null && initialPos.length > 0 && getRoom().getWorldMap().isPosValid(initialPos[0], initialPos[1])) {
                setInitPos(initialPos[0], initialPos[1]);
                if (initialToward != null && initialToward.length > 0) {
                    setInitToward(initialToward[0], initialToward[1]);
                }
            } else {
                boolean isPrivateObj = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE) > 0;
                MistBornPosInfo posObj = room.getObjGenerator().getOutDoorRandomBornPosObj(getType(), isPrivateObj);
                if (posObj != null) {
                    setInitPos(posObj.getPos().getX(), posObj.getPos().getY());
                    setAttribute(MistUnitPropTypeEnum.MUPT_BornPosId_VALUE, posObj.getId());
                }
            }
        }

        setPos(initPos.build());
        setToward(initToward.build());
    }

    public void initComboPos() {
        long complxPosId = getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        if (complxPosId > 0) {
            MistComboBornPosConfigObject cfg = room.getObjGenerator().getComplxBornPosData(getType());
            if (null != cfg && null != cfg.getMasterobjpos() && cfg.getMasterobjpos().length >= 2) {
                setInitPos(cfg.getMasterobjpos()[0], cfg.getMasterobjpos()[1]);
                setPos(getInitPos().build());
                setAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE, cfg.getId());
            }
        }
    }

    public boolean isInSafeRegion() {
        return room.getWorldMap().isInSafeRegion(getPos().getX(), getPos().getY());
    }

    public boolean isAlive() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_LifeState_VALUE) == LifeStateEnum.LSE_Survival_VALUE;
    }

    public void removeObjFromMap() {
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode != null) {
            aoiNode.onObjLeave(this, null);
        } else if (getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0){
            getRoom().getObjGenerator().removeOverallObjId(getId());
            updateRealTimeBattleCmd(buildRemoveObjCmd());
        } else {
            long fighterId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
            if (fighterId > 0) {
                MistFighter fighter = getRoom().getObjManager().getMistObj(fighterId);
                if (null != fighter) {
                    fighter.removeSelfVisibleTarget(getId());
                    updateRealTimeBattleCmd(buildRemoveObjCmd());
                }
            }
        }
        removeMsgFlag = true;
    }

    public void beTouch(MistFighter fighter) {

    }

    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);
        setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime());
        boolean notRemoveFlag = getAttribute(MistUnitPropTypeEnum.MUPT_NotRemoveWhenDead_VALUE) > 0;
        if (!notRemoveFlag || getRebornTime() <= 0) {
//            removeObjFromMap();
        }
        if (getScheduleCfgId() > 0) {
            if (room.getScheduleManager() != null) {
                room.getScheduleManager().returnPosToPosController(getScheduleCfgId(), getScheduleObjCfgId(), getPos().build());
            }
        }
        int complxPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        if (complxPosId > 0) {
            room.getObjGenerator().resetComplxUsedPosIdData(getType(), complxPosId);
            setAttribute(MistUnitPropTypeEnum.MUPT_BornPosId_VALUE, -1); //-1表示是复杂复活点类型，但未生成对应配置id
        } else {
            int bornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_BornPosId_VALUE);
            room.getObjGenerator().resetUsedOutDoorBornPos(getType(), bornPosId);
            setAttribute(MistUnitPropTypeEnum.MUPT_BornPosId_VALUE, 0);
        }

        if (isDailyObj()) {
            room.getObjGenerator().decreaseDailyObjCount(getType());
        }
        removeFromMaster();

        if (getAttribute(MistUnitPropTypeEnum.MUPT_ShowInVipMiniMap_VALUE) > 0) {
            getRoom().getObjManager().removeNeedShowObj(getId());
        }
    }

    public void reborn() {
        setAttribute(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Survival_VALUE);
        rebornChangeProp();
        int[] pos = getScheduleCfgId() > 0 && room.getScheduleManager() != null ? room.getScheduleManager().getPosFromPosController(getScheduleCfgId(), getScheduleObjCfgId()) : null;
        initPos(pos, null);

        if (getAttribute(MistUnitPropTypeEnum.MUPT_NotRemoveWhenDead_VALUE) == 0) {
            if (getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0) {
                room.getObjGenerator().addOverallObjId(getId());
                addCreateObjCmd();
            } else if (getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE) > 0) {
                addCreateObjCmd();
            } else {
                room.getWorldMap().objFirstEnter(this);
            }
        } else {
            addChangePosInfoCmd(getPos().build(), getToward().build());
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Survival_VALUE);
        }

        long lifeTime = getAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE);
        if (lifeTime > 0) {
            setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime() + lifeTime * TimeUtil.MS_IN_A_S);
        } else {
            setDeadTimeStamp(0);
        }
        recoverHp(false);
        setCreateTimeStamp(GlobalTick.getInstance().getCurrentTime());

        if (getAttribute(MistUnitPropTypeEnum.MUPT_ShowInVipMiniMap_VALUE) > 0) {
            getRoom().getObjManager().addNeedShowObj(this);
        }
    }

    public void recoverHp(boolean init) {
        long maxHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE);
        if (maxHp > 0) {
            setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, maxHp);
            if (!init) {
                addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, maxHp);
            }
        }
    }

    public void rebornChangeProp() {
        if (rebornChangePropList == null || rebornChangePropList.isEmpty()) {
            return;
        }
        for (Integer cfgId : rebornChangePropList) {
            updateRandomProp(MistRebornChangeProp.getById(cfgId), true);
        }
    }

    public void updateRandomProp(MistRebornChangePropObject cfg, boolean broadcast) {
        if (cfg == null) {
            return;
        }
        if (cfg.getProptype() == null || cfg.getPropchange() == null) {
            return;
        }
        int sumOdds = 0;
        int rand = RandomUtils.nextInt(1000);
        for (int i = 0; i < cfg.getPropchange().length; i++) {
            if (cfg.getPropchange()[i]==null) {
                continue;
            }
            if (cfg.getPropchange()[i].length < 1) {
                continue;
            }
            sumOdds += cfg.getPropchange()[i][0];
            if (rand < sumOdds) {
                for (int j = 0; j < cfg.getProptype().length; j++) {
                    if (cfg.getPropchange()[i].length > j) {
                        setAttribute(cfg.getProptype()[j], cfg.getPropchange()[i][j+1]);
                        if (broadcast) {
                            addAttributeChangeCmd(cfg.getProptype()[j], cfg.getPropchange()[i][j+1]);
                        }
                    }
                }
                return;
            }
        }
    }

    public void changeCamp(long newCamp) {
        setAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE, newCamp);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_Group_VALUE, newCamp);
    }

    protected boolean isSpecialProp(int propType) {
        return false;
    }

    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata.Builder builder = UnitMetadata.newBuilder();
        builder.setUnitTypeValue(getType());
        PropertyDict.Builder builder1 = PropertyDict.newBuilder();
        for (Map.Entry<Integer, Long> entry : getAttributes().entrySet()) {
            if (MistConst.isOnlyServerUseProp(entry.getKey())) {
                continue;
            }
            if (isSpecialProp(entry.getKey())) {
                continue;
            }
            if (entry.getKey() == MistUnitPropTypeEnum.MUPT_Speed_VALUE) {
                builder1.addKeysValue(entry.getKey());
                builder1.addValues(calcRealSpeed());
            } else {
                builder1.addKeysValue(entry.getKey());
                builder1.addValues(entry.getValue());
            }
        }
        builder.setProperties(builder1);
        UnitSnapShot.Builder builder2 = UnitSnapShot.newBuilder();
        builder2.setUnitId(getId());
        ProtoVector.Builder posBuilder = ProtoVector.newBuilder();
        posBuilder.setX(getPos().getX() * 1000).setY(getPos().getY() * 1000);
        builder2.setPos(posBuilder);
        builder2.setToward(getToward());
        builder2.setIsMoving(isMoving());
        builder.setSnapShotData(builder2);

        List<UnitBuffData> buffList = bufMachine.getAllBuffData();
        if (buffList != null) {
            builder.addAllBuffData(buffList);
        }
        return builder.build();
    }

    public List<UnitMetadata> getSlaveMetaData(MistFighter fighter) {
        if (CollectionUtils.isEmpty(slaveObjList)) {
            return null;
        }
        List<UnitMetadata> metadataList = new ArrayList<>();
        for (Long id : slaveObjList) {
            MistObject obj = getRoom().getObjManager().getMistObj(id);
            if (null == obj) {
                continue;
            }
            metadataList.add(obj.getMetaData(fighter));
        }
        return metadataList;
    }

    public List<Long> getSlaveObjList() {
        return slaveObjList;
    }

    public void addSlaveObj(long id) {
        if (null == slaveObjList) {
            slaveObjList = new ArrayList<>();
        }
        slaveObjList.add(id);
    }

    public void removeSlaveId(long id) {
        if (CollectionUtils.isEmpty(slaveObjList)) {
            return;
        }
        slaveObjList.remove(id);
    }

    public void clearSlaveObj() {
        if (CollectionUtils.isEmpty(slaveObjList)) {
            return;
        }
        List<Long> tmpList = slaveObjList.stream().collect(Collectors.toList());
        for (Long slaveId : tmpList) {
            MistObject obj = getRoom().getObjManager().getMistObj(slaveId);
            if (null == obj) {
                continue;
            }
            obj.setRebornTime(0);
            if (obj.isAlive()) {
                obj.dead();
            } else if (obj.getAttribute(MistUnitPropTypeEnum.MUPT_NotRemoveWhenDead_VALUE) > 0) { // 幻象死亡时boss没死，幻象不移除，boss死亡时幻象为死亡状态就真正移除
                obj.removeObjFromMap();
            }
        }
    }

    public void removeFromMaster() {
        MistObject obj = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
        if (null != obj) {
            obj.removeSlaveId(getId());
        }
    }

    protected void sendSnapShotToPlayers(UnitSnapShot snapShot) {

    }

    public boolean checkSnapShot(UnitSnapShot snapShot) {
        if (snapShot.getUnitId() != getId()) {
            return false;
        }
        if (!room.getWorldMap().isPosReachable(snapShot.getPos().getX() / 1000, snapShot.getPos().getY() / 1000)) {
            return false;
        }
        return true;
    }

    public void updateSnapShot(UnitSnapShot snapShot) {
        int oldX = getPos().getX();
        int oldY = getPos().getY();
        int newX = snapShot.getPos().getX() / 1000;
        int newY = snapShot.getPos().getY() / 1000;
        boolean posChanged = oldX != newX || oldY != newY;
        setToward(snapShot.getToward());
        setMoving(snapShot.getIsMoving());
        sendSnapShotToPlayers(snapShot);
        if (posChanged) {
            setPos(newX, newY);
            if (getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE) == 0
                    && getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) == 0) {
                room.getWorldMap().objMove(this, oldX, oldY);
            }
        }

        if (snapShot.getIsMoving()) {
            bufMachine.interruptBuffByType(MistBuffInterruptType.Move);
        }
    }

    protected void updateBattleCmd() {
        if (battleCmdList.getCMDListCount() <= 0) {
            return;
        }
        long visibleObjId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visibleObjId == 0) {
            boolean isOverallObj = getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0;
            if (isOverallObj) {
                getRoom().broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, battleCmdList, true);
            } else {
                AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
                if (aoiNode == null) {
                    return;
                }
                aoiNode.broadcastCmd(battleCmdList, null);
            }
        } else {
            MistFighter fighter = room.getObjManager().getMistObj(visibleObjId);
            if (fighter == null) {
                return;
            }
            MistPlayer player = fighter.getOwnerPlayerInSameRoom();
            if (player == null || player.isRobot() || !player.isOnline()) {
                return;
            }
            player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE,  battleCmdList);
        }
    }

    protected void updateRealTimeBattleCmd(BattleCmdData.Builder cmdBuilder) {
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        builder.addCMDList(cmdBuilder);
        long visibleObjId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visibleObjId == 0) {
            boolean isOverallObj = getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0;
            if (isOverallObj) {
                getRoom().broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
            } else {
                AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
                if (aoiNode == null) {
                    return;
                }
                aoiNode.broadcastCmd(builder, null);
            }
        } else {
            MistFighter fighter = room.getObjManager().getMistObj(visibleObjId);
            if (fighter == null) {
                return;
            }
            MistPlayer player = fighter.getOwnerPlayerInSameRoom();
            if (player == null || player.isRobot() || !player.isOnline()) {
                return;
            }
            player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE,  builder);
        }
    }

    public void addAttributeChangeCmd(int attrType, long value) {
        if (MistConst.isOnlyServerUseProp(attrType)) {
            return;
        }
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_PropertyChange);
        BattleCMD_PropertyChange.Builder cmdBuilder = BattleCMD_PropertyChange.newBuilder();
        cmdBuilder.setTargetUnitID(id);
        cmdBuilder.setPropertyTypeValue(attrType);
        cmdBuilder.setNewValue(value);
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void addPrivatePropCmd(MistFighter fighter, int propType, long propValues) {
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null || !player.isOnline()) {
            return;
        }
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder batCmdBuilder = BattleCmdData.newBuilder();
        BattleCMD_PropertyChange.Builder cmdBuilder = BattleCMD_PropertyChange.newBuilder();
        cmdBuilder.setTargetUnitID(getId());
        cmdBuilder.setPropertyTypeValue(propType);
        cmdBuilder.setNewValue(propValues);
        batCmdBuilder.setCMDContent(cmdBuilder.build().toByteString());
        builder.addCMDList(batCmdBuilder);
        player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE,  builder);
    }

    public void addCreateObjCmd() {
        BattleCmdData.Builder cmdBuilder = buildCreateObjCmd();
        battleCmdList.addCMDList(cmdBuilder);
    }

    public BattleCmdData.Builder buildCreateObjCmd() {
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
        cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_AddObj);
        BattleCMD_AddObj.Builder addObjCmdBuilder = BattleCMD_AddObj.newBuilder();
        addObjCmdBuilder.addObjsMetaData(getMetaData(null));
//        List<UnitMetadata> slaveMetaData = getSlaveMetaData();
//        if (!CollectionUtils.isEmpty(slaveMetaData)) {
//            addObjCmdBuilder.addAllObjsMetaData(slaveMetaData);
//        }
        cmdBuilder.setCMDContent(addObjCmdBuilder.build().toByteString());
        return cmdBuilder;
    }

    public BattleCmdData.Builder buildRemoveObjCmd() {
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
        cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_RemoveObj);
        BattleCMD_RemoveObj.Builder removeObjCmdBuilder = BattleCMD_RemoveObj.newBuilder();
        removeObjCmdBuilder.addObjIds(getId());
        cmdBuilder.setCMDContent(removeObjCmdBuilder.build().toByteString());
        return cmdBuilder;
    }

    public void addChangePosInfoCmd(ProtoVector pos, ProtoVector toward) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_ChangePos);
        BattleCMD_ChangePos.Builder cmdBuilder = BattleCMD_ChangePos.newBuilder();
        cmdBuilder.setTargetId(id);
        cmdBuilder.getPosBuilder().setX(pos.getX() * 1000).setY(getPos().getY() * 1000);
        cmdBuilder.setTowards(toward);
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void addAddBuffCmd(Buff buff, long curTime) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_AddBuff);
        BattleCMD_AddBuff.Builder cmdBuilder = BattleCMD_AddBuff.newBuilder();
        cmdBuilder.setBuffData(buff.buildBuffData(curTime));
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void addRemoveBuffCmd(int buffId) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_RemoveBuff);
        BattleCMD_RemoveBuff.Builder cmdBuilder = BattleCMD_RemoveBuff.newBuilder();
        cmdBuilder.setTargetId(id);
        cmdBuilder.setBuffId(buffId);
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void addUpdateBuffCmd(Buff buff, long curTime) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_UpdateBuff);
        BattleCMD_UpdateBuff.Builder cmdBuilder = BattleCMD_UpdateBuff.newBuilder();
        cmdBuilder.setBuffData(buff.buildBuffData(curTime));
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void addBlinkCmd(int distance) {
        if (!MistConst.objCanMove(getType())) {
            return;
        }
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_Blink);
        BattleCMD_Blink.Builder cmdBuilder = BattleCMD_Blink.newBuilder();
        cmdBuilder.setTargetId(id);
        cmdBuilder.setBlinkDistance(distance);
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void addBroadcastTipsCmd(BattleCMD_BroacastTips tipsCmd) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_BroadcastTips);
        builder.setCMDContent(tipsCmd.toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void addFlickAwayCmd(int initSpeed, int flickTime, int toward) {
        if (!MistConst.objCanMove(getType())) {
            return;
        }
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_FlickAway);
        BattleCMD_FlickAway.Builder cmdBuilder = BattleCMD_FlickAway.newBuilder();
        cmdBuilder.setTargetId(getId());
        cmdBuilder.setInitSpeed(initSpeed);
        cmdBuilder.setFilckTime(flickTime);
        ProtoVector.Builder flickToward = ProtoVector.newBuilder();
        if (toward > 0) {
            flickToward.setX(-getToward().getX());
            flickToward.setY(-getToward().getY());
        } else {
            flickToward.setX(getToward().getX());
            flickToward.setY(getToward().getY());
        }
        cmdBuilder.setFlickTowards(flickToward);
        builder.setCMDContent(cmdBuilder.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    public void addTriggerEffectCmd(int effectId) {
        BattleCmdData.Builder builder = BattleCmdData.newBuilder();
        builder.setCMDType(MistBattleCmdEnum.MBC_TriggerEffect);
        BattleCMD_TriggerEffect.Builder effectCmd = BattleCMD_TriggerEffect.newBuilder();
        effectCmd.addEffectHosts(getId());
        effectCmd.addTriggerId(effectId);
        builder.setCMDContent(effectCmd.build().toByteString());
        battleCmdList.addCMDList(builder);
    }

    protected void generateDropObj() {
        int configId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistDropObjConfigObject cfg = MistDropObjConfig.getById(configId);
        if (cfg == null) {
            return;
        }
        MistObject obj = getRoom().getObjManager().createObj(cfg.getDropobjtype());
        obj.addAttributes(cfg.getDropobjprop());
        obj.initByMaster(this);
        obj.afterInit(new int[]{getPos().getX(), getPos().getY()}, null);
        if (obj.getAttribute(MistUnitPropTypeEnum.MUPT_IsOverallObj_VALUE) > 0){
            SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
            room.getObjGenerator().addOverallObjId(obj.getId());
            builder.addCMDList(obj.buildCreateObjCmd());
            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, builder, true);
        } else {
            getRoom().getWorldMap().objFirstEnter(obj);
        }
    }

    protected void generatePrivateDropObj(MistFighter fighter) {
        int configId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistDropObjConfigObject cfg = MistDropObjConfig.getById(configId);
        if (cfg == null) {
            return;
        }
        MistObject obj = getRoom().getObjManager().createObj(cfg.getDropobjtype());
        obj.addAttributes(cfg.getDropobjprop());
        obj.setAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, fighter.getId());
        obj.afterInit(new int[]{getPos().getX(), getPos().getY()}, null);

        obj.addCreateObjCmd();
        fighter.addSelfVisibleTarget(obj.getId());
    }

    public void onTick(long curTime) {
        bufMachine.onTick(curTime);
        updateBattleCmd();
        battleCmdList.clear();
        long deadTimeStamp = getDeadTimeStamp();
        if (isAlive()) {
            if (deadTimeStamp > 0 && curTime >= deadTimeStamp) {
                dead();
            }
        } else {
            if (getRebornTime() <= 0) {
                if (deadTimeStamp == 0 || curTime - deadTimeStamp >= TimeUtil.MS_IN_A_S * MistConst.MistDelayRemoveTime - 100) {
                    LogUtil.debug("remove obj id = " + getId());
                    removeObjFromMap();
                    room.getObjManager().removeObj(getId());
                    if (getScheduleObjCfgId() > 0 && room.getScheduleManager() != null) {
                        room.getScheduleManager().removeAliveObj(getId(), getScheduleObjCfgId());
                    }
                    clear();
                }
            } else if (curTime - getDeadTimeStamp() >= getRebornTime() * TimeUtil.MS_IN_A_S) {
                reborn();
                removeMsgFlag = false;
            } else if (!removeMsgFlag && getAttribute(MistUnitPropTypeEnum.MUPT_NotRemoveWhenDead_VALUE) == 0 && curTime - deadTimeStamp >= TimeUtil.MS_IN_A_S * MistConst.MistDelayRemoveTime - 100) {
                removeObjFromMap();
                removeMsgFlag = true;
            }
        }
    }
}
