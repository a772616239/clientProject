package cfg;
import model.base.baseConfigObject;
public class  ArenaRankingRewardObject implements baseConfigObject{



private int id;

private int danid;

private int startranking;

private int endranking;

private int dailyrewards;

private int weeklyrewards;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDanid(int danid) {

this.danid = danid;

}

public int getDanid() {

return this.danid;

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


public void setWeeklyrewards(int weeklyrewards) {

this.weeklyrewards = weeklyrewards;

}

public int getWeeklyrewards() {

return this.weeklyrewards;

}




}
