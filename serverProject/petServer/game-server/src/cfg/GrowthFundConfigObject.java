package cfg;
import model.base.baseConfigObject;
public class  GrowthFundConfigObject implements baseConfigObject{



private int id;

private int targetplayerlv;

private int reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setTargetplayerlv(int targetplayerlv) {

this.targetplayerlv = targetplayerlv;

}

public int getTargetplayerlv() {

return this.targetplayerlv;

}


public void setReward(int reward) {

this.reward = reward;

}

public int getReward() {

return this.reward;

}




}
