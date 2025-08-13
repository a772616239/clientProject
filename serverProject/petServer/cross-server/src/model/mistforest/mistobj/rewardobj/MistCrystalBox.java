package model.mistforest.mistobj.rewardobj;

import cfg.MistComboBornPosConfigObject;
import cfg.MistCrystallBoxConfig;
import cfg.MistCrystallBoxConfigObject;
import cfg.MistExplodeConfig;
import cfg.MistExplodeConfigObject;
import common.GlobalData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistTaskTargetType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.UnitMetadata;
import protocol.ServerTransfer.CS_GS_ReqChangeMistStamina;
import protocol.TargetSystem.TargetTypeEnum;

public class MistCrystalBox extends MistBaseBox {
    protected Set<Long> claimRewardPlayers = new HashSet<>();

    public MistCrystalBox(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void clear() {
        super.clear();
        claimRewardPlayers.clear();
    }

    @Override
    public void reborn() {
        super.reborn();
        claimRewardPlayers.clear();
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE, getAttribute(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE));
    }

    @Override
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

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_LifeState_VALUE || propType == MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata metadata = super.getMetaData(fighter);
        UnitMetadata.Builder builder = metadata.toBuilder();
        if (isAlive()) {
            long lifeState = LifeStateEnum.LSE_Survival_VALUE;
            if (fighter != null) {
                long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
                lifeState = claimRewardPlayers.contains(playerId) ? LifeStateEnum.LSE_Dead_VALUE : LifeStateEnum.LSE_Survival_VALUE;
            }
            builder.getPropertiesBuilder().addKeys(MistUnitPropTypeEnum.MUPT_LifeState).addValues(lifeState);

            long canChosenTimes = getAttribute(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE);
            if (canChosenTimes > 0 && lifeState == LifeStateEnum.LSE_Survival_VALUE) {
                canChosenTimes = Math.max(0, canChosenTimes - claimRewardPlayers.size());
                builder.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE).addValues(canChosenTimes);
            }
        } else {
            builder.getPropertiesBuilder().addKeys(MistUnitPropTypeEnum.MUPT_LifeState).addValues(LifeStateEnum.LSE_Dead_VALUE);
        }
        return builder.build();
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        long canChosenTimes = getAttribute(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE);
        if (canChosenTimes > 0 && canChosenTimes <= claimRewardPlayers.size()) {
            return;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (claimRewardPlayers.contains(playerId)) {
            return;
        }
        long visibleId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visibleId > 0 && visibleId != fighter.getId()) {
            return;
        }

        int needStamina = (int) getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
        if (needStamina > 0) {
            MistPlayer owner = fighter.getOwnerPlayerInSameRoom();
            if (owner == null || owner.isRobot()) {
                return;
            }
            if (owner.getMistStamina() < needStamina) {
                return;
            }
            CS_GS_ReqChangeMistStamina.Builder builder = CS_GS_ReqChangeMistStamina.newBuilder();
            builder.setPlayerIdx(owner.getIdx());
            builder.setChangeValue(-needStamina);
            GlobalData.getInstance().sendMsgToServer(owner.getServerIndex(), MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE, builder);
        }

        generateRewardObj(fighter);

        claimRewardPlayers.add(playerId);
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);

        if (canChosenTimes > 0) {
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_SelfChooseBoxChooseTimes_VALUE, canChosenTimes - claimRewardPlayers.size());
        }

        fighter.getNpcTask().doNpcTask(MistTaskTargetType.MTTT_TouchCrystalBox_VALUE, 1, 0);

        int taskParam = (int) getAttribute(MistUnitPropTypeEnum.MUPT_CrystalBoxTaskParam_VALUE);
        fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_TouchCrystalBox, taskParam,1);

        if (visibleId > 0) { // 私有的拾取后直接死亡
            dead();
        }
    }

    public void generateRewardObj(MistFighter fighter) {
        if (fighter == null) {
            return;
        }
        int rewardCfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistCrystallBoxConfigObject rewardCfg = MistCrystallBoxConfig.getById(rewardCfgId);
        if (rewardCfg == null || rewardCfg.getOptionallist() == null) {
            return;
        }
        int rewardObjCount = rewardCfg.getOptionallist().length;
        if (rewardObjCount <= 0) {
            return;
        }
        List<ProtoVector.Builder> posList = new ArrayList<>();
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
            obj.initByMaster(fighter);
            obj.setAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, fighter.getId());
            obj.setPos(newPos.build());
            obj.afterInit(new int[]{newPos.getX(), newPos.getY()}, null);

            obj.addCreateObjCmd();
            fighter.addSelfVisibleTarget(obj.getId());

            if (obj instanceof MistRewardObj) {
                objectList.add((MistRewardObj) obj);
            }
        }

        fighter.sendExplodeDropReward(getPos(), null, objectList);

        int newbieTaskId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_NewbieTaskFlag_VALUE);
        if (newbieTaskId > 0) {
            fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_PickUpMistNewbieBox, newbieTaskId, 1);
        }
    }
}
