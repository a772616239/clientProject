package model.mistforest.map.Aoi;

import common.GlobalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.mistforest.MistConst;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.mistobj.rewardobj.MistTreasureBag;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_AddObj;
import protocol.MistForest.BattleCMD_RemoveObj;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.UnitMetadata;

public class AoiNode {
    private int key;
    private Map<Integer, AoiNode> aroundAoiNodeMap;
    private Map<Long, MistObject> mistObjects;

    private Map<Integer, Set<MistPlayer>> ipPlayerMap;

    public AoiNode(int key) {
        this.key = key;
        ipPlayerMap = new HashMap<>();
    }

    public void clear() {
        if (aroundAoiNodeMap != null) {
            aroundAoiNodeMap.clear();
        }
        if (mistObjects != null) {
            mistObjects.clear();
        }
        ipPlayerMap.clear();
    }

    public int getKey() {
        return key;
    }

    public void addAroundAoiNode(AoiNode aoiNode) {
        if (aoiNode == null) {
            return;
        }
        if (aroundAoiNodeMap == null) {
            aroundAoiNodeMap = new HashMap<>();
        }
        aroundAoiNodeMap.put(aoiNode.getKey(), aoiNode);
    }

    public boolean isAroundAoiNode(int key) {
        return aroundAoiNodeMap != null && aroundAoiNodeMap.containsKey(key);
    }

    public boolean containObj(long objId) {
        return mistObjects != null && mistObjects.containsKey(objId);
    }

