package cfg;
import model.base.baseConfigObject;
public class  PatrolRewardObject implements baseConfigObject{



private int id;

private String rewardtype;

private int[] rewardrange;

private int[][] randomreward;

private int[][] fixedreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRewardtype(String rewardtype) {

this.rewardtype = rewardtype;

}

public String getRewardtype() {

return this.rewardtype;

}


public void setRewardrange(int[] rewardrange) {

this.rewardrange = rewardrange;

}

public int[] getRewardrange() {

return this.rewardrange;

}


public void setRandomreward(int[][] randomreward) {

this.randomreward = randomreward;

}

public int[][] getRandomreward() {

return this.randomreward;

}


public void setFixedreward(int[][] fixedreward) {

this.fixedreward = fixedreward;

}

public int[][] getFixedreward() {

return this.fixedreward;

}




}
