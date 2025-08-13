package model.mistforest.mistobj.activityboss;

import model.mistforest.MistConst.MistActivityBossStage;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.RetCodeId.RetCodeEnum;

public class MistGargoyleMonster extends MistObject {
    public MistGargoyleMonster(MistRoom room, int objType) {
        super(room, objType);
    }

    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }

        RetCodeEnum retCodeEnum = RetCodeEnum.RCE_Mist_FakeGargoyleMonster;
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsTrulyStatue_VALUE) > 0) {
            int rewardId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_BoxRewardId_VALUE);
            fighter.gainReward(rewardId);
            MistActivityBoss boss = getRoom().getObjManager().getMistObj(getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
            if (null != boss) {
                boss.changeToStage(MistActivityBossStage.weakStage);
            }
            retCodeEnum = RetCodeEnum.RCE_Mist_TrulyGargoyleMonster;
        } else {
            dead();
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (null != player) {
            player.sendRetCodeMsg(retCodeEnum);
        }
    }
}
