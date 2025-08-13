package cfg;
import model.base.baseConfigObject;
public class  MistEliteMonsterConfigObject implements baseConfigObject{



private int id;

private int maxrewardtimes;

private int[][] radnombattlereward;

private int[][] mustgainreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMaxrewardtimes(int maxrewardtimes) {

this.maxrewardtimes = maxrewardtimes;

}

public int getMaxrewardtimes() {

return this.maxrewardtimes;

}


public void setRadnombattlereward(int[][] radnombattlereward) {

this.radnombattlereward = radnombattlereward;

}

public int[][] getRadnombattlereward() {

return this.radnombattlereward;

}


public void setMustgainreward(int[][] mustgainreward) {

this.mustgainreward = mustgainreward;

}

public int[][] getMustgainreward() {

return this.mustgainreward;

}




}
