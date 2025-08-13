package model.mistforest.mistobj.rewardobj;

import cfg.MistBox;
import cfg.MistBoxObject;
import model.mistforest.MistConst;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistSubBoxType;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.MistTaskTargetType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;

public class MistTreasureBox extends MistBaseBox {
    public MistTreasureBox(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        if (getAttribute(MistUnitPropTypeEnum.MUPT_SubBoxType_VALUE) == EnumMistSubBoxType.EMSBT_TreasureBox_VALUE) {
            room.getObjManager().addBoxQualityCount(getBoxQuality());
        }
    }

    @Override
    public void reborn() {
        if (getAttribute(MistUnitPropTypeEnum.MUPT_SubBoxType_VALUE) == EnumMistSubBoxType.EMSBT_TreasureBox_VALUE) {
            room.getObjManager().addBoxQualityCount(getBoxQuality());
        }
        super.reborn();
    }

    @Override
    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);

        if (getAttribute(MistUnitPropTypeEnum.MUPT_SubBoxType_VALUE) == EnumMistSubBoxType.EMSBT_TreasureBox_VALUE) {
            room.getObjManager().minusBoxQualityCount(getBoxQuality());
        }
        super.dead();
    }

    @Override
    public void gainReward(MistFighter fighter) {
        MistPlayer player = fighter.getOwnerPlayer();
        if (player == null) {
            return;
        }
        super.gainReward(fighter);
        int rewardId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_BoxRewardId_VALUE);
        int extCount =  MistConst.calcCrossVipDropExtBox(player.getCrossVipLv(), getBoxQuality());
        if (extCount > 0) {
            fighter.gainRewardBox(rewardId, extCount);
            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                    room.buildMistTips(EnumMistTipsType.EMTT_LuckyBoxReward_VALUE, fighter, this, player.getCrossVipLv(), rewardId, extCount), false);
        }

        fighter.getNpcTask().doNpcTask(MistTaskTargetType.MTTT_GainTreasureBox_VALUE, 1, getBoxQuality());
        fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_PickUpMistBox, getBoxQuality(), 1);
    }

    public int getBoxQuality() {
        MistBoxObject cfg = MistBox.getByRewardid((int) getAttribute(MistUnitPropTypeEnum.MUPT_BoxRewardId_VALUE));
        return cfg != null ? cfg.getCountshowtype() : 0;
    }
}
