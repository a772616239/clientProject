package cfg;
import model.base.baseConfigObject;
public class  StoneRiftScienceObject implements baseConfigObject{



private int id;

private int function;

private int[] levelprams;

private int maxlevel;

private int[] prveinfo;

private int[] studyconsume;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setFunction(int function) {

this.function = function;

}

public int getFunction() {

return this.function;

}


public void setLevelprams(int[] levelprams) {

this.levelprams = levelprams;

}

public int[] getLevelprams() {

return this.levelprams;

}


public void setMaxlevel(int maxlevel) {

this.maxlevel = maxlevel;

}

public int getMaxlevel() {

return this.maxlevel;

}


public void setPrveinfo(int[] prveinfo) {

this.prveinfo = prveinfo;

}

public int[] getPrveinfo() {

return this.prveinfo;

}


public void setStudyconsume(int[] studyconsume) {

this.studyconsume = studyconsume;

}

public int[] getStudyconsume() {

return this.studyconsume;

}




}
