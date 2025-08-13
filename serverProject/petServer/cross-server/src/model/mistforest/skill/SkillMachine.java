package model.mistforest.skill;

import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import cfg.MistPlayerSkillCfg;
import cfg.MistPlayerSkillCfgObject;
import cfg.MistSkillConfig;
import cfg.MistSkillConfigObject;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import protocol.MistForest.MistItemInfo;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistVipSkillData;
import util.LogUtil;
import util.TimeUtil;

public class SkillMachine {
    private MistObject owner;
    private long updateSkillTime;

    private HashMap<Integer, ArrayList<Skill>> allPassiveSkills;
    private ArrayList<Skill> itemSkillList;

    private HashMap<Integer, VipSkill> vipSkills;

    public SkillMachine(MistObject owner) {
        this.owner = owner;
        allPassiveSkills = new HashMap<>();
        itemSkillList = new ArrayList<>(MistConst.MistItemSkillMaxCount);
        for (int i = 0; i < MistConst.MistItemSkillMaxCount; ++i) {
            itemSkillList.add(new Skill());
        }
    }

    public void clear() {
        owner = null;
        allPassiveSkills.clear();
        itemSkillList.clear();
        if (vipSkills != null) {
            vipSkills.clear();
        }
    }

    public MistObject getOwner() {
        return owner;
    }

    public void setOwner(MistObject owner) {
        this.owner = owner;
    }

    public void clearPassiveSkill() {
        allPassiveSkills.clear();
    }

    public void addPassiveSkill(int timing, Skill skill) {
        ArrayList<Skill> skillList = allPassiveSkills.get(timing);
        if (skillList == null) {
            skillList = new ArrayList<>();
        }
        skillList.add(skill);
        allPassiveSkills.put(timing, skillList);
    }

    public void triggerPassiveSkills(int timing, MistObject target, HashMap<Integer, Long> params) {
        ArrayList<Skill> skillList = allPassiveSkills.get(timing);
        if (skillList == null) {
            return;
        }
        for (Skill skill : skillList) {
            skill.triggerSkill(target, params);
        }
    }

    public List<MistItemInfo> getAllItemSkillInfo() {
        List<MistItemInfo> skillList = new ArrayList<>();
        MistSkillConfigObject skillCfg;
        for (int i = 0; i < itemSkillList.size(); ++i) {
            Skill skill = itemSkillList.get(i);
            skillCfg = MistSkillConfig.getInstance().getById(skill.getSkillId());

            MistItemInfo.Builder builder = MistItemInfo.newBuilder();
            builder.setIndex(i);
            if (skillCfg != null) {
                builder.setItemCfgId(skillCfg.getSourceitemtype());
            }
            skillList.add(builder.build());
        }
        return skillList;
    }

    public void initItemSkillList(List<MistItemInfo> itemInfos) {
        if (itemInfos == null || itemInfos.isEmpty()) {
            return;
        }
        Skill skill;
        for (MistItemInfo itemInfo : itemInfos) {
            if (itemInfo.getIndex() < 0 || itemInfo.getIndex() >= itemSkillList.size()) {
                continue;
            }
            int itemSkillId = MistSkillConfig.getSkillByItemType(itemInfo.getItemCfgId());
            if (itemSkillId <= 0) {
                continue;
            }
            MistSkillConfigObject skillCfg = MistSkillConfig.getInstance().getById(itemSkillId);
            if (skillCfg == null) {
                LogUtil.error("init player skill error,itemSkillId=" + itemSkillId + ",itemCfgId=" + itemInfo.getItemCfgId());
                continue;
            }
            skill = itemSkillList.get(itemInfo.getIndex());
            skill.replaceSkill(itemSkillId, owner, skillCfg);
        }
    }

    public int addItemSkill(int skillId) {
        int index = -1;
        Skill skill;
        for (int i = 0; i < itemSkillList.size(); ++i) {
            skill = itemSkillList.get(i);
            if (skill.getSkillId() == 0) {
                MistSkillConfigObject skillCfg = MistSkillConfig.getInstance().getById(skillId);
                if (skillCfg != null) {
                    index = i;
                    skill.replaceSkill(skillId, owner, skillCfg);
                    itemSkillList.set(i, skill);
                }
                break;
            }
        }
        return index;
    }

    public boolean removeItemSkill(int index) {
        if (index < 0 || index >= itemSkillList.size()) {
            return false;
        }
        Skill skill = itemSkillList.get(index);
        if (skill == null) {
            return false;
        }
        skill.clear();
        return true;
    }

    public boolean isItemSkillFull() {
        boolean hasEmptyPos = false;
        for (Skill skill : itemSkillList) {
            if (skill.getSkillId() == 0) {
                hasEmptyPos = true;
                break;
            }
        }
        return !hasEmptyPos;
    }

