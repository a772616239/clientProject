package cfg;
import model.base.baseConfigObject;
public class  MistFateDoorConfigObject implements baseConfigObject{



private int id;

private int[][] rewardobjlist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRewardobjlist(int[][] rewardobjlist) {

this.rewardobjlist = rewardobjlist;

}

public int[][] getRewardobjlist() {

return this.rewardobjlist;

}




}
