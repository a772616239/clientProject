package cfg;
import model.base.baseConfigObject;
public class  ForeignInvasionRankingRewardObject implements baseConfigObject{



private int id;

private int startranking;

private int endranking;

private int[][] rewards;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStartranking(int startranking) {

this.startranking = startranking;

}

public int getStartranking() {

return this.startranking;

}


public void setEndranking(int endranking) {

this.endranking = endranking;

}

public int getEndranking() {

return this.endranking;

}


public void setRewards(int[][] rewards) {

this.rewards = rewards;

}

public int[][] getRewards() {

return this.rewards;

}




}
