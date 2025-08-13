package cfg;
import model.base.baseConfigObject;
public class  ResourceRecycleCfgObject implements baseConfigObject{



private int functionid;

private int[] baseconsume;

private int[] advancedconsume;

private int[] params;




public void setFunctionid(int functionid) {

this.functionid = functionid;

}

public int getFunctionid() {

return this.functionid;

}


public void setBaseconsume(int[] baseconsume) {

this.baseconsume = baseconsume;

}

public int[] getBaseconsume() {

return this.baseconsume;

}


public void setAdvancedconsume(int[] advancedconsume) {

this.advancedconsume = advancedconsume;

}

public int[] getAdvancedconsume() {

return this.advancedconsume;

}


public void setParams(int[] params) {

this.params = params;

}

public int[] getParams() {

return this.params;

}




}
