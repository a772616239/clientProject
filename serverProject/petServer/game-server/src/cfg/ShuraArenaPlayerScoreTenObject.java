package cfg;
import model.base.baseConfigObject;
public class  ShuraArenaPlayerScoreTenObject implements baseConfigObject{



private int id;

private int areaid;

private int[] section;

private int[][] dailyreward;

private int[][] weeklyreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setAreaid(int areaid) {

this.areaid = areaid;

}

public int getAreaid() {

return this.areaid;

}


public void setSection(int[] section) {

this.section = section;

}

public int[] getSection() {

return this.section;

}


public void setDailyreward(int[][] dailyreward) {

this.dailyreward = dailyreward;

}

public int[][] getDailyreward() {

return this.dailyreward;

}


public void setWeeklyreward(int[][] weeklyreward) {

this.weeklyreward = weeklyreward;

}

public int[][] getWeeklyreward() {

return this.weeklyreward;

}




}
