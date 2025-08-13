package cfg;
import model.base.baseConfigObject;
public class  ActivityBossRankingRewardObject implements baseConfigObject{



private int id;

private int startranking;

private int endranking;

private int dailyrewards;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setStartranking(int startranking) {

this.startranking = startranking;

}

public int getStartranking() {

return this.startranking;

}


public void setEndranking(int endranking) {

this.endranking = endranking;

}

public int getEndranking() {

return this.endranking;

}


public void setDailyrewards(int dailyrewards) {

this.dailyrewards = dailyrewards;

}

public int getDailyrewards() {

return this.dailyrewards;

}




}
