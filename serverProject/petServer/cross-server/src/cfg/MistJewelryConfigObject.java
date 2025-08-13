package cfg;
import model.base.baseConfigObject;
public class  MistJewelryConfigObject implements baseConfigObject{



private int id;

private int needstamina;

private int duration;

private int[][] reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setNeedstamina(int needstamina) {

this.needstamina = needstamina;

}

public int getNeedstamina() {

return this.needstamina;

}


public void setDuration(int duration) {

this.duration = duration;

}

public int getDuration() {

return this.duration;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}




}
