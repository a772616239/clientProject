package cfg;
import model.base.baseConfigObject;
public class  WishWellConfigObject implements baseConfigObject{



private int id;

private int startday;

private int claimday;

private int rewardoptions;

private int[] makeupprice;

private int[] replenishsignprice;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStartday(int startday) {

this.startday = startday;

}

public int getStartday() {

return this.startday;

}


public void setClaimday(int claimday) {

this.claimday = claimday;

}

public int getClaimday() {

return this.claimday;

}


public void setRewardoptions(int rewardoptions) {

this.rewardoptions = rewardoptions;

}

public int getRewardoptions() {

return this.rewardoptions;

}


public void setMakeupprice(int[] makeupprice) {

this.makeupprice = makeupprice;

}

public int[] getMakeupprice() {

return this.makeupprice;

}


public void setReplenishsignprice(int[] replenishsignprice) {

this.replenishsignprice = replenishsignprice;

}

public int[] getReplenishsignprice() {

return this.replenishsignprice;

}




}
