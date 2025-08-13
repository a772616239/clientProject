package cfg;
import model.base.baseConfigObject;
public class  ArenaDanObject implements baseConfigObject{



private int id;

private int servername;

private int roommaxsize;

private int startscore;

private int fightmap;

private int[][] opponentrange;

private int upgradescore;

private int upgraderanking;

private int upgradedirectcount;

private int nextdan;

private boolean needsettledan;

private int[] robotpetpool;

private int[][] danreachreward;

private int titleid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setServername(int servername) {

this.servername = servername;

}

public int getServername() {

return this.servername;

}


public void setRoommaxsize(int roommaxsize) {

this.roommaxsize = roommaxsize;

}

public int getRoommaxsize() {

return this.roommaxsize;

}


public void setStartscore(int startscore) {

this.startscore = startscore;

}

public int getStartscore() {

return this.startscore;

}


public void setFightmap(int fightmap) {

this.fightmap = fightmap;

}

public int getFightmap() {

return this.fightmap;

}


public void setOpponentrange(int[][] opponentrange) {

this.opponentrange = opponentrange;

}

public int[][] getOpponentrange() {

return this.opponentrange;

}


public void setUpgradescore(int upgradescore) {

this.upgradescore = upgradescore;

}

public int getUpgradescore() {

return this.upgradescore;

}


public void setUpgraderanking(int upgraderanking) {

this.upgraderanking = upgraderanking;

}

public int getUpgraderanking() {

return this.upgraderanking;

}


public void setUpgradedirectcount(int upgradedirectcount) {

this.upgradedirectcount = upgradedirectcount;

}

public int getUpgradedirectcount() {

return this.upgradedirectcount;

}


public void setNextdan(int nextdan) {

this.nextdan = nextdan;

}

public int getNextdan() {

return this.nextdan;

}


public void setNeedsettledan(boolean needsettledan) {

this.needsettledan = needsettledan;

}

public boolean getNeedsettledan() {

return this.needsettledan;

}


public void setRobotpetpool(int[] robotpetpool) {

this.robotpetpool = robotpetpool;

}

public int[] getRobotpetpool() {

return this.robotpetpool;

}


public void setDanreachreward(int[][] danreachreward) {

this.danreachreward = danreachreward;

}

public int[][] getDanreachreward() {

return this.danreachreward;

}


public void setTitleid(int titleid) {

this.titleid = titleid;

}

public int getTitleid() {

return this.titleid;

}




}