    public void onObjEnter(MistObject newObj, AoiNode oldAoiNode) {
        if (newObj == null) {
            return;
        }
        MistPlayer player = null;
        List<UnitMetadata> metadataList = null;
        MistFighter newFighter = null;
        if (newObj instanceof MistFighter) {
            newFighter = (MistFighter) newObj;
            player = newFighter.getOwnerPlayerInSameRoom();
            if (player != null) {
                metadataList = new ArrayList<>();
            }
        }

        Map<Integer ,Set<String>> playerMap = new HashMap<>();
        if (oldAoiNode == null || !oldAoiNode.isAroundAoiNode(getKey())) {
            getAoiPlayers(playerMap, newFighter);
            if (player != null) {
                getAllObjMetaData(metadataList, newFighter);
            }
        }

        if (aroundAoiNodeMap != null) {
            for (AoiNode aroundAoiNode : aroundAoiNodeMap.values()) {
                if (oldAoiNode != null && (oldAoiNode.getKey() == aroundAoiNode.getKey() || oldAoiNode.isAroundAoiNode(aroundAoiNode.getKey()))) {
                    continue;
                }
                aroundAoiNode.getAoiPlayers(playerMap, newFighter);
                if (player != null) {
                    aroundAoiNode.getAllObjMetaData(metadataList, newFighter);
                }
            }
        }

        // 发送附近对象给当前玩家
        if (metadataList != null && !metadataList.isEmpty()) {
            SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
            BattleCMD_AddObj.Builder addObjCmdBuilder = BattleCMD_AddObj.newBuilder();
            addObjCmdBuilder.addAllObjsMetaData(metadataList);
            BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
            cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_AddObj);
            cmdBuilder.setCMDContent(addObjCmdBuilder.build().toByteString());
            builder.addCMDList(cmdBuilder);
            player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, builder);
        }

        // 发送当前玩家给附近其他玩家
        if (!playerMap.isEmpty()) {
            SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
            builder.addCMDList(newObj.buildCreateObjCmd());
            for (Entry<Integer, Set<String>> entry : playerMap.entrySet()) {
                GlobalData.getInstance().sendMistMsgToServer(entry.getKey(), MsgIdEnum.SC_BattleCmd_VALUE, entry.getValue(), builder);
            }
        }

        if (mistObjects == null) {
            mistObjects = new ConcurrentHashMap<>();
        }
        mistObjects.put(newObj.getId(), newObj);
        newObj.setAoiNodeKey(getKey());

        if (player != null) {
            Set<MistPlayer> playerSet = ipPlayerMap.get(player.getServerIndex());
            if (playerSet == null) {
                playerSet = new HashSet<>();
            }
            playerSet.add(player);
            ipPlayerMap.put(player.getServerIndex(), playerSet);
        }
    }

    public void onObjLeave(MistObject leaveObj, AoiNode newAoiNode) {
        if (leaveObj == null) {
            return;
        }
        if (mistObjects != null && mistObjects.containsKey(leaveObj.getId())) {
            mistObjects.remove(leaveObj.getId());
        }
        leaveObj.setAoiNodeKey(0);
        MistPlayer player = null;
        List<Long> removeObjList = null;
        MistFighter leaveFighter = null;
        if (leaveObj instanceof MistFighter) {
            // 处理玩家信息
            leaveFighter = (MistFighter) leaveObj;
            player = leaveFighter.getOwnerPlayerInSameRoom();
            if (player != null) {
                removeObjList = new ArrayList<>();
                Set<MistPlayer> playerSet = ipPlayerMap.get(player.getServerIndex());
                if (playerSet != null && playerSet.contains(player)) {
                    playerSet.remove(player);
                }
            }
        }

        Map<Integer ,Set<String>> playerMap = new HashMap<>();
        if (newAoiNode == null || !newAoiNode.isAroundAoiNode(getKey())) {
            getAoiPlayers(playerMap, leaveFighter);
            if (player != null) {
                getAllObjId(removeObjList, leaveFighter);
            }
        }
        if (aroundAoiNodeMap != null) {
            for (AoiNode aroundAoiNode : aroundAoiNodeMap.values()) {
                if (newAoiNode != null && (newAoiNode.getKey() == aroundAoiNode.getKey() || newAoiNode.isAroundAoiNode(aroundAoiNode.getKey()))) {
                    continue;
                }
                aroundAoiNode.getAoiPlayers(playerMap, leaveFighter);
                if (player != null) {
                    aroundAoiNode.getAllObjId(removeObjList, leaveFighter);
                }
            }
        }

        if (removeObjList != null && !removeObjList.isEmpty()) {
            SC_BattleCmd.Builder oldObjBuilder = SC_BattleCmd.newBuilder();
            BattleCMD_RemoveObj.Builder oldObjBuilder1 = BattleCMD_RemoveObj.newBuilder();
            oldObjBuilder1.addAllObjIds(removeObjList);
            BattleCmdData.Builder oldObjBuilder2 = BattleCmdData.newBuilder();
            oldObjBuilder2.setCMDType(MistBattleCmdEnum.MBC_RemoveObj);
            oldObjBuilder2.setCMDContent(oldObjBuilder1.build().toByteString());
            oldObjBuilder.addCMDList(oldObjBuilder2);
            player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, oldObjBuilder);
        }

        if (!playerMap.isEmpty()) {
            SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
            builder.addCMDList(leaveObj.buildRemoveObjCmd());
            for (Entry<Integer, Set<String>> entry : playerMap.entrySet()) {
                GlobalData.getInstance().sendMistMsgToServer(entry.getKey(), MsgIdEnum.SC_BattleCmd_VALUE, entry.getValue(), builder);
            }
        }
    }

    public void onTreasureBagDead(MistTreasureBag bag) {
        if (bag == null) {
            return;
        }
        if (mistObjects != null && mistObjects.containsKey(bag.getId())) {
            mistObjects.remove(bag.getId());
        }
        bag.setAoiNodeKey(0);
    }

    public void onPlayerRevert(MistFighter fighter) {
        if (fighter == null) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        List<UnitMetadata> metadataList = new ArrayList<>();
        getAllObjMetaData(metadataList, fighter);

        if (aroundAoiNodeMap != null) {
            for (AoiNode aroundAoiNode : aroundAoiNodeMap.values()) {
                aroundAoiNode.getAllObjMetaData(metadataList, fighter);
            }
        }

        // 发送附近对象给当前玩家
        if (metadataList != null && !metadataList.isEmpty()) {
            SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
            BattleCMD_AddObj.Builder addObjCmdBuilder = BattleCMD_AddObj.newBuilder();
            addObjCmdBuilder.addAllObjsMetaData(metadataList);
            BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
            cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_AddObj);
            cmdBuilder.setCMDContent(addObjCmdBuilder.build().toByteString());
            builder.addCMDList(cmdBuilder);
            player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, builder);
        }
    }

    public void getAoiPlayers(final Map<Integer ,Set<String>> tmpAroundPlayer, MistFighter commander) {
        if (tmpAroundPlayer == null) {
            return;
        }
        MistFighter fighter;
        Set<String> playerSet;
        for (Entry<Integer, Set<MistPlayer>> entry : ipPlayerMap.entrySet()) {
            playerSet = tmpAroundPlayer.get(entry.getKey());
            for (MistPlayer player : entry.getValue()) {
                if (!player.isOnline()) {
                    continue;
                }
                if (commander != null) {
                    fighter = commander.getRoom().getObjManager().getMistObj(player.getFighterId());
                    if (fighter == null || fighter.notNeedToBroadCastMsg() || fighter.isTeammate(commander.getId())) {
                        continue;
                    }
                }
                if (playerSet == null) {
                    playerSet = new HashSet<>();
                    tmpAroundPlayer.put(entry.getKey(), playerSet);
                }
                playerSet.add(player.getIdx());
            }
        }
    }

    public void getAllAroundPlayers(final Map<Integer ,Set<String>> tmpAroundPlayer, MistFighter commander) {
        if (tmpAroundPlayer == null) {
            return;
        }
        getAoiPlayers(tmpAroundPlayer, commander);
        if (aroundAoiNodeMap != null) {
            for (AoiNode aoiNode : aroundAoiNodeMap.values()) {
                aoiNode.getAoiPlayers(tmpAroundPlayer, commander);
            }
        }
    }

    public void getAoiPlayersExcludeSelf(final Map<Integer ,Set<String>> tmpAroundPlayer, MistFighter commander) {
        if (tmpAroundPlayer == null) {
            return;
        }
        MistFighter fighter;
        Set<String> playerSet;
        for (Entry<Integer, Set<MistPlayer>> entry : ipPlayerMap.entrySet()) {
            playerSet = tmpAroundPlayer.get(entry.getKey());
            for (MistPlayer player : entry.getValue()) {
                if (!player.isOnline()) {
                    continue;
                }
                if (commander != null) {
                    fighter = commander.getRoom().getObjManager().getMistObj(player.getFighterId());
                    if (fighter == null || fighter.getId() == commander.getId()) {
                        continue;
                    }
                    if (fighter.isBattling() || fighter.isTeammate(commander.getId())) {
                        continue;
                    }
                }
                if (playerSet == null) {
                    playerSet = new HashSet<>();
                    tmpAroundPlayer.put(entry.getKey(), playerSet);
                }
                playerSet.add(player.getIdx());
            }
        }
    }

    public void getAllAroundPlayersExcludeSelf(final Map<Integer ,Set<String>> tmpAroundPlayer, MistFighter commander) {
        if (tmpAroundPlayer == null) {
            return;
        }
        getAoiPlayersExcludeSelf(tmpAroundPlayer, commander);
        if (aroundAoiNodeMap != null) {
            for (AoiNode aoiNode : aroundAoiNodeMap.values()) {
                aoiNode.getAoiPlayersExcludeSelf(tmpAroundPlayer, commander);
            }
        }
    }


    public void getAllObjMetaData(List<UnitMetadata> metadataList, MistFighter fighter) {
        if (metadataList == null || mistObjects == null || mistObjects.isEmpty()) {
            return;
        }
//        List<UnitMetadata> objList;
        for (MistObject obj : mistObjects.values()) {
            if (fighter != null && fighter.isTeammate(obj.getId())) { // fighter不为空表示排除队友
                continue;
            }
            metadataList.add(obj.getMetaData(fighter));
//            objList = obj.getSlaveMetaData();
//            if (!CollectionUtils.isEmpty(objList)) {
//                metadataList.addAll(objList);
//            }
        }
    }

    public void getAllObjId(List<Long> idList, MistFighter fighter) {
        if (idList == null || mistObjects == null || mistObjects.isEmpty()) {
            return;
        }
//        List<Long> slaveList;
        for (MistObject obj : mistObjects.values()) {
            if (fighter != null && fighter.isTeammate(obj.getId())) { // fighter不为空表示排除队友
                continue;
            }
            idList.add(obj.getId());
//            slaveList = obj.getSlaveObjList();
//            if (!CollectionUtils.isEmpty(slaveList)) {
//                idList.addAll(slaveList);
//            }
        }
    }

    public <T extends MistObject> void getAllObjByType(List<T> objList, int type) {
        if (objList == null || mistObjects == null || mistObjects.isEmpty()) {
            return;
        }
        for (MistObject obj : mistObjects.values()) {
            if (obj.getType() != type) {
                continue;
            }
            objList.add((T) obj);
        }
    }

    public <T extends MistObject> void getAroundObjByType(List<T> objList, int type) {
        getAllObjByType(objList, type);
        if (aroundAoiNodeMap != null) {
            for (AoiNode aoiNode : aroundAoiNodeMap.values()) {
                aoiNode.getAllObjByType(objList, type);
            }
        }
    }

    public void getNearObjByType(List<MistObject> objList, MistObject source, int objType, int distance, boolean enemy) {
        if (source == null || objList == null || mistObjects == null || mistObjects.isEmpty()) {
            return;
        }
        long camp = source.getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE);
        for (MistObject obj : mistObjects.values()) {
            if (!obj.isAlive() || obj.getId() == source.getId()) {
                continue;
            }
            if (objType >= 0 && obj.getType() != objType) {
                continue;
            }
            if ((enemy && camp == obj.getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE))
                    || (!enemy && camp != obj.getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE))) {
                continue;
            }
            if (MistConst.checkInDistance(distance, source.getPos().build(), obj.getPos().build())) {
                objList.add(obj);
            }
        }
    }

    public void getAllAroundNearObjByType(List<MistObject> objList, MistObject source, int objType, int distance, boolean enemy) {
        getNearObjByType(objList, source, objType, distance, enemy);
        if (aroundAoiNodeMap != null) {
            for (AoiNode aoiNode : aroundAoiNodeMap.values()) {
                aoiNode.getNearObjByType(objList, source, objType, distance, enemy);
            }
        }
    }

    public ProtoVector.Builder getAvailablePos(ProtoVector.Builder pos) {
        ProtoVector.Builder newPos = ProtoVector.newBuilder();
        newPos.setX(pos.getX());
        newPos.setY(pos.getY());
        for (int i = -1; i <= 1; i++) { // 遍历两圈
            for (int j = -1; j <= 1; j++) {

            }
        }
        // TODO 查找周围空闲点
        return newPos;
    }

    public void broadcastCmd(SC_BattleCmd.Builder builder, MistFighter fighter) {
        Map<Integer ,Set<String>> playerMap = new HashMap<>();
        getAllAroundPlayers(playerMap, fighter);
        if (!playerMap.isEmpty()) {
            for (Entry<Integer, Set<String>> entry : playerMap.entrySet()) {
                GlobalData.getInstance().sendMistMsgToServer(entry.getKey(), MsgIdEnum.SC_BattleCmd_VALUE, entry.getValue(), builder);
            }
        }
    }
}
