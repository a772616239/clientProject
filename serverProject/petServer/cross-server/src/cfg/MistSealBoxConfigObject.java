package cfg;
import model.base.baseConfigObject;
public class  MistSealBoxConfigObject implements baseConfigObject{



private int id;

private int columncount;

private int[] needitemlist;

private int[][] failedsubmitreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setColumncount(int columncount) {

this.columncount = columncount;

}

public int getColumncount() {

return this.columncount;

}


public void setNeeditemlist(int[] needitemlist) {

this.needitemlist = needitemlist;

}

public int[] getNeeditemlist() {

return this.needitemlist;

}


public void setFailedsubmitreward(int[][] failedsubmitreward) {

this.failedsubmitreward = failedsubmitreward;

}

public int[][] getFailedsubmitreward() {

return this.failedsubmitreward;

}




}
