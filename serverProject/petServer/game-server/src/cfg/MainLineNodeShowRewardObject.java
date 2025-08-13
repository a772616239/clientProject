package cfg;
import model.base.baseConfigObject;
public class  MainLineNodeShowRewardObject implements baseConfigObject{



private int id;

private int[] onshowreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setOnshowreward(int[] onshowreward) {

this.onshowreward = onshowreward;

}

public int[] getOnshowreward() {

return this.onshowreward;

}




}