    public boolean canUseItemSkill(int index) {
        if (index < 0 || index >= itemSkillList.size()) {
            return false;
        }
        if (owner.isInSafeRegion()) {
            return false;
        }
//        long curTime = System.currentTimeMillis();
//        if (curTime - lastCastSkillTime < 3000) {
//            return false;
//        }
        if (owner.getAttribute(MistUnitPropTypeEnum.MUPT_IsUnderControl_VALUE) > 0) {
            return false;
        }
        Skill skill = itemSkillList.get(index);
        return skill != null && skill.getSkillId() != 0;
    }

    public boolean castItemSkill(int index, HashMap<Integer, Long> params) {
        long curTime = GlobalTick.getInstance().getCurrentTime();
//        if (curTime - lastCastSkillTime < 3000) {
//            return false;
//        }
        if (index < 0 || index >= itemSkillList.size()) {
            return false;
        }
        Skill skill = itemSkillList.get(index);
        if (skill == null || skill.getSkillId() == 0) {
            return false;
        }
        params.put(MistTriggerParamType.ItemSkillId, Long.valueOf(skill.getSkillId()));
        MistSkillConfigObject skillCfg = MistSkillConfig.getById(skill.getSkillId());
        if (skillCfg != null && skillCfg.getSourceitemtype() == MistConst.MistInvokeScrollItemId) {
            params.put(MistTriggerParamType.TransInvokerId, owner.getId());
        }
        skill.triggerSkill(owner, params);
        removeItemSkill(index);
        return true;
    }

    public int getItemTypeByIndex(int index) {
        try {
            if (index < 0 || index >= itemSkillList.size()) {
                return 0;
            }
            Skill skill = itemSkillList.get(index);
            if (skill == null || skill.getSkillId() == 0) {
                return 0;
            }
            MistSkillConfigObject skillCfg = MistSkillConfig.getById(skill.getSkillId());
            return skillCfg != null ? skillCfg.getSourceitemtype() : 0;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return 0;
        }
    }

    public void initOffVipSkillData(int crossVipLv, List<MistVipSkillData> vipSkillDataList) {
        if (!(owner instanceof MistFighter)) {
            return;
        }
        MistFighter fighter = (MistFighter) owner;
        CrossArenaLvCfgObject crossVipCfg = CrossArenaLvCfg.getByLv(crossVipLv);
        if (crossVipCfg == null) {
            return;
        }
        int[] cfgSkillList = crossVipCfg.getMistvipskilllist();
        if (cfgSkillList == null || cfgSkillList.length < 0) {
            return;
        }
        VipSkill vipSkill;
        MistPlayerSkillCfgObject skillCfg;
        for (int skillId : cfgSkillList) {
            skillCfg = MistPlayerSkillCfg.getById(skillId);
            if (skillCfg == null) {
                continue;
            }
            vipSkill = new VipSkill(fighter, skillCfg);
            for (MistVipSkillData skillData : vipSkillDataList) {
                if (skillData.getSkillId() == skillId) {
                    vipSkill.initByOffSkillData(skillData);
                    break;
                }
            }
            if (vipSkills == null) {
                vipSkills = new HashMap<>();
            }
            vipSkills.put(skillId, vipSkill);
        }
    }

    public MistRetCode useVipSkill(int skillId, String param) {
        if (vipSkills == null || vipSkills.isEmpty()) {
            return MistRetCode.MRC_NotHaveSkill; // 没有此技能
        }
        VipSkill vipSkill = vipSkills.get(skillId);
        if (vipSkill == null) {
            return MistRetCode.MRC_NotHaveSkill; // 没有此技能
        }
        return vipSkill.useSkill(param);
    }

    public List<MistVipSkillData> buildAllVipSkillData() {
        if (vipSkills == null || vipSkills.isEmpty()) {
            return null;
        }
        List<MistVipSkillData> skillList = new ArrayList<>();
        for (VipSkill skill : vipSkills.values()) {
            skillList.add(skill.buildVipSkillData());
        }
        return skillList;
    }

    public VipSkill getVipSkillByType(int skillType) {
        if (vipSkills == null) {
            return null;
        }
        for (VipSkill skill : vipSkills.values()) {
            if (skill.getSkillType() == skillType) {
                return skill;
            }
        }
        return null;
    }

    public void onTick(long curTime) {
        if (vipSkills == null || vipSkills.isEmpty()) {
            return;
        }
        if (updateSkillTime > curTime) {
            return;
        }
        updateSkillTime = curTime + TimeUtil.MS_IN_A_S;
        for (VipSkill vipSkill : vipSkills.values()) {
            vipSkill.onTick(curTime);
        }
    }
}
