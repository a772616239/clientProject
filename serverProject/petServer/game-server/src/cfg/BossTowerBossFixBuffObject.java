package cfg;
import model.base.baseConfigObject;
public class  BossTowerBossFixBuffObject implements baseConfigObject{



private int id;

private int[] playerbuff;

private int[] bossbuff;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setPlayerbuff(int[] playerbuff) {

this.playerbuff = playerbuff;

}

public int[] getPlayerbuff() {

return this.playerbuff;

}


public void setBossbuff(int[] bossbuff) {

this.bossbuff = bossbuff;

}

public int[] getBossbuff() {

return this.bossbuff;

}




}
