package cfg;
import model.base.baseConfigObject;

public class  MatchArenaDanConfigObject implements baseConfigObject{



private int id;

private int servername;

private int needscore;

private int medallimit;

private int medalgainrate;

private int[][] danrewards;

private int battlepetlimit;

private int matchaifailtimes;

private int canmatchrobottime;

private int expandmatchinterval;

private int onceexpandscore;

private int maxscorediff;




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


public void setNeedscore(int needscore) {

this.needscore = needscore;

}

public int getNeedscore() {

return this.needscore;

}


public void setMedallimit(int medallimit) {

this.medallimit = medallimit;

}

public int getMedallimit() {

return this.medallimit;

}


public void setMedalgainrate(int medalgainrate) {

this.medalgainrate = medalgainrate;

}

public int getMedalgainrate() {

return this.medalgainrate;

}


public void setDanrewards(int[][] danrewards) {

this.danrewards = danrewards;

}

public int[][] getDanrewards() {

return this.danrewards;

}


public void setBattlepetlimit(int battlepetlimit) {

this.battlepetlimit = battlepetlimit;

}

public int getBattlepetlimit() {

return this.battlepetlimit;

}


public void setMatchaifailtimes(int matchaifailtimes) {

this.matchaifailtimes = matchaifailtimes;

}

public int getMatchaifailtimes() {

return this.matchaifailtimes;

}


public void setCanmatchrobottime(int canmatchrobottime) {

this.canmatchrobottime = canmatchrobottime;

}

public int getCanmatchrobottime() {

return this.canmatchrobottime;

}


public void setExpandmatchinterval(int expandmatchinterval) {

this.expandmatchinterval = expandmatchinterval;

}

public int getExpandmatchinterval() {

return this.expandmatchinterval;

}


public void setOnceexpandscore(int onceexpandscore) {

this.onceexpandscore = onceexpandscore;

}

public int getOnceexpandscore() {

return this.onceexpandscore;

}


public void setMaxscorediff(int maxscorediff) {

this.maxscorediff = maxscorediff;

}

public int getMaxscorediff() {

return this.maxscorediff;

}




}
