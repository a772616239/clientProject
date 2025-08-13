package cfg;
import model.base.baseConfigObject;
public class  HadesConfigObject implements baseConfigObject{



private int id;

private int defaulttimes;

private int[] dailymission;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDefaulttimes(int defaulttimes) {

this.defaulttimes = defaulttimes;

}

public int getDefaulttimes() {

return this.defaulttimes;

}


public void setDailymission(int[] dailymission) {

this.dailymission = dailymission;

}

public int[] getDailymission() {

return this.dailymission;

}




}
