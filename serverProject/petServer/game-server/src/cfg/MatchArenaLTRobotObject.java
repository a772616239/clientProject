package cfg;
import model.base.baseConfigObject;
public class  MatchArenaLTRobotObject implements baseConfigObject{



private int id;

private int rank;

private int[] team;

private int[] level;

private int[] rarity;

private int winnum;




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




}
