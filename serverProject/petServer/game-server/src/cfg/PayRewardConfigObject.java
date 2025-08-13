package cfg;
import model.base.baseConfigObject;
public class  PayRewardConfigObject implements baseConfigObject{



private int id;

private int diamondneed;

private int[] reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDiamondneed(int diamondneed) {

this.diamondneed = diamondneed;

}

public int getDiamondneed() {

return this.diamondneed;

}


public void setReward(int[] reward) {

this.reward = reward;

}

public int[] getReward() {

return this.reward;

}




}
