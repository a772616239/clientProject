package cfg;
import model.base.baseConfigObject;

public class  PetRunePropertiesObject implements baseConfigObject{



private int runeid;

private int runename;

private String severname;

private int runerarity;

private int runetype;

private int[][] baseproperties;

private int[] expropertiesrange;

private int[][] exproperties;

private int runesuit;




public void setRuneid(int runeid) {

this.runeid = runeid;

}

public int getRuneid() {

return this.runeid;

}


public void setRunename(int runename) {

this.runename = runename;

}

public int getRunename() {

return this.runename;

}


public void setSevername(String severname) {

this.severname = severname;

}

public String getSevername() {

return this.severname;

}


public void setRunerarity(int runerarity) {

this.runerarity = runerarity;

}

public int getRunerarity() {

return this.runerarity;

}


public void setRunetype(int runetype) {

this.runetype = runetype;

}

public int getRunetype() {

return this.runetype;

}


public void setBaseproperties(int[][] baseproperties) {

this.baseproperties = baseproperties;

}

public int[][] getBaseproperties() {

return this.baseproperties;

}


public void setExpropertiesrange(int[] expropertiesrange) {

this.expropertiesrange = expropertiesrange;

}

public int[] getExpropertiesrange() {

return this.expropertiesrange;

}


public void setExproperties(int[][] exproperties) {

this.exproperties = exproperties;

}

public int[][] getExproperties() {

return this.exproperties;

}


public void setRunesuit(int runesuit) {

this.runesuit = runesuit;

}

public int getRunesuit() {

return this.runesuit;

}




}
