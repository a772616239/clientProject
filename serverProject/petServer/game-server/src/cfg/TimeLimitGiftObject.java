package cfg;

import model.base.baseConfigObject;

public class TimeLimitGiftObject implements baseConfigObject {


    private int id;

    private int type;

    private int target;

    private boolean musthave;

    private int probability;

    private int exprobability;

    private int triggerlimit;

    private int[] price;

    private int reward;

    private int expiretime;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setType(int type) {

        this.type = type;

    }

    public int getType() {

        return this.type;

    }


    public void setTarget(int target) {

        this.target = target;

    }

    public int getTarget() {

        return this.target;

    }


    public void setMusthave(boolean musthave) {

        this.musthave = musthave;

    }

    public boolean getMusthave() {

        return this.musthave;

    }


    public void setProbability(int probability) {

        this.probability = probability;

    }

    public int getProbability() {

        return this.probability;

    }


    public void setExprobability(int exprobability) {

        this.exprobability = exprobability;

    }

    public int getExprobability() {

        return this.exprobability;

    }


    public void setTriggerlimit(int triggerlimit) {

        this.triggerlimit = triggerlimit;

    }

    public int getTriggerlimit() {

        return this.triggerlimit;

    }


    public void setPrice(int[] price) {

        this.price = price;

    }

    public int[] getPrice() {

        return this.price;

    }


    public void setReward(int reward) {

        this.reward = reward;

    }

    public int getReward() {

        return this.reward;

    }


    public void setExpiretime(int expiretime) {

        this.expiretime = expiretime;

    }

    public int getExpiretime() {

        return this.expiretime;

    }


}
