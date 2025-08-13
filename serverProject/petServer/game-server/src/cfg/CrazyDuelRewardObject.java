package cfg;
import model.base.baseConfigObject;
public class  CrazyDuelRewardObject implements baseConfigObject{



private int id;

private int lowerlimit;

private int upperlimit;

private int[][] reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLowerlimit(int lowerlimit) {

this.lowerlimit = lowerlimit;

}

public int getLowerlimit() {

return this.lowerlimit;

}


public void setUpperlimit(int upperlimit) {

this.upperlimit = upperlimit;

}

public int getUpperlimit() {

return this.upperlimit;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}




}
