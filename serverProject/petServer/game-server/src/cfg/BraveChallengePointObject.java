package cfg;
import model.base.baseConfigObject;
public class  BraveChallengePointObject implements baseConfigObject{



private int id;

private int pointtype;

private int riviselv;

private int fightmake;

private boolean needboss;

private int[] exproperty;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setPointtype(int pointtype) {

this.pointtype = pointtype;

}

public int getPointtype() {

return this.pointtype;

}


public void setRiviselv(int riviselv) {

this.riviselv = riviselv;

}

public int getRiviselv() {

return this.riviselv;

}


public void setFightmake(int fightmake) {

this.fightmake = fightmake;

}

public int getFightmake() {

return this.fightmake;

}


public void setNeedboss(boolean needboss) {

this.needboss = needboss;

}

public boolean getNeedboss() {

return this.needboss;

}


public void setExproperty(int[] exproperty) {

this.exproperty = exproperty;

}

public int[] getExproperty() {

return this.exproperty;

}




}
