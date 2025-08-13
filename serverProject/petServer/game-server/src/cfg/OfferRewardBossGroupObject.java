package cfg;
import model.base.baseConfigObject;
public class  OfferRewardBossGroupObject implements baseConfigObject{



private int id;

private int[] allboss;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setAllboss(int[] allboss) {

this.allboss = allboss;

}

public int[] getAllboss() {

return this.allboss;

}




}
