package cfg;

import model.base.baseConfigObject;
public class  MainLineMissionObject implements baseConfigObject{



private int id;

private int[][] rewards;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRewards(int[][] rewards) {

this.rewards = rewards;

}

public int[][] getRewards() {

return this.rewards;

}




}
