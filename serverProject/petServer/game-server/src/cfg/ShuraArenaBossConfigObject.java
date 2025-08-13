package cfg;
import model.base.baseConfigObject;
public class  ShuraArenaBossConfigObject implements baseConfigObject{



private int id;

private int[] fightmakeid;

private int difficult;

private int area;

private int scoreaddition;

private int bossbuff;

private int playerbuff;

private int petlvincr;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setFightmakeid(int[] fightmakeid) {

this.fightmakeid = fightmakeid;

}

public int[] getFightmakeid() {

return this.fightmakeid;

}


public void setDifficult(int difficult) {

this.difficult = difficult;

}

public int getDifficult() {

return this.difficult;

}


public void setArea(int area) {

this.area = area;

}

public int getArea() {

return this.area;

}


public void setScoreaddition(int scoreaddition) {

this.scoreaddition = scoreaddition;

}

public int getScoreaddition() {

return this.scoreaddition;

}


public void setBossbuff(int bossbuff) {

this.bossbuff = bossbuff;

}

public int getBossbuff() {

return this.bossbuff;

}


public void setPlayerbuff(int playerbuff) {

this.playerbuff = playerbuff;

}

public int getPlayerbuff() {

return this.playerbuff;

}


public void setPetlvincr(int petlvincr) {

this.petlvincr = petlvincr;

}

public int getPetlvincr() {

return this.petlvincr;

}




}
