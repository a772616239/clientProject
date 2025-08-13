package cfg;
import model.base.baseConfigObject;
public class  CollectingrewardsObject implements baseConfigObject{



private int id;

private int count;

private int[][] awards;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setCount(int count) {

this.count = count;

}

public int getCount() {

return this.count;

}


public void setAwards(int[][] awards) {

this.awards = awards;

}

public int[][] getAwards() {

return this.awards;

}




}
