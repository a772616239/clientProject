package petrobot.system.mistForest;

import com.google.protobuf.InvalidProtocolBufferException;
import datatool.StringHelper;
import lombok.Getter;
import lombok.Setter;
import petrobot.robot.Robot;
import petrobot.system.mistForest.map.WorldMap;
import petrobot.system.mistForest.obj.MistObj;
import petrobot.system.mistForest.obj.ObjManager;
import petrobot.system.mistForest.obj.dynamicobj.MistDynamicObj;
import petrobot.system.mistForest.obj.dynamicobj.MistFighter;
import petrobot.tick.GlobalTick;
import petrobot.util.LogUtil;
import protocol.MistForest.BattleCMD_AddObj;
import protocol.MistForest.BattleCMD_ChangePos;
import protocol.MistForest.BattleCMD_PropertyChange;
import protocol.MistForest.BattleCMD_RemoveObj;
import protocol.MistForest.BattleCMD_SnapShotList;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistItemInfo;
import protocol.MistForest.MistPlayerInfo;
import protocol.MistForest.MistTeamInfo;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.UnitMetadata;
import protocol.MistForest.UnitSnapShot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RobotMistForest {
    protected Robot owner;
    protected String roomId;
    protected WorldMap worldMap;

    protected ObjManager objManager;

    protected Map<String, MistPlayerInfo> playerMap;

    protected MistFighter fighter; // robot控制的单位
    protected List<MistItemInfo> itemInfo;
    protected MistTeamInfo teamInfo;

    protected long nextTickTime;

    public RobotMistForest(Robot robot) {
        this.owner = robot;
    }

    public void clear() {
        if (worldMap != null) {
            worldMap.clear();
        }

        if (objManager != null) {
            objManager.clear();
        }
        if (playerMap != null) {
            playerMap.clear();
        }
        if (itemInfo != null) {
            itemInfo.clear();
        }

        roomId = "";
        fighter = null;
        teamInfo = null;
    }

    public void initMistMap(int mapId) {
        if (worldMap == null) {
            worldMap = new WorldMap();
        }
        worldMap.init(mapId);
    }

    public void addPlayerInfo(List<MistPlayerInfo> playerList) {
        if (playerMap == null) {
            playerMap = new HashMap<>();
        }
        for (MistPlayerInfo playerInfo : playerList) {
            playerMap.put(playerInfo.getId(), playerInfo);
        }
    }

    public void addItemInfo(List<MistItemInfo> itemInfoList) {
        if (itemInfo == null) {
            itemInfo = new ArrayList<>();
        }
        for (MistItemInfo mistItem : itemInfoList) {
            itemInfo.add(mistItem);
        }
    }

    public void addObjList(List<UnitMetadata> metadataList) {
        if (objManager == null) {
            objManager = new ObjManager(this);
        }
        for (UnitMetadata metadata : metadataList) {
            addNewObj(metadata);
        }
    }


    public void addNewObj(UnitMetadata metadata) {
        if (metadata == null) {
            return;
        }
        MistObj newObj = objManager.getObjById(MistConst.parsePropertyLongValue(metadata.getProperties(), MistUnitPropTypeEnum.MUPT_UnitID));
        if (newObj == null) {
            newObj = objManager.addNewObj(metadata);
        } else {
            newObj.init(metadata);
        }
        if (newObj != null) {
            newObj.setRobotMistForest(this);
            worldMap.addPosObj(newObj);
        }
    }

    public void removeObj(long id) {
        MistObj removeObj = objManager.getObjById(id);
        if (removeObj == null) {
            return;
        }
        worldMap.removePosObj(removeObj);
        objManager.removeObj(id);
    }

    public void HandleMistCmd(SC_BattleCmd cmdList) {
        for (BattleCmdData cmd : cmdList.getCMDListList()) {
            try {
                switch (cmd.getCMDType()) {
                    case MBC_PropertyChange:
                        BattleCMD_PropertyChange propChangeCmd = BattleCMD_PropertyChange.parseFrom(cmd.getCMDContent());
                        MistObj mistObj = objManager.getObjById(propChangeCmd.getTargetUnitID());
                        if (mistObj != null) {
                            mistObj.setAttribute(propChangeCmd.getPropertyType(), propChangeCmd.getNewValue());
                        }
                        if (propChangeCmd.getPropertyType() == MistUnitPropTypeEnum.MUPT_BattlingTargetId) {
                            LogUtil.info("Set battleTargetId="+ propChangeCmd.getNewValue());
                        }
                        break;
                    case MBC_SnapShotList:
                        BattleCMD_SnapShotList snapShotList = BattleCMD_SnapShotList.parseFrom(cmd.getCMDContent());
                        MistDynamicObj dynamicObj;
                        for (UnitSnapShot snapShot : snapShotList.getSnapShotListList()) {
                            dynamicObj = objManager.getDynamicObj(snapShot.getUnitId());
                            if (dynamicObj != null) {
                                dynamicObj.moveToVecPos(snapShot);
                            }
                        }
                        break;
                    case MBC_AddObj:
                        BattleCMD_AddObj addObjCmd = BattleCMD_AddObj.parseFrom(cmd.getCMDContent());
                        for (UnitMetadata metadata : addObjCmd.getObjsMetaDataList()) {
                            addNewObj(metadata);
                        }
                        break;
                    case MBC_RemoveObj:
                        BattleCMD_RemoveObj removeObjCmd = BattleCMD_RemoveObj.parseFrom(cmd.getCMDContent());
                        for (Long id : removeObjCmd.getObjIdsList()) {
                            removeObj(id);
                        }
                        break;
                    case MBC_ChangePos:
                        BattleCMD_ChangePos changePosObj = BattleCMD_ChangePos.parseFrom(cmd.getCMDContent());
                        MistDynamicObj dyObj = objManager.getObjById(changePosObj.getTargetId());
                        if (dyObj != null) {
                            dyObj.moveToVecPos(changePosObj.getPos(), changePosObj.getTowards(), false, true);
                            if (dyObj instanceof MistFighter && dyObj.getId() == fighter.getId()) {
                                ((MistFighter) dyObj).setNextPos(null);
                            }
                        }
                        break;
                }
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }
    }

    public void onTick() {
        if (StringHelper.isNull(roomId) || owner.getData().getRobotMistForest() == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (curTime > nextTickTime) {
            nextTickTime = curTime;
            for (MistObj obj : objManager.getTotalObjMap().values()) {
//                if (obj.getRobotMistForest()==null) {
//                    obj.setRobotMistForest(this);
//                }
                obj.onTick();
            }
        }
    }
}
