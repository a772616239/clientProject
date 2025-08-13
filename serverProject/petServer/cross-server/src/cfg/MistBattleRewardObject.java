package cfg;
import model.base.baseConfigObject;
public class  MistBattleRewardObject implements baseConfigObject{



private int level;

private int beatbossreward;

private int beatbossteamreward;

private int beathiddenevilreward;




public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setBeatbossreward(int beatbossreward) {

this.beatbossreward = beatbossreward;

}

public int getBeatbossreward() {

return this.beatbossreward;

}


public void setBeatbossteamreward(int beatbossteamreward) {

this.beatbossteamreward = beatbossteamreward;

}

public int getBeatbossteamreward() {

return this.beatbossteamreward;

}


public void setBeathiddenevilreward(int beathiddenevilreward) {

this.beathiddenevilreward = beathiddenevilreward;

}

public int getBeathiddenevilreward() {

return this.beathiddenevilreward;

}




}
