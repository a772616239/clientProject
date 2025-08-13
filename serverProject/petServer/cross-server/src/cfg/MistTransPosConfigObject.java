package cfg;
import model.base.baseConfigObject;
public class  MistTransPosConfigObject implements baseConfigObject{



private int id;

private int[][] transposlist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setTransposlist(int[][] transposlist) {

this.transposlist = transposlist;

}

public int[][] getTransposlist() {

return this.transposlist;

}




}
