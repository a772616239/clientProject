package cfg;
import model.base.baseConfigObject;
public class  RankRewardTargetConfigObject implements baseConfigObject{



private int id;

private int[][] reward;

private int targetvalue;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setTargetvalue(int targetvalue) {

this.targetvalue = targetvalue;

}

public int getTargetvalue() {

return this.targetvalue;

}




}
