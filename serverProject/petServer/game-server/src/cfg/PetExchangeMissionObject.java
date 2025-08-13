package cfg;
import model.base.baseConfigObject;
public class  PetExchangeMissionObject implements baseConfigObject{



private int index;

private int name;

private int desc;

private int limit;

private int endtime;

private int[][] rewards;

private int[][] apposeaddition;




public void setIndex(int index) {

this.index = index;

}

public int getIndex() {

return this.index;

}


public void setName(int name) {

this.name = name;

}

public int getName() {

return this.name;

}


public void setDesc(int desc) {

this.desc = desc;

}

public int getDesc() {

return this.desc;

}


public void setLimit(int limit) {

this.limit = limit;

}

public int getLimit() {

return this.limit;

}


public void setEndtime(int endtime) {

this.endtime = endtime;

}

public int getEndtime() {

return this.endtime;

}


public void setRewards(int[][] rewards) {

this.rewards = rewards;

}

public int[][] getRewards() {

return this.rewards;

}


public void setApposeaddition(int[][] apposeaddition) {

this.apposeaddition = apposeaddition;

}

public int[][] getApposeaddition() {

return this.apposeaddition;

}




}
