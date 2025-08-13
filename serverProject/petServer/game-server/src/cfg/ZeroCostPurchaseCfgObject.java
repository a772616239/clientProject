package cfg;
import model.base.baseConfigObject;
public class  ZeroCostPurchaseCfgObject implements baseConfigObject{



private int id;

private int[] consume;

private int[][] delayreward;

private int[] instantreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setConsume(int[] consume) {

this.consume = consume;

}

public int[] getConsume() {

return this.consume;

}


public void setDelayreward(int[][] delayreward) {

this.delayreward = delayreward;

}

public int[][] getDelayreward() {

return this.delayreward;

}


public void setInstantreward(int[] instantreward) {

this.instantreward = instantreward;

}

public int[] getInstantreward() {

return this.instantreward;

}




}
