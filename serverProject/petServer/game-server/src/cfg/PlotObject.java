package cfg;
import model.base.baseConfigObject;
public class  PlotObject implements baseConfigObject{



private int id;

private String helppetid;

private int[] rewardlist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setHelppetid(String helppetid) {

this.helppetid = helppetid;

}

public String getHelppetid() {

return this.helppetid;

}


public void setRewardlist(int[] rewardlist) {

this.rewardlist = rewardlist;

}

public int[] getRewardlist() {

return this.rewardlist;

}




}
