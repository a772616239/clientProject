package cfg;
import model.base.baseConfigObject;
public class  ActivityBossRewardObject implements baseConfigObject{



private int times;

private int playerlv;

private int[][] mustreward;

private long damagebase;

private int[][] randomreward;




public void setTimes(int times) {

this.times = times;

}

public int getTimes() {

return this.times;

}


public void setPlayerlv(int playerlv) {

this.playerlv = playerlv;

}

public int getPlayerlv() {

return this.playerlv;

}


public void setMustreward(int[][] mustreward) {

this.mustreward = mustreward;

}

public int[][] getMustreward() {

return this.mustreward;

}


public void setDamagebase(long damagebase) {

this.damagebase = damagebase;

}

public long getDamagebase() {

return this.damagebase;

}


public void setRandomreward(int[][] randomreward) {

this.randomreward = randomreward;

}

public int[][] getRandomreward() {

return this.randomreward;

}




}
