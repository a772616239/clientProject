package cfg;
import model.base.baseConfigObject;
public class  MistCommonRewardConfigObject implements baseConfigObject{



private int id;

private int[][] commonrewardlist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setCommonrewardlist(int[][] commonrewardlist) {

this.commonrewardlist = commonrewardlist;

}

public int[][] getCommonrewardlist() {

return this.commonrewardlist;

}




}
