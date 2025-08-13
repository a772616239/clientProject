package cfg;
import model.base.baseConfigObject;
public class  BossTowerBuyTimeConsumeObject implements baseConfigObject{



private int id;

private int[] consume;




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




}
