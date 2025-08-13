package cfg;
import model.base.baseConfigObject;
public class  OfferRewardLevelLimitObject implements baseConfigObject{



private int id;

private int boss;

private int[][] boss1;

private int[][] boss2;

private int[][] boss3;

private int[][] boss4;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setBoss(int boss) {

this.boss = boss;

}

public int getBoss() {

return this.boss;

}


public void setBoss1(int[][] boss1) {

this.boss1 = boss1;

}

public int[][] getBoss1() {

return this.boss1;

}


public void setBoss2(int[][] boss2) {

this.boss2 = boss2;

}

public int[][] getBoss2() {

return this.boss2;

}


public void setBoss3(int[][] boss3) {

this.boss3 = boss3;

}

public int[][] getBoss3() {

return this.boss3;

}


public void setBoss4(int[][] boss4) {

this.boss4 = boss4;

}

public int[][] getBoss4() {

return this.boss4;

}




}
