package cfg;
import model.base.baseConfigObject;
public class  MistWordMapInfoConfigObject implements baseConfigObject{



private int mapid;

private int[][] sweepmissions;

private int[] targetmission;

private int[] unlockcondition;

private int[][] finishreward;

private int[][] sweepreward;

private int sweeprewardcount;

private int[][] sweepconsume;




public void setMapid(int mapid) {

this.mapid = mapid;

}

public int getMapid() {

return this.mapid;

}


public void setSweepmissions(int[][] sweepmissions) {

this.sweepmissions = sweepmissions;

}

public int[][] getSweepmissions() {

return this.sweepmissions;

}


public void setTargetmission(int[] targetmission) {

this.targetmission = targetmission;

}

public int[] getTargetmission() {

return this.targetmission;

}


public void setUnlockcondition(int[] unlockcondition) {

this.unlockcondition = unlockcondition;

}

public int[] getUnlockcondition() {

return this.unlockcondition;

}


public void setFinishreward(int[][] finishreward) {

this.finishreward = finishreward;

}

public int[][] getFinishreward() {

return this.finishreward;

}


public void setSweepreward(int[][] sweepreward) {

this.sweepreward = sweepreward;

}

public int[][] getSweepreward() {

return this.sweepreward;

}


public void setSweeprewardcount(int sweeprewardcount) {

this.sweeprewardcount = sweeprewardcount;

}

public int getSweeprewardcount() {

return this.sweeprewardcount;

}


public void setSweepconsume(int[][] sweepconsume) {

this.sweepconsume = sweepconsume;

}

public int[][] getSweepconsume() {

return this.sweepconsume;

}




}
