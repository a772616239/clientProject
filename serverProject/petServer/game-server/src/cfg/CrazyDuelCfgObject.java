package cfg;
import model.base.baseConfigObject;
public class  CrazyDuelCfgObject implements baseConfigObject{



private int id;

private int[] buffpool;

private int[][] floorbuffsize;

private int[] offertime;

private int offerplay;

private int matchscorediff;

private int playerinitscore;

private int pagesize;

private int[][] defendablityincr;

private int[][] matchscope;

private int refreshunlockgradelv;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setBuffpool(int[] buffpool) {

this.buffpool = buffpool;

}

public int[] getBuffpool() {

return this.buffpool;

}


public void setFloorbuffsize(int[][] floorbuffsize) {

this.floorbuffsize = floorbuffsize;

}

public int[][] getFloorbuffsize() {

return this.floorbuffsize;

}


public void setOffertime(int[] offertime) {

this.offertime = offertime;

}

public int[] getOffertime() {

return this.offertime;

}


public void setOfferplay(int offerplay) {

this.offerplay = offerplay;

}

public int getOfferplay() {

return this.offerplay;

}


public void setMatchscorediff(int matchscorediff) {

this.matchscorediff = matchscorediff;

}

public int getMatchscorediff() {

return this.matchscorediff;

}


public void setPlayerinitscore(int playerinitscore) {

this.playerinitscore = playerinitscore;

}

public int getPlayerinitscore() {

return this.playerinitscore;

}


public void setPagesize(int pagesize) {

this.pagesize = pagesize;

}

public int getPagesize() {

return this.pagesize;

}


public void setDefendablityincr(int[][] defendablityincr) {

this.defendablityincr = defendablityincr;

}

public int[][] getDefendablityincr() {

return this.defendablityincr;

}


public void setMatchscope(int[][] matchscope) {

this.matchscope = matchscope;

}

public int[][] getMatchscope() {

return this.matchscope;

}


public void setRefreshunlockgradelv(int refreshunlockgradelv) {

this.refreshunlockgradelv = refreshunlockgradelv;

}

public int getRefreshunlockgradelv() {

return this.refreshunlockgradelv;

}




}
