package cfg;
import model.base.baseConfigObject;
public class  FarmMineAwardObject implements baseConfigObject{



private int id;

private int type;

private int quality;

private int weight;

private int[] reward;

private float rewardvue;

private int petid;

private int petadd;




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


public void setQuality(int quality) {

this.quality = quality;

}

public int getQuality() {

return this.quality;

}


public void setWeight(int weight) {

this.weight = weight;

}

public int getWeight() {

return this.weight;

}


public void setReward(int[] reward) {

this.reward = reward;

}

public int[] getReward() {

return this.reward;

}


public void setRewardvue(float rewardvue) {

this.rewardvue = rewardvue;

}

public float getRewardvue() {

return this.rewardvue;

}


public void setPetid(int petid) {

this.petid = petid;

}

public int getPetid() {

return this.petid;

}


public void setPetadd(int petadd) {

this.petadd = petadd;

}

public int getPetadd() {

return this.petadd;

}




}
