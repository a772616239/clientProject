package cfg;
import model.base.baseConfigObject;
public class  MainLineQuickOnHookObject implements baseConfigObject{



private int id;

private int time;

private int[] consumes;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setTime(int time) {

this.time = time;

}

public int getTime() {

return this.time;

}


public void setConsumes(int[] consumes) {

this.consumes = consumes;

}

public int[] getConsumes() {

return this.consumes;

}




}
