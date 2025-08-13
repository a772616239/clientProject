package cfg;
import model.base.baseConfigObject;
public class  HadesWorshipConfigObject implements baseConfigObject{



private int id;

private int[] consume;

private int[][] returnrewards;




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


public void setReturnrewards(int[][] returnrewards) {

this.returnrewards = returnrewards;

}

public int[][] getReturnrewards() {

return this.returnrewards;

}




}
