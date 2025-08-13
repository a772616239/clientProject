package cfg;
import model.base.baseConfigObject;
public class  PetRuneKindCfgObject implements baseConfigObject{



private int runekind;

private int[] suitids;




public void setRunekind(int runekind) {

this.runekind = runekind;

}

public int getRunekind() {

return this.runekind;

}


public void setSuitids(int[] suitids) {

this.suitids = suitids;

}

public int[] getSuitids() {

return this.suitids;

}




}
