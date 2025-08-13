package cfg;

import java.util.List;
import model.base.baseConfigObject;
import model.mistforest.trigger.Trigger;

public class MistBuffConfigObject implements baseConfigObject {


    private int id;

    private int lifetime;

    private int cycletime;

    private int maxstackcount;

    private boolean isprogressbuff;

    private boolean isdebuff;

    private int[] interrupttype;

    private int pausedecreasetime;

    private boolean isofflinebuff;

    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setLifetime(int lifetime) {

        this.lifetime = lifetime;

    }

    public int getLifetime() {

        return this.lifetime;

    }


    public void setCycletime(int cycletime) {

        this.cycletime = cycletime;

    }

    public int getCycletime() {

        return this.cycletime;

    }


    public void setMaxstackcount(int maxstackcount) {

        this.maxstackcount = maxstackcount;

    }

    public int getMaxstackcount() {

        return this.maxstackcount;

    }


    public void setIsprogressbuff(boolean isprogressbuff) {

        this.isprogressbuff = isprogressbuff;

    }

    public boolean getIsprogressbuff() {

        return this.isprogressbuff;

    }


    public void setIsdebuff(boolean isdebuff) {

        this.isdebuff = isdebuff;

    }

    public boolean getIsdebuff() {

        return this.isdebuff;

    }


    public void setInterrupttype(int[] interrupttype) {

        this.interrupttype = interrupttype;

    }

    public int[] getInterrupttype() {

        return this.interrupttype;

    }


    public void setPausedecreasetime(int pausedecreasetime) {

        this.pausedecreasetime = pausedecreasetime;

    }

    public int getPausedecreasetime() {

        return this.pausedecreasetime;

    }


    public void setIsofflinebuff(boolean isofflinebuff) {

        this.isofflinebuff = isofflinebuff;

    }

    public boolean getIsofflinebuff() {

        return this.isofflinebuff;

    }

    /*********************自动生成分隔*****************************/

    private List<Trigger> buffAddTriggers;
    private List<Trigger> buffDelTriggers;
    private List<Trigger> buffCycleTriggers;
    private List<Trigger> interruptTriggers;
    private List<Trigger> buffRobbedTriggers;

    public List<Trigger> getBuffAddTriggers() {
        return buffAddTriggers;
    }

    public void setBuffAddTriggers(List<Trigger> buffAddTriggers) {
        this.buffAddTriggers = buffAddTriggers;
    }

    public List<Trigger> getBuffDelTriggers() {
        return buffDelTriggers;
    }

    public void setBuffDelTriggers(List<Trigger> buffDelTriggers) {
        this.buffDelTriggers = buffDelTriggers;
    }

    public List<Trigger> getBuffCycleTriggers() {
        return buffCycleTriggers;
    }

    public void setBuffCycleTriggers(List<Trigger> buffCycleTriggers) {
        this.buffCycleTriggers = buffCycleTriggers;
    }

    public List<Trigger> getInterruptTriggers() {
        return interruptTriggers;
    }

    public void setInterruptTriggers(List<Trigger> interruptTriggers) {
        this.interruptTriggers = interruptTriggers;
    }

    public List<Trigger> getBuffrobbedeffectcmd() {

        return this.buffRobbedTriggers;

    }

    public void setBuffrobbedeffectcmd(List<Trigger> buffRobbedTriggers) {

        this.buffRobbedTriggers = buffRobbedTriggers;

    }
}
