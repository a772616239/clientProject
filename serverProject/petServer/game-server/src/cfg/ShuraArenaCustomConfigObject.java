package cfg;
import model.base.baseConfigObject;
public class  ShuraArenaCustomConfigObject implements baseConfigObject{



private int id;

private int custom;

private int robotname;

private int avatar;

private int reward;

private int score;

private int buff;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setCustom(int custom) {

this.custom = custom;

}

public int getCustom() {

return this.custom;

}


public void setRobotname(int robotname) {

this.robotname = robotname;

}

public int getRobotname() {

return this.robotname;

}


public void setAvatar(int avatar) {

this.avatar = avatar;

}

public int getAvatar() {

return this.avatar;

}


public void setReward(int reward) {

this.reward = reward;

}

public int getReward() {

return this.reward;

}


public void setScore(int score) {

this.score = score;

}

public int getScore() {

return this.score;

}


public void setBuff(int buff) {

this.buff = buff;

}

public int getBuff() {

return this.buff;

}




}
