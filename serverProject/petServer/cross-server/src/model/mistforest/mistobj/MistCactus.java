package model.mistforest.mistobj;

import cfg.MistCrystallBoxConfig;
import cfg.MistCrystallBoxConfigObject;
import cfg.MistExplodeConfig;
import cfg.MistExplodeConfigObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.rewardobj.MistRewardObj;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.BattleCMD_ExplodeReward;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistTaskTargetType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.ProtoVector.Builder;

public class MistCactus extends MistObject {
    protected long initCanBeTouchTimes;

    public MistCactus(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        initCanBeTouchTimes = getAttribute(MistUnitPropTypeEnum.MUPT_CactusBeTouchTimes_VALUE);
    }

    @Override
    public void reborn() {
        super.reborn();
        setAttribute(MistUnitPropTypeEnum.MUPT_CactusBeTouchTimes_VALUE, initCanBeTouchTimes);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_CactusBeTouchTimes_VALUE, initCanBeTouchTimes);
    }

    @Override
    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_CactusBeTouchTimes_VALUE, 0);
        super.dead();
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        long needStamina = getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
        if (player.getMistStamina() < needStamina) {
            return;
        }
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.ChangeStaminaVal, needStamina);
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchCactus, this, params);

        long canBeTouchTimes = getAttribute(MistUnitPropTypeEnum.MUPT_CactusBeTouchTimes_VALUE);
        if (--canBeTouchTimes > 0) {
            setAttribute(MistUnitPropTypeEnum.MUPT_CactusBeTouchTimes_VALUE, canBeTouchTimes);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_CactusBeTouchTimes_VALUE, canBeTouchTimes);
        } else {
            generateRewardObj();
            fighter.getNpcTask().doNpcTask(MistTaskTargetType.MTTT_GainCactusReward_VALUE, 1, 0);
            dead();
        }
    }

    public void generateRewardObj() {
        int rewardCfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistCrystallBoxConfigObject rewardCfg = MistCrystallBoxConfig.getById(rewardCfgId);
        if (rewardCfg == null || rewardCfg.getOptionallist() == null) {
            return;
        }
        int rewardObjCount = rewardCfg.getOptionallist().length;
        if (rewardObjCount <= 0) {
            return;
        }
        List<Builder> posList = new ArrayList<>();
        // 搜索附近1格的空闲位置
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (!getRoom().getWorldMap().isPosReachable(pos.getX() + i, pos.getY() + j)) {
                    continue;
                }
                posList.add(ProtoVector.newBuilder().setX(pos.getX() + i).setY(pos.getY() + j));
            }
        }
        if (posList.size() < rewardObjCount) {
            for (int i = posList.size(); i < rewardObjCount; i++) {
                posList.add(ProtoVector.newBuilder().mergeFrom(pos.build()));
            }
        }
        Collections.shuffle(posList);
        MistExplodeConfigObject explodeCfg;
        List<MistRewardObj> objectList = new ArrayList<>();
        for (int i = 0; i < rewardObjCount; i++) {
            explodeCfg = MistExplodeConfig.getById(rewardCfg.getOptionallist()[i]);
            if (explodeCfg == null) {
                continue;
            }
            ProtoVector.Builder newPos = posList.get(i);
            MistObject obj = getRoom().getObjManager().createObj(explodeCfg.getObjtype());
            obj.addAttributes(explodeCfg.getInitprop());

            obj.setPos(newPos.build());
            obj.afterInit(new int[]{newPos.getX(), newPos.getY()}, null);
            getRoom().getWorldMap().objFirstEnter(obj);
            if (obj instanceof MistRewardObj) {
                objectList.add((MistRewardObj) obj);
            }
        }

        if (!objectList.isEmpty()) {
            BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder().setCMDType(MistBattleCmdEnum.MBC_ExplodeReward);
            BattleCMD_ExplodeReward.Builder dropRewardCmd = BattleCMD_ExplodeReward.newBuilder();
            dropRewardCmd.setExplodePos(getPos());
            for (MistRewardObj rewardObj : objectList) {
                dropRewardCmd.addDropObjs(rewardObj.getMetaData(null));
            }
            cmdBuilder.setCMDContent(dropRewardCmd.build().toByteString());
            battleCmdList.addCMDList(cmdBuilder);
        }
    }
}
