package cfg;
import model.base.baseConfigObject;

public class  MatchArenaLTObject implements baseConfigObject{



private int id;

private int[][] stageltnum;

private int[] winbuff;

private int[] missionlist;

private int winguessnum;

private int extguessnum;

private int wingrade;

private int failgrade;

private int[][] timegrade;

private int[][] wingradeadd;

private int[][] beatgradeadd;

private int ailimit;

private int aimintime;

private int aimaxtime;

private int settlementtime;

private int fightmakeid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStageltnum(int[][] stageltnum) {

this.stageltnum = stageltnum;

}

public int[][] getStageltnum() {

return this.stageltnum;

}


public void setWinbuff(int[] winbuff) {

this.winbuff = winbuff;

}

public int[] getWinbuff() {

return this.winbuff;

}


public void setMissionlist(int[] missionlist) {

this.missionlist = missionlist;

}

public int[] getMissionlist() {

return this.missionlist;

}


public void setWinguessnum(int winguessnum) {

this.winguessnum = winguessnum;

}

public int getWinguessnum() {

return this.winguessnum;

}


public void setExtguessnum(int extguessnum) {

this.extguessnum = extguessnum;

}

public int getExtguessnum() {

return this.extguessnum;

}


public void setWingrade(int wingrade) {

this.wingrade = wingrade;

}

public int getWingrade() {

return this.wingrade;

}


public void setFailgrade(int failgrade) {

this.failgrade = failgrade;

}

public int getFailgrade() {

return this.failgrade;

}


public void setTimegrade(int[][] timegrade) {

this.timegrade = timegrade;

}

public int[][] getTimegrade() {

return this.timegrade;

}


public void setWingradeadd(int[][] wingradeadd) {

this.wingradeadd = wingradeadd;

}

public int[][] getWingradeadd() {

return this.wingradeadd;

}


public void setBeatgradeadd(int[][] beatgradeadd) {

this.beatgradeadd = beatgradeadd;

}

public int[][] getBeatgradeadd() {

return this.beatgradeadd;

}


public void setAilimit(int ailimit) {

this.ailimit = ailimit;

}

public int getAilimit() {

return this.ailimit;

}


public void setAimintime(int aimintime) {

this.aimintime = aimintime;

}

public int getAimintime() {

return this.aimintime;

}


public void setAimaxtime(int aimaxtime) {

this.aimaxtime = aimaxtime;

}

public int getAimaxtime() {

return this.aimaxtime;

}


public void setSettlementtime(int settlementtime) {

this.settlementtime = settlementtime;

}

public int getSettlementtime() {

return this.settlementtime;

}


public void setFightmakeid(int fightmakeid) {

this.fightmakeid = fightmakeid;

}

public int getFightmakeid() {

return this.fightmakeid;

}




}
