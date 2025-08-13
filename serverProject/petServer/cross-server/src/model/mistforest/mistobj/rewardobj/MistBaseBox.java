package model.mistforest.mistobj.rewardobj;

import cfg.MistComboBornPosConfig;
import cfg.MistComboBornPosConfigObject;
import common.GlobalTick;
import java.util.HashMap;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistGuardMonster;
import model.mistforest.room.entity.MistRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistSubBoxType;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.TimeUtil;

public class MistBaseBox extends MistRewardObj {

    public MistBaseBox(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        initPos(initialPos, initialPos);

        initRebornTime();
        long lifeTime = getAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE);
        if (lifeTime > 0) {
            setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime() + lifeTime * TimeUtil.MS_IN_A_S);
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_SubBoxType_VALUE) == EnumMistSubBoxType.EMSBT_GuardMonster_VALUE) {
            createGuardMonster();
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_ShowInVipMiniMap_VALUE) > 0) {
            getRoom().getObjManager().addNeedShowObj(this);
        }
    }

    @Override
    public void initComboPos() {
        MistComboBornPosConfigObject cfg = MistComboBornPosConfig.getById((int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE));
        if (null != cfg && null != cfg.getMasterobjpos() && cfg.getMasterobjpos().length >= 2) {
            setInitPos(cfg.getMasterobjpos()[0], cfg.getMasterobjpos()[1]);
            setPos(getInitPos().build());
        }
    }

//    @Override
//    public void initRebornTime() {
//        if (getAttribute(MistUnitPropTypeEnum.MUPT_SubBoxType_VALUE) >= EnumMistSubBoxType.EMSBT_MagicGuardBox_VALUE) {
//            int rebornTime = (int) getAttribute(MistUnitPropTypeEnum.MUPT_RebornTime_VALUE);
//            if (rebornTime > 0) {
//                rebornTime = Math.max(MistConst.MistDelayRemoveTime, rebornTime);
//            }
//            setRebornTime(rebornTime);
//        } else {
//            super.initRebornTime();
//        }
//    }

    @Override
    public void reborn() {
        super.reborn();
        if (getAttribute(MistUnitPropTypeEnum.MUPT_SubBoxType_VALUE) == EnumMistSubBoxType.EMSBT_GuardMonster_VALUE) {
            createGuardMonster();
        }
    }

    public RetCodeEnum canTouch(MistFighter fighter, int curStamina) {
        if (!isAlive()) {
            return RetCodeEnum.RCE_Mist_BoxDisappear;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (playerId == 0) {
            return RetCodeEnum.RCE_Mist_PlayerNotFound;
        }
        if (!isQualifiedPlayer(playerId)) {
            return RetCodeEnum.RCE_Mist_NoQualificationToGetReward;
        }

        if (playerId == fighter.getAttribute(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE)) {
            return RetCodeEnum.RCE_Mist_BoxHasBeenOpened;
        }
        int needStamina = (int) getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
        if (curStamina < needStamina) {
            return RetCodeEnum.RCE_Mist_StaminaNotEnough;
        }
        return RetCodeEnum.RCE_Success;
    }

    public void beTouch(MistFighter fighter) {
        HashMap<Integer, Long> params = new HashMap<>();
        params.put(MistTriggerParamType.TreasureBoxId, getId());
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchTreasureBox, this, params);
    }

    public void gainReward(MistFighter fighter) {
        int rewardId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_BoxRewardId_VALUE);
        boolean isBossActivityBox = getAttribute(MistUnitPropTypeEnum.MUPT_IsBossActivityBox_VALUE) > 0;
        if (isBossActivityBox) {
            fighter.gainActivityBossReward(rewardId);
        } else {
            fighter.gainReward(rewardId);
        }
        room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                room.buildMistTips(EnumMistTipsType.EMTT_PickupBox_VALUE, fighter, this, rewardId), false);


    }

    public void createGuardMonster() {
        int complxBornPosId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE);
        MistComboBornPosConfigObject cfg = MistComboBornPosConfig.getById(complxBornPosId);
        if (null == cfg || null == cfg.getSlaveobjposlist()) {
            return;
        }
        int count = cfg.getSlaveobjposlist().length;
        for (int i = 0; i < count; i++) {
            int[] posData = cfg.getSlaveobjposlist()[i];
            if (null == posData || posData.length < 3) {
                continue;
            }
            MistGuardMonster guardMonster = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_GuardMonster_VALUE);
            guardMonster.initByMaster(this);
            guardMonster.setAttribute(MistUnitPropTypeEnum.MUPT_GuardMonsterPathCfgId_VALUE, posData[2]); // 需在afterInit之前设置
            guardMonster.afterInit(posData, null);
            guardMonster.setAttribute(MistUnitPropTypeEnum.MUPT_ComplexBornPosCfgId_VALUE, complxBornPosId); // 需在初始化位置之后设置

            addSlaveObj(guardMonster.getId());
            getRoom().getWorldMap().objFirstEnter(guardMonster);
        }
    }
}
