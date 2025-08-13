package cfg;
import model.base.baseConfigObject;
public class  PetBondConfigObject implements baseConfigObject{



private String id;

private int buffid;

private int monsterbuffid;




public void setId(String id) {

this.id = id;

}

public String getId() {

return this.id;

}


public void setBuffid(int buffid) {

this.buffid = buffid;

}

public int getBuffid() {

return this.buffid;

}


public void setMonsterbuffid(int monsterbuffid) {

this.monsterbuffid = monsterbuffid;

}

public int getMonsterbuffid() {

return this.monsterbuffid;

}




}
