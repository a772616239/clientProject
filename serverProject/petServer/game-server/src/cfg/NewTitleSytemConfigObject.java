package cfg;
import model.base.baseConfigObject;
public class  NewTitleSytemConfigObject implements baseConfigObject{



private int id;

private int[][] addproperty;

private int limittime;

private int servername;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setAddproperty(int[][] addproperty) {

this.addproperty = addproperty;

}

public int[][] getAddproperty() {

return this.addproperty;

}


public void setLimittime(int limittime) {

this.limittime = limittime;

}

public int getLimittime() {

return this.limittime;

}


public void setServername(int servername) {

this.servername = servername;

}

public int getServername() {

return this.servername;

}




}
