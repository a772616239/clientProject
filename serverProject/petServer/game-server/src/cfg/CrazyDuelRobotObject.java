package cfg;
import model.base.baseConfigObject;
public class  CrazyDuelRobotObject implements baseConfigObject{



private int id;

private int level;

private int startscore;

private int endscore;

private int needcount;

private int[][] petcount;

private int[] petlvrange;

private int honrlv;

private int openfloor;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setStartscore(int startscore) {

this.startscore = startscore;

}

public int getStartscore() {

return this.startscore;

}


public void setEndscore(int endscore) {

this.endscore = endscore;

}

public int getEndscore() {

return this.endscore;

}


public void setNeedcount(int needcount) {

this.needcount = needcount;

}

public int getNeedcount() {

return this.needcount;

}


public void setPetcount(int[][] petcount) {

this.petcount = petcount;

}

public int[][] getPetcount() {

return this.petcount;

}


public void setPetlvrange(int[] petlvrange) {

this.petlvrange = petlvrange;

}

public int[] getPetlvrange() {

return this.petlvrange;

}


public void setHonrlv(int honrlv) {

this.honrlv = honrlv;

}

public int getHonrlv() {

return this.honrlv;

}


public void setOpenfloor(int openfloor) {

this.openfloor = openfloor;

}

public int getOpenfloor() {

return this.openfloor;

}




}
