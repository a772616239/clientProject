package cfg;
import model.base.baseConfigObject;
public class  RollCardPoolObject implements baseConfigObject{



private int id;

private int[] reward;

private int grade;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setReward(int[] reward) {

this.reward = reward;

}

public int[] getReward() {

return this.reward;

}


public void setGrade(int grade) {

this.grade = grade;

}

public int getGrade() {

return this.grade;

}




}
