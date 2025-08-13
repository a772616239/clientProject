package cfg;
import model.base.baseConfigObject;
public class  OfferRewardBossObject implements baseConfigObject{



private int id;

private int boss;

private int star;

private int[] buff;

private int[][] reward;

private int[][] fight_reward1;

private int[][] fight_reward2;

private int[][] fight_reward3;

private int maxchooserewardcount;

private int generaterewardcount;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setBoss(int boss) {

this.boss = boss;

}

public int getBoss() {

return this.boss;

}


public void setStar(int star) {

this.star = star;

}

public int getStar() {

return this.star;

}


public void setBuff(int[] buff) {

this.buff = buff;

}

public int[] getBuff() {

return this.buff;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setFight_reward1(int[][] fight_reward1) {

this.fight_reward1 = fight_reward1;

}

public int[][] getFight_reward1() {

return this.fight_reward1;

}


public void setFight_reward2(int[][] fight_reward2) {

this.fight_reward2 = fight_reward2;

}

public int[][] getFight_reward2() {

return this.fight_reward2;

}


public void setFight_reward3(int[][] fight_reward3) {

this.fight_reward3 = fight_reward3;

}

public int[][] getFight_reward3() {

return this.fight_reward3;

}


public void setMaxchooserewardcount(int maxchooserewardcount) {

this.maxchooserewardcount = maxchooserewardcount;

}

public int getMaxchooserewardcount() {

return this.maxchooserewardcount;

}


public void setGeneraterewardcount(int generaterewardcount) {

this.generaterewardcount = generaterewardcount;

}

public int getGeneraterewardcount() {

return this.generaterewardcount;

}




}
