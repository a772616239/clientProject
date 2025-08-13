package cfg;
import model.base.baseConfigObject;
public class  AchievementObject implements baseConfigObject{



private int id;

private int type;

private int addtiomcondition;

private int[][] targetcount;




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


public void setAddtiomcondition(int addtiomcondition) {

this.addtiomcondition = addtiomcondition;

}

public int getAddtiomcondition() {

return this.addtiomcondition;

}


public void setTargetcount(int[][] targetcount) {

this.targetcount = targetcount;

}

public int[][] getTargetcount() {

return this.targetcount;

}




}
