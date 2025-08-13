package cfg;
import model.base.baseConfigObject;
public class  ExchangeAdditionObject implements baseConfigObject{



private int index;

private int type;

private int count;

private int[][] addition;




public void setIndex(int index) {

this.index = index;

}

public int getIndex() {

return this.index;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setCount(int count) {

this.count = count;

}

public int getCount() {

return this.count;

}


public void setAddition(int[][] addition) {

this.addition = addition;

}

public int[][] getAddition() {

return this.addition;

}




}
