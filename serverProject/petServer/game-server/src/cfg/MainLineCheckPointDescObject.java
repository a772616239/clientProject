package cfg;
import model.base.baseConfigObject;
public class  MainLineCheckPointDescObject implements baseConfigObject{



private int id;

private int[] nodelist;

private int finishedreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setNodelist(int[] nodelist) {

this.nodelist = nodelist;

}

public int[] getNodelist() {

return this.nodelist;

}


public void setFinishedreward(int finishedreward) {

this.finishedreward = finishedreward;

}

public int getFinishedreward() {

return this.finishedreward;

}




}
