package model.mistforest.mistobj;

import model.mistforest.MistConst;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import timetool.TimeHelper;
import util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import util.TimeUtil;

public class MistCage extends MistObject {
    List<MistObject> newTargets; // 画地为牢每帧新增目标列表;
    HashSet<Long> oldTargets; // 画地为牢每帧新增目标列表;

    public MistCage(MistRoom room, int objType) {
        super(room, objType);
        newTargets = new ArrayList<>();
        oldTargets = new HashSet<>();
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
//        long lifeTime = getAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE) * TimeUtil.MS_IN_A_S;
//        HashMap params = new HashMap<>();
//        params.put(MistTriggerParamType.BuffTime, lifeTime);
//        bufMachine.addBuff(MistConst.MistCageBuffId,this, params);
    }

    @Override
    public void clear() {
        super.clear();
        newTargets.clear();
        oldTargets.clear();
    }

    @Override
    public void onTick(long curTime) {
        updateBattleCmd();
        battleCmdList.clear();
        if (isAlive()) {
            long lifeTime =  TimeHelper.SEC * getAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE);
            if (lifeTime > 0) {
                long remainLifeTime = lifeTime - (curTime - createTimeStamp);
                if (remainLifeTime <= 0) {
                    dead();
                } else {
                    newTargets.clear();
                    int distance = (int) getAttribute(MistUnitPropTypeEnum.MUPT_CageRadius_VALUE) / 1000;
                    AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
                    if (aoiNode != null) {
                        aoiNode.getAllAroundNearObjByType(newTargets, this, MistUnitTypeEnum.MUT_Player_VALUE, distance, true);
                    }
                    long creatorId = getAttribute(MistUnitPropTypeEnum.MUPT_CreatorId_VALUE);
                    for (MistObject target : newTargets) {
                        if (target.getId() == creatorId) {
                            continue;
                        }
                        if (oldTargets.contains(target.getId())) {
                            continue;
                        }
                        oldTargets.add(target.getId());
                        HashMap<Integer, Long> params = new HashMap();
                        params.put(MistTriggerParamType.BuffTime, remainLifeTime);
                        target.getBufMachine().addBuff(MistConst.MistCageBuffId,this, params);
                    }
                }
            }
        } else {
            if (rebornTime <= 0) {
                long deadTimeStamp = getDeadTimeStamp();
                if (deadTimeStamp == 0 || curTime - deadTimeStamp >= TimeUtil.MS_IN_A_S * MistConst.MistDelayRemoveTime - 100) {
                    LogUtil.debug("remove obj id = " + getId());
                    removeObjFromMap();
                    room.getObjManager().removeObj(getId());
                    clear();
                }
            }
        }
    }
}
