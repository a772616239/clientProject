package cfg;
import model.base.baseConfigObject;
public class  CrossArenaWinTaskObject implements baseConfigObject{



private int id;

private int winning;

private int sceneid;

private int type;

private int reward;

private int random_reward;

private int random;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setWinning(int winning) {

this.winning = winning;

}

public int getWinning() {

return this.winning;

}


public void setSceneid(int sceneid) {

this.sceneid = sceneid;

}

public int getSceneid() {

return this.sceneid;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setReward(int reward) {

this.reward = reward;

}

public int getReward() {

return this.reward;

}


public void setRandom_reward(int random_reward) {

this.random_reward = random_reward;

}

public int getRandom_reward() {

return this.random_reward;

}


public void setRandom(int random) {

this.random = random;

}

public int getRandom() {

return this.random;

}




}
