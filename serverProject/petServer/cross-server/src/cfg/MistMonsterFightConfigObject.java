package cfg;
import model.base.baseConfigObject;
public class  MistMonsterFightConfigObject implements baseConfigObject{



private int id;

private int[] fightmakeid;

private int[][] battlereward;

private int[][] batterrewardobj;

private int[][] battleteamreward;

private int[][] rewardjewelrycount;

private int monstertype;

private int[][] rewardlavabadgecount;

private int directsettledecreasehp;

private int directsettlefightpower;




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


public void setBattlereward(int[][] battlereward) {

this.battlereward = battlereward;

}

public int[][] getBattlereward() {

return this.battlereward;

}


public void setBatterrewardobj(int[][] batterrewardobj) {

this.batterrewardobj = batterrewardobj;

}

public int[][] getBatterrewardobj() {

return this.batterrewardobj;

}


public void setBattleteamreward(int[][] battleteamreward) {

this.battleteamreward = battleteamreward;

}

public int[][] getBattleteamreward() {

return this.battleteamreward;

}


public void setRewardjewelrycount(int[][] rewardjewelrycount) {

this.rewardjewelrycount = rewardjewelrycount;

}

public int[][] getRewardjewelrycount() {

return this.rewardjewelrycount;

}


public void setMonstertype(int monstertype) {

this.monstertype = monstertype;

}

public int getMonstertype() {

return this.monstertype;

}


public void setRewardlavabadgecount(int[][] rewardlavabadgecount) {

this.rewardlavabadgecount = rewardlavabadgecount;

}

public int[][] getRewardlavabadgecount() {

return this.rewardlavabadgecount;

}


public void setDirectsettledecreasehp(int directsettledecreasehp) {

this.directsettledecreasehp = directsettledecreasehp;

}

public int getDirectsettledecreasehp() {

return this.directsettledecreasehp;

}


public void setDirectsettlefightpower(int directsettlefightpower) {

this.directsettlefightpower = directsettlefightpower;

}

public int getDirectsettlefightpower() {

return this.directsettlefightpower;

}




}
