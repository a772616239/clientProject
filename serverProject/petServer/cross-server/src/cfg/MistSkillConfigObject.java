package cfg;

import model.base.baseConfigObject;
import model.mistforest.trigger.Trigger;

import java.util.ArrayList;
import java.util.List;

public class MistSkillConfigObject implements baseConfigObject {


    private int id;

    private boolean isinitialskill;

    private int sourceitemtype;

    private int skilltriggertiming;

    private int cooldown;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }

    public boolean getIsinitialskill() {
        return isinitialskill;
    }

    public void setIsinitialskill(boolean isinitialskill) {
        this.isinitialskill = isinitialskill;
    }

    public void setSourceitemtype(int sourceitemtype) {

        this.sourceitemtype = sourceitemtype;

    }

    public int getSourceitemtype() {

        return this.sourceitemtype;

    }

    public void setSkilltriggertiming(int skilltriggertiming) {

        this.skilltriggertiming = skilltriggertiming;

    }

    public int getSkilltriggertiming() {

        return this.skilltriggertiming;

    }


    public void setCooldown(int cooldown) {

        this.cooldown = cooldown;

    }

    public int getCooldown() {

        return this.cooldown;

    }

    /*********************自动生成分隔*****************************/

    private List<Trigger> triggerList = new ArrayList<>();

    public List<Trigger> getTriggerList() {
        return triggerList;
    }

    public void setTriggerList(List<Trigger> triggerList) {
        this.triggerList = triggerList;
    }

}
