package cfg;
import model.base.baseConfigObject;
public class  ResourceRecycleRewardCfgObject implements baseConfigObject{



private int id;

private int functionid;

private int pointid;

private int rewardid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setFunctionid(int functionid) {

this.functionid = functionid;

}

public int getFunctionid() {

return this.functionid;

}


public void setPointid(int pointid) {

this.pointid = pointid;

}

public int getPointid() {

return this.pointid;

}


public void setRewardid(int rewardid) {

this.rewardid = rewardid;

}

public int getRewardid() {

return this.rewardid;

}




}
