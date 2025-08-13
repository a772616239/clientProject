package cfg;
import model.base.baseConfigObject;
public class  RankRewardRangeConfigObject implements baseConfigObject{



private int id;

private int rangemin;

private int rangemax;

private int[][] reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRangemin(int rangemin) {

this.rangemin = rangemin;

}

public int getRangemin() {

return this.rangemin;

}


public void setRangemax(int rangemax) {

this.rangemax = rangemax;

}

public int getRangemax() {

return this.rangemax;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}




}
