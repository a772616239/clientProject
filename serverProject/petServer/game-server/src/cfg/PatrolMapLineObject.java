package cfg;
import model.base.baseConfigObject;
public class  PatrolMapLineObject implements baseConfigObject{



private int lineid;

private int[][] pointlist;




public void setLineid(int lineid) {

this.lineid = lineid;

}

public int getLineid() {

return this.lineid;

}


public void setPointlist(int[][] pointlist) {

this.pointlist = pointlist;

}

public int[][] getPointlist() {

return this.pointlist;

}




}
