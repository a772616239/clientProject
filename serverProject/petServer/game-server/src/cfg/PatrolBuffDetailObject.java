package cfg;
import model.base.baseConfigObject;
public class  PatrolBuffDetailObject implements baseConfigObject{



private int buffid;

private int buffcamp;

private int bufftype;

private int effecttype;

private int effectratge;

private int buffmaxcount;




public void setBuffid(int buffid) {

this.buffid = buffid;

}

public int getBuffid() {

return this.buffid;

}


public void setBuffcamp(int buffcamp) {

this.buffcamp = buffcamp;

}

public int getBuffcamp() {

return this.buffcamp;

}


public void setBufftype(int bufftype) {

this.bufftype = bufftype;

}

public int getBufftype() {

return this.bufftype;

}


public void setEffecttype(int effecttype) {

this.effecttype = effecttype;

}

public int getEffecttype() {

return this.effecttype;

}


public void setEffectratge(int effectratge) {

this.effectratge = effectratge;

}

public int getEffectratge() {

return this.effectratge;

}


public void setBuffmaxcount(int buffmaxcount) {

this.buffmaxcount = buffmaxcount;

}

public int getBuffmaxcount() {

return this.buffmaxcount;

}




}
