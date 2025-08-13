package cfg;
import model.base.baseConfigObject;
public class  CpTeamCfgObject implements baseConfigObject{



private int id;

private int[][] eventhappen;

private int[] stareventreward;

private int[][] treasurerewardpool;

private int[][] buffpool;

private int[][] diffcultscore;

private int freetimes;

private int teamplayernum;

private int copytime;

private int[] buyreviceconsume;

private int[] buygameplayconsume;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setEventhappen(int[][] eventhappen) {

this.eventhappen = eventhappen;

}

public int[][] getEventhappen() {

return this.eventhappen;

}


public void setStareventreward(int[] stareventreward) {

this.stareventreward = stareventreward;

}

public int[] getStareventreward() {

return this.stareventreward;

}


public void setTreasurerewardpool(int[][] treasurerewardpool) {

this.treasurerewardpool = treasurerewardpool;

}

public int[][] getTreasurerewardpool() {

return this.treasurerewardpool;

}


public void setBuffpool(int[][] buffpool) {

this.buffpool = buffpool;

}

public int[][] getBuffpool() {

return this.buffpool;

}


public void setDiffcultscore(int[][] diffcultscore) {

this.diffcultscore = diffcultscore;

}

public int[][] getDiffcultscore() {

return this.diffcultscore;

}


public void setFreetimes(int freetimes) {

this.freetimes = freetimes;

}

public int getFreetimes() {

return this.freetimes;

}


public void setTeamplayernum(int teamplayernum) {

this.teamplayernum = teamplayernum;

}

public int getTeamplayernum() {

return this.teamplayernum;

}


public void setCopytime(int copytime) {

this.copytime = copytime;

}

public int getCopytime() {

return this.copytime;

}


public void setBuyreviceconsume(int[] buyreviceconsume) {

this.buyreviceconsume = buyreviceconsume;

}

public int[] getBuyreviceconsume() {

return this.buyreviceconsume;

}


public void setBuygameplayconsume(int[] buygameplayconsume) {

this.buygameplayconsume = buygameplayconsume;

}

public int[] getBuygameplayconsume() {

return this.buygameplayconsume;

}




}
