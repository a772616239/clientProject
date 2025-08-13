package cfg;
import model.base.baseConfigObject;
public class  ShuraArenaBossDamageRewardObject implements baseConfigObject{



private int id;

private int damagel;

private int damageh;

private int[][] reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDamagel(int damagel) {

this.damagel = damagel;

}

public int getDamagel() {

return this.damagel;

}


public void setDamageh(int damageh) {

this.damageh = damageh;

}

public int getDamageh() {

return this.damageh;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}




}
