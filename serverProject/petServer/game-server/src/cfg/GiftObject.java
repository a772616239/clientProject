package cfg;
import model.base.baseConfigObject;
public class  GiftObject implements baseConfigObject{



private int id;

private int type;

private int[] consume;

private int[][] reward;

private int limit;

private int vipexp;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setConsume(int[] consume) {

this.consume = consume;

}

public int[] getConsume() {

return this.consume;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setLimit(int limit) {

this.limit = limit;

}

public int getLimit() {

return this.limit;

}


public void setVipexp(int vipexp) {

this.vipexp = vipexp;

}

public int getVipexp() {

return this.vipexp;

}




}
