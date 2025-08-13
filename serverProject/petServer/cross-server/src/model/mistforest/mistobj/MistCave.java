package model.mistforest.mistobj;

import cfg.MistTransPosConfig;
import cfg.MistTransPosConfigObject;
import java.util.HashMap;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.GameUtil;
import util.LogUtil;

public class MistCave extends MistObject {
    public MistCave(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        if (fighter == null || fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
            return;
        }
        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_TransPosCfgId_VALUE);
        MistTransPosConfigObject cfg = MistTransPosConfig.getById(cfgId);
        if (cfg == null || cfg.getTransposlist() == null || cfg.getTransposlist().length <= 0) {
            return;
        }
        int rand = 0;
        if (cfg.getTransposlist().length > 1) {
            rand = RandomUtils.nextInt(cfg.getTransposlist().length);
        }
        int newPosX = cfg.getTransposlist()[rand][0];
        int newPosY = cfg.getTransposlist()[rand][1];
        if (!getRoom().getWorldMap().isPosValid(newPosX, newPosY)) {
            LogUtil.error("MistCave transCfg error,cfgId=" + cfgId + ",posX=" + newPosX + ",poxY=" + newPosY);
            return;
        }
        long posData = GameUtil.mergeIntToLong(newPosX, newPosY);
        HashMap<Integer, Long> param = new HashMap<>();
        param.put(MistTriggerParamType.TranPosData, posData);
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.TouchCave, this, param);

        fighter.doMistTargetProg(TargetTypeEnum.TTE_Mist_TouchCave, 0, 1);
    }
}
