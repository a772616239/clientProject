package cfg;
import model.base.baseConfigObject;
public class  CrossArenaRobotObject implements baseConfigObject{



private int id;

private int rank;

private int honrlv;

private int[] team;

private int[] level;

private int[] rarity;

private int winnum;

private int usetype;

private int difficult;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRank(int rank) {

this.rank = rank;

}

public int getRank() {

return this.rank;

}


public void setHonrlv(int honrlv) {

this.honrlv = honrlv;

}

public int getHonrlv() {

return this.honrlv;

}


public void setTeam(int[] team) {

this.team = team;

}

public int[] getTeam() {

return this.team;

}


public void setLevel(int[] level) {

this.level = level;

}

public int[] getLevel() {

return this.level;

}


public void setRarity(int[] rarity) {

this.rarity = rarity;

}

public int[] getRarity() {

return this.rarity;

}


public void setWinnum(int winnum) {

this.winnum = winnum;

}

public int getWinnum() {

return this.winnum;

}


public void setUsetype(int usetype) {

this.usetype = usetype;

}

public int getUsetype() {

return this.usetype;

}


public void setDifficult(int difficult) {

this.difficult = difficult;

}

public int getDifficult() {

return this.difficult;

}




}
