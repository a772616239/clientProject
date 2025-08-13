package cfg;
import model.base.baseConfigObject;
public class  DailyFirstRechargeObject implements baseConfigObject{



private int id;

private int[] segmentdays;

private int[] dailyreward;

private int rechargecycle;

private int[] bigrewardindex;

private int bigrewardmarqueeid;

private int[] extratimesday;

private int[][] towerreward;

private int[][] towerweight;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setSegmentdays(int[] segmentdays) {

this.segmentdays = segmentdays;

}

public int[] getSegmentdays() {

return this.segmentdays;

}


public void setDailyreward(int[] dailyreward) {

this.dailyreward = dailyreward;

}

public int[] getDailyreward() {

return this.dailyreward;

}


public void setRechargecycle(int rechargecycle) {

this.rechargecycle = rechargecycle;

}

public int getRechargecycle() {

return this.rechargecycle;

}


public void setBigrewardindex(int[] bigrewardindex) {

this.bigrewardindex = bigrewardindex;

}

public int[] getBigrewardindex() {

return this.bigrewardindex;

}


public void setBigrewardmarqueeid(int bigrewardmarqueeid) {

this.bigrewardmarqueeid = bigrewardmarqueeid;

}

public int getBigrewardmarqueeid() {

return this.bigrewardmarqueeid;

}


public void setExtratimesday(int[] extratimesday) {

this.extratimesday = extratimesday;

}

public int[] getExtratimesday() {

return this.extratimesday;

}


public void setTowerreward(int[][] towerreward) {

this.towerreward = towerreward;

}

public int[][] getTowerreward() {

return this.towerreward;

}


public void setTowerweight(int[][] towerweight) {

this.towerweight = towerweight;

}

public int[][] getTowerweight() {

return this.towerweight;

}




}
