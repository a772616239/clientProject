package model.mistforest.skill;

import cfg.MistSkillConfigObject;
import model.mistforest.mistobj.MistObject;
import model.mistforest.trigger.Trigger;

import java.util.HashMap;
import java.util.List;

public class Skill {
    private int skillId;
    private MistObject owner;
    private List<Trigger> skillEffects;

    public Skill() {
        clear();
    }

    public Skill(int skillId, MistObject owner, MistSkillConfigObject skillCfg) {
        this.skillId = skillId;
        this.owner = owner;
        this.skillEffects = skillCfg.getTriggerList();
    }

    public void clear() {
        this.skillId = 0;
        this.owner = null;
        this.skillEffects = null;
    }

    public void replaceSkill(int skillId, MistObject owner, MistSkillConfigObject skillCfg) {
        this.skillId = skillId;
        this.owner = owner;
        this.skillEffects = skillCfg.getTriggerList();
    }

    public MistObject getOwner() {
        return owner;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public void onTick() {

    }

    public void triggerSkill(MistObject target, HashMap<Integer, Long> params) {
        if (skillEffects != null) {
            for (Trigger trigger : skillEffects) {
                trigger.fire(owner, target, params);
            }
        }
    }

}
