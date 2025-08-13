package cfg;
import model.base.baseConfigObject;
public class  MistBusinessManRewardObject implements baseConfigObject{



private int id;

private int[][] finishreward;

private int[][] failedreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setFinishreward(int[][] finishreward) {

this.finishreward = finishreward;

}

public int[][] getFinishreward() {

return this.finishreward;

}


public void setFailedreward(int[][] failedreward) {

this.failedreward = failedreward;

}

public int[][] getFailedreward() {

return this.failedreward;

}




}
