package cfg;
import model.base.baseConfigObject;
public class  MistRebornChangePropObject implements baseConfigObject{



private int id;

private int[] proptype;

private int[][] propchange;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setProptype(int[] proptype) {

this.proptype = proptype;

}

public int[] getProptype() {

return this.proptype;

}


public void setPropchange(int[][] propchange) {

this.propchange = propchange;

}

public int[][] getPropchange() {

return this.propchange;

}




}
