package cfg;
import model.base.baseConfigObject;
public class  CrossArenaLvTaskObject implements baseConfigObject{



private int id;

private int type;

private int value;

private int[][] reward;




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


public void setValue(int value) {

this.value = value;

}

public int getValue() {

return this.value;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}




}
